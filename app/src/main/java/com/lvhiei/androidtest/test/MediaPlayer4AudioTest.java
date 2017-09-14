package com.lvhiei.androidtest.test;

import android.media.MediaPlayer;
import android.os.Message;

import java.io.IOException;

/**
 * Created by mj on 17-9-13.
 */


public class MediaPlayer4AudioTest extends BaseTest {
    private static final int MIC_COUNT = 4;

    private MediaPlayer[] mMediaPlayers;

    private Thread[] mWorkThreads;

    private Runnable[] mRunnables;

    private Boolean[] mMediaPlayerReleased;

    private long mBeingTestTime = 0;
    private long mEndTestTime = 0;

    @Override
    public void doTest() {
        mRunnables = new Runnable[MIC_COUNT];
        mWorkThreads = new Thread[MIC_COUNT];
        mMediaPlayers = new MediaPlayer[MIC_COUNT];
        mMediaPlayerReleased = new Boolean[MIC_COUNT];

        for(int i = 0; i < MIC_COUNT; ++i){
            mMediaPlayers[i] = new MediaPlayer();
            mMediaPlayerReleased[i] = false;
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


    protected void internalRun(int idx){
        String aacpath = "/sdcard/android_test/lc30_" + idx + ".aac";
        try {
            mMediaPlayers[idx].setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(null != mp){
                        mp.release();

                        onMediaPlayerReleased(mp);
                    }
                }
            });
            mMediaPlayers[idx].setDataSource(aacpath);
            mMediaPlayers[idx].prepare();
            mMediaPlayers[idx].start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected synchronized void onMediaPlayerReleased(MediaPlayer mp){
        boolean allMediaPlayerReleased = true;
        for(int i = 0; i < MIC_COUNT; ++i){
            if(mp == mMediaPlayers[i]){
                mMediaPlayerReleased[i] = true;
            }

            allMediaPlayerReleased = allMediaPlayerReleased && mMediaPlayerReleased[i];
        }

        if(allMediaPlayerReleased){
            mEndTestTime = System.currentTimeMillis();
            onTestThreadEnded(0, mEndTestTime - mBeingTestTime);
        }
    }
}
