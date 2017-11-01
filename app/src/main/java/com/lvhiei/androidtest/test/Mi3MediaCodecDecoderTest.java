package com.lvhiei.androidtest.test;

import com.lvhiei.androidtest.JniTools;
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
    private long mFirstTimestamp = -1;
    private long mMediaInstance;

    @Override
    protected int localTest() {
        VideoConfig config = new VideoConfig();
        config.setWidth(352);
        config.setHeight(640);
        config.setBitrate(400);
        config.setGop(1);
        config.setFramerate(15);

        mBuffer = ByteBuffer.allocateDirect(352*640*3);

        mRoomDecoder = new VVRoomCodecDecoder(config, 0);

        openFile();

        while ((mBufferLen = readNextPacket()) > 0){
            mTimestamp = getTimestamp();

            if(getMediaType() != 1){
                continue;
            }

            if(mFirstTimestamp == -1){
                mFirstTimestamp = mTimestamp;
            }

            if(!mRoomDecoder.isValid()){
                if(isSps(mBuffer)){
                    mRoomDecoder.parseSPSPPS(mBuffer, mBufferLen);
                }
            }

            if(mRoomDecoder.isValid() && !mRoomDecoder.isStarted()){
                mRoomDecoder.start();
            }

            mBuffer.position(0);

            if(mRoomDecoder.isStarted()){
                while (!mRoomDecoder.push2decoder(mBuffer, mBufferLen, mTimestamp - mFirstTimestamp));
            }
        }

        mRoomDecoder.stop();

        closeFile();

        return 0;
    }


    private boolean isSps(ByteBuffer es){
        if (es.get(2) == 1){
            return (es.get(3) & 0x1f) == 7;
        }else{
            return (es.get(4) & 0x1f) == 7;
        }
    }

    private int openFile(){
        mMediaInstance = JniTools.nativeOpenMediaFile(mFlvFile);
        if(mMediaInstance == 0){
            return -1;
        }

        return 0;
    }

    private int readNextPacket(){
        if(mMediaInstance == 0){
            return 0;
        }

        return JniTools.nativeReadMediaPacket(mMediaInstance, mBuffer);
    }

    private long getTimestamp(){
        if(mMediaInstance == 0){
            return 0;
        }

        return JniTools.nativeGetMediaTimestamp(mMediaInstance);
    }

    private int getMediaType(){
        if(mMediaInstance == 0){
            return 0;
        }

        return JniTools.nativeGetMediaType(mMediaInstance);
    }

    private void closeFile(){
        if(mMediaInstance == 0){
            return ;
        }

        JniTools.nativeCloseMediaFile(mMediaInstance);
    }
}
