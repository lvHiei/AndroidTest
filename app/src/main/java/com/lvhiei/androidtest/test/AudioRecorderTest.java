package com.lvhiei.androidtest.test;

import com.lvhiei.androidtest.Tools.AudioRecorder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by mj on 17-12-8.
 */


public class AudioRecorderTest extends BaseTest {
    private AudioRecorder mRecorder;

    private boolean mbPaused = false;

    @Override
    public void func() {
        if(mbPaused){
            mRecorder.pause(false);
        }else{
            mRecorder.pause(true);
        }

        mbPaused = !mbPaused;
    }

    @Override
    protected int localTest() {
        mRecorder = new AudioRecorder();
        if(!mRecorder.prepare()){
            return -1;
        }

        if(!mRecorder.start()){
            return -1;
        }

        FileOutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream("/sdcard/android_test/test.pcm");
            byte[] buffer = new byte[mRecorder.getBufferSize()];
            int i = 0;
            int length = 0;
            while (i++ < 200){
                length = mRecorder.read(buffer);
                if(length <= 0){
                    --i;
                    continue;
                }
                outputStream.write(buffer);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null != outputStream){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        mRecorder.stop();
        mRecorder.release();

        return 0;
    }
}
