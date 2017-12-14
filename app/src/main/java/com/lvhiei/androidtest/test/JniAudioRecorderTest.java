package com.lvhiei.androidtest.test;

import com.lvhiei.androidtest.JniTools;

/**
 * Created by mj on 17-12-14.
 */


public class JniAudioRecorderTest extends BaseTest{

    @Override
    protected int localTest() {
        return JniTools.nativeJniRecorderTest();
    }
}
