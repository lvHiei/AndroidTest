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
    private int AUDIO_SAMLPE_RATE = 44100;
    private int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    private int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private int BUFFER_SIZE = 4096;
    private ByteBuffer mReadBuffer;

    private AudioRecord mRecord;
    private int mBufferSize;

    public AudioRecorder(){

    }

    public boolean prepare(){
        mBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMLPE_RATE, AUDIO_CHANNEL, AUDIO_FORMAT);
        if(mBufferSize < BUFFER_SIZE){
            mBufferSize = BUFFER_SIZE;
        }

        try {
            mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMLPE_RATE, AUDIO_CHANNEL, AUDIO_FORMAT, mBufferSize);
        }catch (IllegalArgumentException e){
            log.e("create AudioRecord got IllegalArgumentException");
            return false;
        }

        mReadBuffer = ByteBuffer.allocateDirect(mBufferSize);
        mReadBuffer.clear();

        return mRecord.getState() == AudioRecord.STATE_INITIALIZED;
    }


    public boolean start(){
        if(null == mRecord){
            return false;
        }

        try {
            mRecord.startRecording();
        }catch (IllegalStateException e){
            log.e("create AudioRecord got IllegalStateException");
            return false;
        }

        return true;
    }

    public int getBufferSize(){
        return mBufferSize;
    }

    public ByteBuffer read(){
        if(mRecord == null){
            return null;
        }

        mRecord.read(mReadBuffer, mBufferSize);
        mReadBuffer.position(0);
        return mReadBuffer;
    }

    public int read(byte[] buffer){
        if(mRecord == null){
            return 0;
        }

        return mRecord.read(buffer, 0, buffer.length);
    }

    public boolean pause(boolean paused){
        return true;
    }


    public boolean stop(){
        mRecord.stop();
        return true;
    }


    public boolean release(){
        mRecord.release();
        return true;
    }
}
