package com.lvhiei.androidtest.test;

import com.lvhiei.androidtest.log.ATLog;

/**
 * Created by mj on 17-9-12.
 */


public class BaseTest implements ITest {
    private ATLog logger = new ATLog(this.getClass().getName());


    protected boolean mIsTesting = false;


    public interface ITestCallback{
        void onTest(long duration);
    }

    protected ITestCallback mTestCallback = null;

    public void setTestCallback(ITestCallback callback){
        mTestCallback = callback;
    }

    @Override
    public void doTest() {
        logger.i("dotest");
        if(null != mTestCallback){
            mTestCallback.onTest(0);
        }
    }

    @Override
    public boolean isTesting() {
        return mIsTesting;
    }
}
