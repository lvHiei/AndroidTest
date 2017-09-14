package com.lvhiei.androidtest.test;

import android.os.Message;

import com.lvhiei.androidtest.JniTools;

import java.io.File;
import java.io.IOException;

/**
 * Created by mj on 17-9-14.
 */


public class AudioSoftDecoder4Test extends BaseTest {

    private static final int MIC_COUNT = 4;

    private Thread[] mWorkThreads;

    private Runnable[] mRunnables;
    private String lcAAcPath ="/sdcard/android_test/lc.aac";

    private Boolean[] mThreadsEnded;

    private long mBeingTestTime = 0;
    private long mEndTestTime = 0;

    @Override
    public void doTest() {
        mRunnables = new Runnable[MIC_COUNT];
        mWorkThreads = new Thread[MIC_COUNT];
        mThreadsEnded = new Boolean[MIC_COUNT];

        for(int i = 0; i < MIC_COUNT; ++i){
            mThreadsEnded[i] = false;
            mRunnables[i] = new Runnable() {
                @Override
                public void run() {
                    for(int i = 0; i < MIC_COUNT; ++i){
                        if(this == mRunnables[i]){
                            internalRun(i);
                            break;
                        }
                    }
                }
            };
            mWorkThreads[i] = new Thread(mRunnables[i]);
            mWorkThreads[i].start();
        }

        asynGetCpuRateIfNeeded();

        mIsTesting = true;
        mBeingTestTime = System.currentTimeMillis();
    }


    private void internalRun(int idx){
//        String aacpath = "/sdcard/android_test/lc30_" + idx + ".aac";
        File file = new File(lcAAcPath);
        if(file.exists()){
            JniTools.nativeAudioSoftDecoder(lcAAcPath, MIC_COUNT);
        }

        onRunEnded(idx);
    }

    private synchronized void onRunEnded(int idx){
        mThreadsEnded[idx] = true;

        for(int i = 0; i < MIC_COUNT; ++i){
            if(!mThreadsEnded[i]){
                return;
            }
        }

        mEndTestTime = System.currentTimeMillis();
        onTestThreadEnded(0, mEndTestTime - mBeingTestTime);
    }
}
