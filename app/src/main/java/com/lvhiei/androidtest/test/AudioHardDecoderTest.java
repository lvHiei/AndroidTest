package com.lvhiei.androidtest.test;

import com.lvhiei.androidtest.JniTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

/**
 * Created by mj on 17-9-12.
 */


public class AudioHardDecoderTest extends BaseTest {

    private String lcAAcPath ="/sdcard/android_test/lc.aac";

    private AudioHardDecoder mAudioDecoder;

    private static final int ONE_AAC_MAX_LEN = 4096;
    private ByteBuffer mAacData;

    private static final int ONE_FRAME_SAMPLES = 1024;
    private static final int ONE_SECONS_SAMPLES = 44100;
    private static final double ONE_PACKET_TIMESTAMP = (1000.0*ONE_FRAME_SAMPLES/ONE_SECONS_SAMPLES);

    private boolean mWantStop = false;

    private long mReader = 0;

    protected Thread mGetPcmDataThread;
    protected Runnable mGetPcmDataRunnable = new Runnable() {
        @Override
        public void run() {
            while (!mWantStop && mAudioDecoder.isStarted()){
                mAudioDecoder.getPcmData(null);
            }
        }
    };


    @Override
    protected int localTest() {

        if(!openAacFile()){
            return -2;
        }

        mAudioDecoder = new AudioHardDecoder();
        mAudioDecoder.start();

        if(!mAudioDecoder.isStarted()){
            return -1;
        }

        mAacData = ByteBuffer.allocateDirect(ONE_AAC_MAX_LEN);

//        mWantStop = false;
//        mGetPcmDataThread = new Thread(mGetPcmDataRunnable);
//        mGetPcmDataThread.start();

        long npacket = 0;
        int status;
        int aacLen = readAPacket();
        while(aacLen > 0){
            ++npacket;
            status = mAudioDecoder.pushAacData(mAacData, aacLen, (long) (npacket * ONE_PACKET_TIMESTAMP));
            while (status < 0){
                status = mAudioDecoder.pushAacData(mAacData, aacLen, (long) (npacket * ONE_PACKET_TIMESTAMP));
            }

            aacLen = readAPacket();
        }

        closeAacFile();
//        mWantStop = true;
//        try {
//            mGetPcmDataThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        mAudioDecoder.stop();

        release();

        return 0;
    }

    protected int readAPacket(){
        mAacData.clear();
        return JniTools.nativeReadAAudioPacket(mReader, mAacData);
    }

    protected void release(){
        mAudioDecoder = null;
        mAacData = null;
    }

    protected boolean openAacFile(){
        File file = new File(lcAAcPath);
        if(!file.exists()){
            return false;
        }

        mReader = JniTools.nativeOpenAudioFile(lcAAcPath);

        return mReader != 0;
    }

    protected void closeAacFile(){
        JniTools.nativeCloseAudioFile(mReader);
        mReader = 0;
    }
}
