package com.lvhiei.androidtest.test;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.lvhiei.androidtest.log.ATLog;

/**
 * Created by mj on 17-9-12.
 */


public class BaseTest implements ITest {
    private ATLog logger = new ATLog(this.getClass().getName());

    protected boolean mIsTesting = false;

    public static final int MSG_TEST_ENDED = 0;

    public interface ITestCallback{
        void onTest(int errcode, long duration);
    }

    protected ITestCallback mTestCallback = null;

    private Thread mWorkThread = null;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            long begin = System.currentTimeMillis();
            int errcode = localTest();
            long end = System.currentTimeMillis();
            if(null != mHandler){
                Message message = mHandler.obtainMessage(MSG_TEST_ENDED);
                message.arg1 = errcode;
                message.obj = end - begin;
                mHandler.sendMessage(message);
            }
            mIsTesting = false;
        }
    };

    private Handler mHandler;
    private Handler.Callback mHandleCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MSG_TEST_ENDED:
                {
                    if(null != mTestCallback){
                        mTestCallback.onTest(msg.arg1, (Long)msg.obj);
                    }
                }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public BaseTest(){
        mHandler = new Handler(Looper.getMainLooper(), mHandleCallback);
    }

    public void setTestCallback(ITestCallback callback){
        mTestCallback = callback;
    }

    @Override
    public void doTest() {
        logger.i("dotest");
        mWorkThread = new Thread(mRunnable);

        mIsTesting = true;
        mWorkThread.start();
    }

    @Override
    public boolean isTesting() {
        return mIsTesting;
    }

    protected int localTest(){
        logger.i("local testing");
        return 0;
    }
}
