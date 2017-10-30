package com.lvhiei.androidtest.test;

import com.lvhiei.androidtest.Tools.VVRoomCodecDecoder;
import com.lvhiei.androidtest.Tools.VideoConfig;
import com.lvhiei.androidtest.test.BaseTest;

import java.nio.ByteBuffer;

/**
 * Created by mj on 17-10-30.
 */


public class Mi3MediaCodecDecoderTest extends BaseTest {

    private String mFlvFile = "/sdcard/51vv/mi3.flv";
    private VVRoomCodecDecoder mRoomDecoder = null;
    private ByteBuffer mBuffer = null;
    private int mBufferLen = 0;
    private long mTimestamp = 0;

    @Override
    protected int localTest() {
        VideoConfig config = new VideoConfig();
        config.setWidth(480);
        config.setHeight(352);
        config.setBitrate(400);
        config.setGop(1);
        config.setFramerate(15);

        mBuffer = ByteBuffer.allocateDirect(480*360*3);

        mRoomDecoder = new VVRoomCodecDecoder(config, 0);

        while (readNextPacket() > 0){
            if(!mRoomDecoder.isValid()){
                if(isSps(mBuffer)){
                    mRoomDecoder.parseSPSPPS(mBuffer, mBufferLen);
                }
            }

            if(mRoomDecoder.isValid() && !mRoomDecoder.isStarted()){
                mRoomDecoder.start();
            }
            while (!mRoomDecoder.push2decoder(mBuffer, mBufferLen, mTimestamp));
        }

        mRoomDecoder.stop();

        return 0;
    }


    private boolean isSps(ByteBuffer es){
        if (es.get(2) == 1){
            return (es.get(3) & 0x1f) == 7;
        }else{
            return (es.get(4) & 0x1f) == 7;
        }
    }

    public int readNextPacket(){
        return 0;
    }



}
