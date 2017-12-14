package com.lvhiei.androidtest.Tools;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import com.lvhiei.androidtest.log.ATLog;

import java.nio.ByteBuffer;

/**
 * Created by mj on 17-12-8.
 */


public class AudioRecorder {
    private ATLog log = new ATLog(this.getClass().getName());
    private static final int BUFFER_STRIDE = 4096;

    private static int AUDIO_SAMLPE_RATE = 44100;
    private static int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static int MIN_BUFFER_SIZE = 4096;

    private AudioRecord mRecord;
    private int mBufferSize;
    private int mSampleRate;
    private int mChannels;
    private int mFormat;

    public AudioRecorder(){
        this(AUDIO_SAMLPE_RATE, AUDIO_CHANNEL, AUDIO_FORMAT);
    }

    public AudioRecorder(int samplerate, int channel, int format){
        mSampleRate = samplerate;
        mChannels = channel;
        mFormat = format;
    }

    public boolean prepare(){
        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, mChannels, mFormat);
        if(mBufferSize < MIN_BUFFER_SIZE){
            mBufferSize = MIN_BUFFER_SIZE;
        }

        if(mBufferSize % BUFFER_STRIDE != 0){
            mBufferSize += BUFFER_STRIDE - mBufferSize % BUFFER_STRIDE;
        }

        try {
            mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate, mChannels, mFormat, mBufferSize);
        }catch (IllegalArgumentException e){
            log.e("create AudioRecord got IllegalArgumentException");
            return false;
        }
        catch (Throwable t){
            log.e("create AudioRecord got unknown error");
            return false;
        }

        return mRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }


    public boolean start(){
        if(null == mRecord){
            return false;
        }

        try {
            mRecord.startRecording();
        }catch (IllegalStateException e){
            log.e("start AudioRecord got IllegalStateException");
            return false;
        }
        catch (Throwable t){
            log.e("start AudioRecord got unknown error");
            return false;
        }

        return true;
    }

    public int getBufferSize(){
        return mBufferSize;
    }

    public int read(ByteBuffer buffer, int cap){
        if(mRecord == null || buffer == null){
            log.e("read ByteBuffer failed got null mRecord:" + mRecord + ",buffer:" + buffer);
            return 0;
        }

        if(cap < mBufferSize){
            log.error("read ByteBuffer failed, buffer doesn't enough, cap:%d,min:%d", cap, mBufferSize);
            return 0;
        }

        if(mRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
            log.error("read ByteBuffer failed, record doesn't in recording state, state:%d", mRecord.getRecordingState());
            return 0;
        }

        buffer.position(0);
        int ret = mRecord.read(buffer, mBufferSize);
        buffer.position(0);

        return ret;
    }

    public int read(byte[] buffer){
        if(mRecord == null || null == buffer){
            log.e("read buffer failed got null mRecord:" + mRecord + ",buffer:" + buffer);
            return 0;
        }

        if(buffer.length < mBufferSize){
            log.error("read buffer failed, buffer doesn't enough, cap:%d,min:%d", buffer.length, mBufferSize);
            return 0;
        }

        if(mRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING){
            log.error("read buffer failed, record doesn't in recording state, state:%d", mRecord.getRecordingState());
            return 0;
        }

        return mRecord.read(buffer, 0, buffer.length);
    }

    public boolean pause(boolean paused){
        if(paused){
            if(mRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING){
                return stop();
            }
        }else{
            if(mRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED){
                return start();
            }
        }
        return false;
    }


    public boolean stop(){
        try {
            mRecord.stop();
        }catch (IllegalStateException e){
            log.e("stop AudioRecord got IllegalStateException");
            return false;
        }
        catch (Throwable t){
            log.e("stop AudioRecord got unknown error");
            return false;
        }
        return true;
    }


    public boolean release(){
        mRecord.release();
        return true;
    }
}
