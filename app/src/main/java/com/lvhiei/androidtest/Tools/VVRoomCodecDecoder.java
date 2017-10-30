package com.lvhiei.androidtest.Tools;

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
 * Created by mj on 17-8-17.
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VVRoomCodecDecoder {
    private static final ATLog _log = new ATLog(VVRoomCodecDecoder.class.getName());

    private static final int YUV_FMT_NV12 = 2;
    private static final int YUV_FMT_YUV420P = 0;

    private MediaCodec vdecoder;
    private MediaCodecInfo vmci;
    private MediaCodec.BufferInfo vebi;

    private int TIMEOUT_IN_US = 10000;

    // video camera settings.
    private int vcolor;

    // preiview color
    private int pcolor;

    private MediaFormat mDecodedMediaFormat = null;


    private static final String VCODEC = "video/avc";


    private ByteBuffer mSPSBuffer = null;
    private ByteBuffer mPPSBuffer = null;
    private int mSPSLength = 0;
    private int mPPSLength = 0;

    private Object mStartLock = new Object();


    private VideoConfig m_videoConfig = null;
    private int mMicIndex;
    private boolean mbStarted = false;

    private String mErrMsg = "";
    private int mVideoWidth;
    private int mVideoHeight;


    public interface IDecodeCallback{
        void onDecodedFrame(int micIndex, ByteBuffer buffer, MediaCodec.BufferInfo bi, int videoWidth, int videoHeight, MediaFormat format, int color);
    }

    private IDecodeCallback mCallback = null;

    VVRoomCodecDecoder(VideoConfig videoConfig, int micIndex){
        this(videoConfig, micIndex, null);
    }

    VVRoomCodecDecoder(VideoConfig videoConfig, int micIndex, IDecodeCallback callback){
        m_videoConfig = videoConfig;
        mMicIndex = micIndex;
        mCallback = callback;
    }

    public void setDecoderCallback(IDecodeCallback callback){
        mCallback = callback;
    }

    public boolean isValid(){
        return mSPSBuffer != null && mPPSBuffer != null;
    }

    public boolean isStarted(){
        return mbStarted;
    }

    public void setSPSBuffer(ByteBuffer buffer, int length){
        mSPSBuffer = ByteBuffer.allocateDirect(length);
        mSPSBuffer.put(buffer);
//        resetVideoParam();
    }

    public void setPPSBuffer(ByteBuffer buffer, int length){
        mPPSBuffer = ByteBuffer.allocateDirect(length);
        mPPSBuffer.put(buffer);
    }


    public void parseSPSPPS(ByteBuffer buffer, int length){
        if(!isSps(buffer)){
            return;
        }

        int ppsPosition = parseNALUStart(buffer, 4, length);
        if(ppsPosition < 0){
            _log.i(String.format("parseSPSPPS failed(%x,%x,%x,%x,%x)", buffer.get(0), buffer.get(1),buffer.get(2),buffer.get(3),buffer.get(4)));
            return;
        }

        mSPSLength = ppsPosition;

        buffer.position(0);
        mSPSBuffer = ByteBuffer.allocateDirect(mSPSLength);
        MemUtil.nativeMemCopy(mSPSBuffer, 0, buffer, 0, mSPSLength);

        int ppsEndPosition = parseNALUStart(buffer, ppsPosition + 4, length);
        if(ppsEndPosition < 0){
            ppsEndPosition = length;
        }

        mPPSLength = ppsEndPosition - ppsPosition;
        buffer.position(ppsPosition);
        mPPSBuffer = ByteBuffer.allocateDirect(mPPSLength);
        MemUtil.nativeMemCopy(mPPSBuffer, 0, buffer, mSPSLength, mPPSLength);

//        resetVideoParam();

        _log.i(String.format("parseSPSPPSForMic1:spsLength:%d(%x,%x,%x,%x,%x),ppsLength:%d(%x,%x,%x,%x,%x)",
                mSPSLength, mSPSBuffer.get(0), mSPSBuffer.get(1), mSPSBuffer.get(2), mSPSBuffer.get(3), mSPSBuffer.get(4),
                mPPSLength, mPPSBuffer.get(0), mPPSBuffer.get(1), mPPSBuffer.get(2), mPPSBuffer.get(3), mPPSBuffer.get(4)));
    }

    public boolean resetSPSPPSIfNeeded(ByteBuffer buffer, int length){
        if(!isSps(buffer)){
            return false;
        }

        int ppsPosition = parseNALUStart(buffer, 4, length);
        if(ppsPosition < 0){
            _log.i(String.format("parseSPSPPS failed(%x,%x,%x,%x,%x)", buffer.get(0), buffer.get(1),buffer.get(2),buffer.get(3),buffer.get(4)));
            return false;
        }

        int spsLength = ppsPosition;

        int ppsEndPosition = parseNALUStart(buffer, ppsPosition + 4, length);
        if(ppsEndPosition < 0){
            ppsEndPosition = length;
        }
        int ppsLength = ppsEndPosition - ppsPosition;

        if(isSPSPPSChanged(buffer, 0, spsLength, spsLength, ppsLength)){

            _log.i(String.format("spspps changed mic:%d,sps:(%d to %d),pps:(%d to %d)", mMicIndex, mSPSLength, spsLength, mPPSLength, ppsLength));

            buffer.position(0);
            mSPSBuffer = ByteBuffer.allocateDirect(spsLength);
            mSPSLength = spsLength;
            MemUtil.nativeMemCopy(mSPSBuffer, 0, buffer, 0, spsLength);

            mPPSBuffer = ByteBuffer.allocateDirect(ppsLength);
            mPPSLength = ppsLength;
            MemUtil.nativeMemCopy(mPPSBuffer, 0, buffer, spsLength, ppsLength);

//            resetVideoParam();

            return true;
        }

        return false;
    }

    public void restart(){
        internalStop();
        start();
    }

    public void start(){
        if(mSPSBuffer == null || mPPSBuffer == null){
            _log.w(String.format("not set sps or pps, start decoder failed"));
            notifyStart();
            return;
        }

        try {
            // choose the right vdecoder, perfer qcom then google.
            vcolor = chooseVideoDecoder();
        }catch (IllegalStateException e){
            mErrMsg = "chooseVideoDecoder got IllegalStateException.";
            _log.e(mErrMsg);
            notifyStart();
            return;
        }catch (Throwable throwable){
            mErrMsg = "chooseVideoDecoder got unknown error.";
            _log.e(mErrMsg);
            notifyStart();
            return;
        }

        // vdecoder yuv to 264 es stream.
        // requires sdk level 16+, Android 4.1, 4.1.1, the JELLY_BEAN
        try {
//            vdecoder = MediaCodec.createDecoderByType(VCODEC);
            vdecoder = MediaCodec.createByCodecName(vmci.getName());
        } catch (IOException e) {
            mErrMsg = "create vdecoder failed.";
            _log.e(mErrMsg);
            notifyStart();
            return;
        }catch (Throwable t){
            mErrMsg = "create vdecoder failed got unknown error";
            _log.e(mErrMsg);
            notifyStart();
            return;
        }

        try{
            vebi = new MediaCodec.BufferInfo();

            // setup the vdecoder.
            // @see https://developer.android.com/reference/android/media/MediaCodec.html
            MediaFormat vformat = MediaFormat.createVideoFormat(VCODEC, m_videoConfig.getWidth(), m_videoConfig.getHeight());
            vformat.setInteger(MediaFormat.KEY_COLOR_FORMAT, vcolor);
            vformat.setByteBuffer("csd-0", mSPSBuffer);
            vformat.setByteBuffer("csd-1", mPPSBuffer);
            _log.i(String.format("vdecoder %s, color=%d,w:%d,h:%d,fmt;%s",
                    vmci.getName(), vcolor, m_videoConfig.getWidth(), m_videoConfig.getHeight(), vformat.toString()));
            // the following error can be ignored:
            // 1. the storeMetaDataInBuffers error:
            //      [OMX.qcom.video.encoder.avc] storeMetaDataInBuffers (output) failed w/ err -2147483648
            //      @see http://bigflake.com/mediacodec/#q12

            _log.i("configuring decoder...");
            vdecoder.configure(vformat, null, null, 0);

            _log.i("starting decoder...");
            vdecoder.start();

            mbStarted = true;
            _log.i("start decoder success, micIdx:" + mMicIndex);
        }catch (IllegalStateException e){
            e.printStackTrace();
            mErrMsg = "start avc vdecoder failed, got IllegalStateException";
            _log.e(mErrMsg);

        }catch (IllegalArgumentException e){
            e.printStackTrace();
            mErrMsg = "start avc vdecoder failed, got IllegalArgumentException";
            _log.e(mErrMsg);
        }catch (Throwable t){
            mErrMsg = "start avc vdecoder got unknown error";
            _log.e(mErrMsg);
        }
        finally {
            notifyStart();
        }
    }


    public void stop(){
        internalStop();
        mbStarted = false;
        mPPSBuffer = null;
        mPPSBuffer = null;
    }

    public Object getStartLock(){
        return mStartLock;
    }

    private void notifyStart(){
        synchronized (mStartLock){
            mStartLock.notifyAll();
        }
    }

    public boolean push2decoder(ByteBuffer data, int length, long timestamp){

        boolean ret = false;
        try{
            // feed the vdecoder with yuv frame, got the encoded 264 es stream.
            ByteBuffer[] inBuffers = vdecoder.getInputBuffers();
            ByteBuffer[] outBuffers = vdecoder.getOutputBuffers();

            if (true) {
                int inBufferIndex = vdecoder.dequeueInputBuffer(TIMEOUT_IN_US);
                //_log.i(String.format("try to dequeue input vbuffer, ii=%d", inBufferIndex));
                if (inBufferIndex >= 0) {
                    ByteBuffer bb = inBuffers[inBufferIndex];
                    bb.clear();
                    _log.i(String.format("feed h264 to decoder %dB, pts=%d,iskey:%b", length, timestamp, isKeyFrame(data)));
                    bb.put(data);
//                long pts = new Date().getTime() * 1000 - presentationTimeUs;
                    vdecoder.queueInputBuffer(inBufferIndex, 0, length, timestamp * 1000, 0);
                    ret =  true;
                }
            }

            for (;;) {
                int outBufferIndex = vdecoder.dequeueOutputBuffer(vebi, 0);
                //_log.i(String.format("try to dequeue output vbuffer, ii=%d, oi=%d", inBufferIndex, outBufferIndex));
                if (outBufferIndex >= 0) {
                    ByteBuffer bb = outBuffers[outBufferIndex];
                    onDecodedVideoFrame(mMicIndex, bb, vebi);
                    vdecoder.releaseOutputBuffer(outBufferIndex, false);
                }

                if (outBufferIndex < 0) {
                    if(outBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                        mDecodedMediaFormat = vdecoder.getOutputFormat();
                        _log.i("decoder foramt changed : " + mDecodedMediaFormat);
                        resetVideoParam();
                        setPColor(mDecodedMediaFormat.getInteger(MediaFormat.KEY_COLOR_FORMAT));
                    }
                    break;
                }
            }

        }catch (IllegalStateException e){
            e.printStackTrace();
            mErrMsg = "push2decoder failed, got IllegalStateException";
            _log.e(mErrMsg);
            return false;
        }catch (Throwable t){
            mErrMsg = "push2decoder got unknown error";
            _log.e("push2decoder got unknown error");
            return false;
        }

        return ret;
    }

    private void resetVideoParam(){
//        if(null == mSPSBuffer){
//            return;
//        }
//
//        int offset = mSPSBuffer.get(2) == 0x01 ? 3 : 4;
//        int vp = nativeGetVideoParam(mSPSBuffer, offset, mSPSLength);
//        mVideoWidth = (int) (vp & 0xFFFF);
//        mVideoHeight = (int) ((vp >> 16) & 0xFFFF);

        if(null == mDecodedMediaFormat){
            return;
        }

        if (mDecodedMediaFormat.containsKey("crop-left") && mDecodedMediaFormat.containsKey("crop-right")) {
            mVideoWidth = mDecodedMediaFormat.getInteger("crop-right") + 1 - mDecodedMediaFormat.getInteger("crop-left");
            m_videoConfig.setWidth(mVideoWidth);
        }
        if (mDecodedMediaFormat.containsKey("crop-top") && mDecodedMediaFormat.containsKey("crop-bottom")) {
            mVideoHeight = mDecodedMediaFormat.getInteger("crop-bottom") + 1 - mDecodedMediaFormat.getInteger("crop-top");
            m_videoConfig.setHeight(mVideoHeight);
        }


        _log.i(String.format("nativeGetVideoParam mVideoWidth:%d,mVideoHeight:%d", mVideoWidth, mVideoHeight));
    }

    private void onDecodedVideoFrame(int micIndex, ByteBuffer es, MediaCodec.BufferInfo bi) {
        _log.i(String.format("onDecodedVideoFrame size:%d,pts:%d,fmt:%d(0x%x),mic:%d,flag:%d,offset:%d", bi.size, bi.presentationTimeUs/1000, vcolor, vcolor,micIndex, bi.flags, bi.offset));

        if(null != mDecodedMediaFormat){
            if(null != mCallback){
                mCallback.onDecodedFrame(mMicIndex, es, bi, mVideoWidth, mVideoHeight, mDecodedMediaFormat, pcolor);
            }
        }
    }


    private void internalStop(){
        try {
            if(null != vdecoder){
                _log.i("stopping vdecoder micIdx:" + mMicIndex);
                vdecoder.stop();
                vdecoder.release();
                vdecoder = null;
            }
        }catch (IllegalStateException e){
            e.printStackTrace();
            mErrMsg = "stop avc vdecoder failed, got IllegalStateException";
            _log.e(mErrMsg);
        }catch (Throwable t){
            mErrMsg = "stop avc vdecoder got unknown error";
            _log.e(mErrMsg);
        }
    }

    // choose the video encoder by name.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private MediaCodecInfo chooseVideoDecoder(String name, MediaCodecInfo def) {
        int nbCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < nbCodecs; i++) {
            MediaCodecInfo mci = MediaCodecList.getCodecInfoAt(i);
            if (mci.isEncoder()) {
                continue;
            }

            String[] types = mci.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(VCODEC)) {
                    //_log.i(String.format("vdecoder %s types: %s", mci.getName(), types[j]));
                    if (name == null) {
                        return mci;
                    }

                    if (mci.getName().contains(name)) {
                        return mci;
                    }
                }
            }
        }

        return def;
    }


    // choose the right supported color format. @see below:
    // https://developer.android.com/reference/android/media/MediaCodecInfo.html
    // https://developer.android.com/reference/android/media/MediaCodecInfo.CodecCapabilities.html
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int chooseVideoDecoder() {
        // choose the encoder "video/avc":
        //      1. select one when type matched.
        //      2. perfer google avc.
        //      3. perfer qcom avc.
        vmci = chooseVideoDecoder(null, null);
        //vmci = chooseVideoDecoder("google", vmci);
        //vmci = chooseVideoDecoder("qcom", vmci);

        int matchedColorFormat = 0;
        MediaCodecInfo.CodecCapabilities cc = vmci.getCapabilitiesForType(VCODEC);
        for (int i = 0; i < cc.colorFormats.length; i++) {
            int cf = cc.colorFormats[i];
            _log.i(String.format("vdecoder %s supports color fomart 0x%x(%d)", vmci.getName(), cf, cf));

            // choose YUV for h.264, prefer the bigger one.
            // corresponding to the color space transform in onPreviewFrame
            if ((cf >= cc.COLOR_FormatYUV420Planar && cf <= cc.COLOR_FormatYUV420SemiPlanar)) {
//                if (cf > matchedColorFormat)
                {
                    matchedColorFormat = cf;
                    if(matchedColorFormat == cc.COLOR_FormatYUV420Planar){
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < cc.profileLevels.length; i++) {
            MediaCodecInfo.CodecProfileLevel pl = cc.profileLevels[i];
            _log.i(String.format("vdecoder %s support profile %d, level %d", vmci.getName(), pl.profile, pl.level));
        }

        setPColor(matchedColorFormat);

        _log.i(String.format("vdecoder %s choose color format 0x%x(%d)", vmci.getName(), matchedColorFormat, matchedColorFormat));
        return matchedColorFormat;
    }

    private void setPColor(int colorformat){
        switch (colorformat){
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                pcolor = YUV_FMT_YUV420P;
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                pcolor = YUV_FMT_NV12;
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_QCOM_FormatYUV420SemiPlanar:
                pcolor = YUV_FMT_NV12;
                break;
            default:
                pcolor = YUV_FMT_NV12;
        }

        vcolor = colorformat;
    }

    private boolean isSps(ByteBuffer es){
        if (es.get(2) == 1){
            return (es.get(3) & 0x1f) == 7;
        }else{
            return (es.get(4) & 0x1f) == 7;
        }
    }

    private boolean isKeyFrame(ByteBuffer es){
        if(isSps(es)){
            return true;
        }

        if (es.get(2) == 1){
            return (es.get(3) & 0x1f) == 5;
        }else{
            return (es.get(4) & 0x1f) == 5;
        }
    }


    public int parseNALUStart(ByteBuffer buffer, int offset, int length){
        for(int i = offset; i < length - 3; ++i){
            if(buffer.get(i) == 0 && buffer.get(i + 1) == 0){
                if(buffer.get(i + 2) == 1){
                    return i;
                }
                else if(buffer.get(i + 2) == 0 && buffer.get(i + 3) == 1){
                    return i;
                }
            }
        }

        return -1;
    }

    private boolean isSPSPPSChanged(ByteBuffer buffer, int spsOffset, int spsLength, int ppsOffset, int ppsLength){
        if(mSPSLength != spsLength || mPPSLength != ppsLength){
            return true;
        }

        for(int i = 0; i < mSPSLength; ++i){
            if(buffer.get(spsOffset + i) != mSPSBuffer.get(i)){
                return true;
            }
        }

        for(int i = 0; i < mPPSLength; ++i){
            if(buffer.get(ppsOffset + i) != mPPSBuffer.get(i)){
                return true;
            }
        }

        return false;
    }


    private void onError(int errcode){

    }
}
