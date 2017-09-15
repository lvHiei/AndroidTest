package com.lvhiei.androidtest.test;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.os.Build;

import com.lvhiei.androidtest.log.ATLog;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by mj on 17-9-15.
 */


public class AudioHardDecoder {

    private ATLog logger = new ATLog(this.getClass().getName());

    private static final String AAC_MIME_TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;

    public static final int STATUS_OK = 0;
    public static final int STATUS_MEDIACODEC_NOT_STARTED = -1;
    public static final int STATUS_DEQUEUE_BUFFER_TIMEOUT = -2;

    private static final int TIMEOUT_INPUT_US = 10000;
    private static final int TIMEOUT_OUTPUT_US = 50000;

    private MediaCodec mMediaCodec;
    private MediaCodecInfo mMediaCodecInfo;
    private MediaFormat mMediaFormat;
    private MediaCodec.BufferInfo mBufferInfo;

    private int mAacType;
    private int mChannels;
    private int mSampleRate;
    private int mBitrate;

    private ByteBuffer mESDSData;

    private boolean mCodecStarted = false;

    public AudioHardDecoder() {
        mAacType = 0;
        mChannels = 2;
        mSampleRate = 44100;
        mBitrate = 128;
    }

    public void start(){
        try {
            mMediaFormat = MediaFormat.createAudioFormat(AAC_MIME_TYPE, mSampleRate, mChannels);
            if(0 == mAacType){
                mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            }else if(1 == mAacType){
                mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
            }
            mMediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitrate*1000);
//            byte[] bytes = new byte[2];
//            bytes[0] = 0x12;
//            bytes[1] = 0x20;
//            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            genESDSData();
            mMediaFormat.setByteBuffer("csd-0", mESDSData);
            mBufferInfo = new MediaCodec.BufferInfo();
            chooseAudioDecoder();

            if(null == mMediaCodecInfo){
                logger.error("choose audio decoder failed, mime=%s,samplerate:%d,channels:%d", AAC_MIME_TYPE, mSampleRate, mChannels);
                return;
            }

//            mMediaCodec = MediaCodec.createByCodecName(mMediaCodecInfo.getName());
            mMediaCodec = MediaCodec.createDecoderByType(AAC_MIME_TYPE);
            mMediaCodec.configure(mMediaFormat, null, null, 0);
            mMediaCodec.start();
            mCodecStarted = true;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("start audio decoder got IOException");
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            logger.error("start audio decoder got IllegalArgumentException");
        }catch (IllegalStateException e){
            e.printStackTrace();
            logger.error("start audio decoder got IllegalStateException");
        }catch (Throwable t){
            logger.error("start audio decoder got unknown error");
        }
    }

    public boolean isStarted(){
        return mCodecStarted;
    }

    public int pushAacData(ByteBuffer data, int size, long timestamp){
        if(!mCodecStarted){
            return STATUS_MEDIACODEC_NOT_STARTED;
        }

        int idx = mMediaCodec.dequeueInputBuffer(TIMEOUT_INPUT_US);
        if(idx < 0){
            return STATUS_DEQUEUE_BUFFER_TIMEOUT;
        }

        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        ByteBuffer inputBuffer = inputBuffers[idx];
        inputBuffer.clear();
        inputBuffer.put(data);
        mMediaCodec.queueInputBuffer(idx, 0, size, timestamp * 1000, 0);

        int outIdx = mMediaCodec.dequeueOutputBuffer(mBufferInfo, 0);
        if(outIdx >= 0){
            logger.info("releasing outidx:%d", outIdx);
            mMediaCodec.releaseOutputBuffer(idx, false);
        }

        return STATUS_OK;
    }

    public int getPcmData(byte[] data){
        if(!mCodecStarted){
            return STATUS_MEDIACODEC_NOT_STARTED;
        }

        int idx = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_OUTPUT_US);
        if(idx < 0){
            if(idx == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                logger.info("mediaformat changed:" + mMediaCodec.getOutputFormat());
            }else if (MediaCodec.INFO_TRY_AGAIN_LATER == idx){
                // timeout
            }

            return STATUS_DEQUEUE_BUFFER_TIMEOUT;
        }

        ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
        ByteBuffer outputBuffer = outputBuffers[idx];
        if(null != data){
            outputBuffer.get(data);
        }
        mMediaCodec.releaseOutputBuffer(idx, false);

        return STATUS_OK;
    }

    public void stop(){
        try {
            if(mCodecStarted){
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            }
            mCodecStarted = false;
        }catch (IllegalStateException e){
            e.printStackTrace();
            logger.error("stop audio decoder got IllegalStateException");
        }

    }

    public int getmAacType() {
        return mAacType;
    }

    public void setmAacType(int mAacType) {
        this.mAacType = mAacType;
    }

    public int getmChannels() {
        return mChannels;
    }

    public void setmChannels(int mChannels) {
        this.mChannels = mChannels;
    }

    public int getmSampleRate() {
        return mSampleRate;
    }

    public void setmSampleRate(int mSampleRate) {
        this.mSampleRate = mSampleRate;
    }

    public int getmBitrate() {
        return mBitrate;
    }

    public void setmBitrate(int mBitrate) {
        this.mBitrate = mBitrate;
    }

    private void chooseAudioDecoder(){
        mMediaCodecInfo = chooseAudioDecoder(null);
    }

    private MediaCodecInfo chooseAudioDecoder(String name){
        int codecCounts = MediaCodecList.getCodecCount();
        MediaCodecInfo bestCodecInfo = null;

        for(int i = 0; i < codecCounts; ++i){
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if(codecInfo.isEncoder()){
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for(String type : types){
                if(type.equals(AAC_MIME_TYPE)){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                        MediaCodecInfo.CodecCapabilities codecCapabilities = codecInfo.getCapabilitiesForType(type);
                        if(codecCapabilities.isFormatSupported(mMediaFormat)){
                            if(name == null){
                                bestCodecInfo = codecInfo;
                                return bestCodecInfo;
                            }else if(!codecInfo.getName().contains(name) && bestCodecInfo == null){
                                bestCodecInfo = codecInfo;
                            }else if(codecInfo.getName().contains(name)){
                                bestCodecInfo = codecInfo;
                                return bestCodecInfo;
                            }
                        }
                    }else{
                        if(name == null){
                            bestCodecInfo = codecInfo;
                            return bestCodecInfo;
                        }else if(!codecInfo.getName().contains(name) && bestCodecInfo == null){
                            bestCodecInfo = codecInfo;
                        }else if(codecInfo.getName().contains(name)){
                            bestCodecInfo = codecInfo;
                            return bestCodecInfo;
                        }
                    }

                }
            }
        }

        return bestCodecInfo;
    }

    protected void genESDSData(){
        byte aacObjectType = getAacObjectType();
        byte sampleIdx = getSampleIndex();
        byte channels = getChannels();

        byte byte0 = (byte)((byte)(aacObjectType << 3) | (byte)(sampleIdx >> 1));
        byte byte1 = (byte)((byte)(sampleIdx << 7) | (byte)(channels << 3));

        mESDSData = ByteBuffer.allocate(2);
        mESDSData.put(0, byte0);
        mESDSData.put(1, byte1);
    }

    protected byte getAacObjectType(){
        byte aacObjectType;

        switch (mAacType){
            // LC
            case 0:
                aacObjectType = 2;
                break;
            //HE
            case 1:
                aacObjectType = 3;
                break;
            default:
                aacObjectType = 2;
                break;
        }

        return aacObjectType;
    }

    protected byte getSampleIndex(){
        byte sampleIdx;

        // 这里44100要除以2，不知道为什么
        switch (mSampleRate/2){
            case  96000:
                sampleIdx = 0;
                break;
            case  88200:
                sampleIdx = 1;
                break;
            case  64000:
                sampleIdx = 2;
                break;
            case  48000:
                sampleIdx = 3;
                break;
            case  44100:
                sampleIdx = 4;
                break;
            case  32000:
                sampleIdx = 5;
                break;
            case  24000:
                sampleIdx = 6;
                break;
            case  22050:
                sampleIdx = 7;
                break;
            case  16000:
                sampleIdx = 8;
                break;
            case  12000:
                sampleIdx = 9;
                break;
            case  11025:
                sampleIdx = 10;
                break;
            case  8000:
                sampleIdx = 11;
                break;
            case  7350:
                sampleIdx = 12;
                break;
            default:
                sampleIdx = 7;
                break;
        }

        return sampleIdx;
    }

    protected byte getChannels(){
        byte channels;

        switch (mChannels){
            case 0:
                channels = 0;
                break;
            case 1:
                channels = 1;
                break;
            case 2:
                channels = 2;
                break;
            case 3:
                channels = 3;
                break;
            case 4:
                channels = 4;
                break;
            case 5:
                channels = 5;
                break;
            case 6:
                channels = 6;
                break;
            case 7:
                channels = 8;
                break;
            default:
                channels = 2;
                break;
        }

        return channels;
    }
}
