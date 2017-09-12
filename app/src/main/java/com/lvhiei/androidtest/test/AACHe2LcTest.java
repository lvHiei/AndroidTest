package com.lvhiei.androidtest.test;

import android.os.Environment;

import com.lvhiei.androidtest.JniTools;

import java.io.File;

/**
 * Created by mj on 17-9-12.
 */


public class AACHe2LcTest extends BaseTest {

    private Thread mWorkThread = null;

    //    private String heAAcPath = Environment.getExternalStorageDirectory() + "/android_test/he.aac";
    //    private String lcAAcPath = Environment.getExternalStorageDirectory() + "/android_test/lc.aac";
    private String heAAcPath ="/sdcard/android_test/he.aac";
    private String lcAAcPath ="/sdcard/android_test/lc.aac";

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            long begin = System.currentTimeMillis();

            File file = new File(heAAcPath);
            if(file.exists()){
                JniTools.nativeAacHE2LC(heAAcPath, lcAAcPath);
            }

            long end = System.currentTimeMillis();
            if(mTestCallback != null){
                mTestCallback.onTest(end - begin);
            }
            mIsTesting = false;
        }
    };

    public AACHe2LcTest(){

    }

    @Override
    public void doTest() {
        mWorkThread = new Thread(mRunnable);

        mIsTesting = true;
        mWorkThread.start();
    }
}
