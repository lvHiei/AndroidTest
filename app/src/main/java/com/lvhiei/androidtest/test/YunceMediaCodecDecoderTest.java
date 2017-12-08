package com.lvhiei.androidtest.test;

import com.lvhiei.androidtest.JniTools;
import com.lvhiei.androidtest.Tools.VVRoomCodecDecoder;
import com.lvhiei.androidtest.Tools.VideoConfig;
import com.lvhiei.androidtest.log.ATLog;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Created by mj on 17-10-30.
 */


public class YunceMediaCodecDecoderTest extends BaseTest {
    private ATLog log = new ATLog(this.getClass().getName());
    private String mMediaFile = "/sdcard/51vv/mi3.flv";
    private String mMediaFilePath = "/sdcard/51vv/testh264/";
    private String mLogPath = "/sdcard/51vv/yucnelog";
    private VVRoomCodecDecoder mRoomDecoder = null;
    private ByteBuffer mBuffer = null;
    private int mBufferLen = 0;
    private long mTimestamp = 0;
    private long mFirstTimestamp = -1;
    private long mMediaInstance;

    protected void doTestFile(String filename){
        log.i("testing " + filename);

        mMediaFile = filename;

        VideoConfig config = new VideoConfig();
        config.setWidth(352);
        config.setHeight(640);
        config.setBitrate(400);
        config.setGop(1);
        config.setFramerate(15);

        if(mBuffer == null){
            mBuffer = ByteBuffer.allocateDirect(352*640*3);
        }else {
            mBuffer.clear();
        }

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
    }

    @Override
    protected int localTest() {
        File filepath = new File(mMediaFilePath);

        log.setEnableFileLog(true);
        log.setFileLogPath(mLogPath);

        if(filepath.exists()){
            File[] files = filepath.listFiles();

            for(File file : files){
                if(file.isFile()){
                    doTestFile(file.getAbsolutePath());
//                    break;
                }
            }
        }

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
        mMediaInstance = JniTools.nativeOpenMediaFile(mMediaFile);
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
