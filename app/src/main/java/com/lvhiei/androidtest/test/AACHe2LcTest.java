package com.lvhiei.androidtest.test;

import android.os.Environment;

import com.lvhiei.androidtest.JniTools;

import java.io.File;

/**
 * Created by mj on 17-9-12.
 */


public class AACHe2LcTest extends BaseTest {

    //    private String heAAcPath = Environment.getExternalStorageDirectory() + "/android_test/he.aac";
    //    private String lcAAcPath = Environment.getExternalStorageDirectory() + "/android_test/lc.aac";
    private String heAAcPath ="/sdcard/android_test/he.aac";
    private String lcAAcPath ="/sdcard/android_test/lc.aac";

    public AACHe2LcTest(){

    }

    @Override
    protected int localTest() {
        File file = new File(heAAcPath);
        if(file.exists()){
            JniTools.nativeAacHE2LC(heAAcPath, lcAAcPath);
        }

        return 0;
    }
}
