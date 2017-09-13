package com.lvhiei.androidtest.test;

import com.lvhiei.androidtest.JniTools;

import java.io.File;

/**
 * Created by mj on 17-9-12.
 */


public class AudioSoftDecoderTest extends BaseTest {

    //    private String heAAcPath = Environment.getExternalStorageDirectory() + "/android_test/he.aac";
    //    private String lcAAcPath = Environment.getExternalStorageDirectory() + "/android_test/lc.aac";
    private String lcAAcPath ="/sdcard/android_test/lc.aac";


    public AudioSoftDecoderTest(){

    }


    @Override
    protected int localTest() {

        File file = new File(lcAAcPath);
        if(file.exists()){
            JniTools.nativeAudioSoftDecoder(lcAAcPath);
        }

        return 0;
    }
}
