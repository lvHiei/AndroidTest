package com.lvhiei.androidtest.test;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.lvhiei.androidtest.log.ATLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by mj on 17-9-12.
 */


public class BaseTest implements ITest {
    private ATLog logger = new ATLog(this.getClass().getName());

    public static final boolean msGetCPURateInProgress = false;

    protected boolean mIsTesting = false;

    public static final int MSG_TEST_ENDED = 0;
    public static final int MSG_GET_CPU = 1;

    public static final int GET_CPU_DELAY_TIME = 2000;

    public interface ITestCallback{
        void onTest(int errcode, long duration);
        void onCPUUseRate(int userRate, int sysRate);
    }

    private String local_app = "com.lvhiei.androidtest";

    protected int mUserCPURate = 0;
    protected int mSysCPURate = 0;


    protected ITestCallback mTestCallback = null;

    protected Thread mWorkThread = null;
    protected Thread mGetCPUThread = null;
    protected Object mLock = new Object();



    protected Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            long begin = System.currentTimeMillis();
            int errcode = localTest();
            long end = System.currentTimeMillis();

            onTestThreadEnded(errcode, end - begin);
        }
    };

    protected Runnable mGetCPURunnable = new Runnable() {
        @Override
        public void run() {
            while (mIsTesting){
                try {
                    getCpuRate();

                    if(mIsTesting && mHandler != null){
                        mHandler.sendEmptyMessage(MSG_GET_CPU);
                    }

                    if(mIsTesting){
                        synchronized (mLock){
                            mLock.wait(GET_CPU_DELAY_TIME);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };

    protected Handler mHandler;
    protected Handler.Callback mHandleCallback = new Handler.Callback() {
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
                case MSG_GET_CPU:
                {
                    if(null != mTestCallback){
                        mTestCallback.onCPUUseRate(mUserCPURate, mSysCPURate);
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
        logger.info("dotest");
        mWorkThread = new Thread(mRunnable);

        mIsTesting = true;
        mWorkThread.start();
        asynGetCpuRateIfNeeded();
    }

    @Override
    public boolean isTesting() {
        return mIsTesting;
    }

    protected int localTest(){
        logger.info("local testing");
        return 0;
    }

    protected void onTestThreadEnded(int errcode, long duration){
        mIsTesting = false;
        synchronized (mLock){
            mLock.notifyAll();
        }

        if(null != mHandler){
            Message message = mHandler.obtainMessage(MSG_TEST_ENDED);
            message.arg1 = errcode;
            message.obj = duration;
            mHandler.sendMessage(message);
        }
    }

    protected void asynGetCpuRateIfNeeded(){
        if(msGetCPURateInProgress){
            mGetCPUThread = new Thread(mGetCPURunnable);
            mGetCPUThread.start();
        }
    }

    protected int getCpuRate(){
        StringBuilder tv = new StringBuilder();
        int rate = 0;

        try {
            String Result;
            Process p;
            p = Runtime.getRuntime().exec("top -n 1");

            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = Result.split("%");
                    tv.append("USER:" + CPUusr[0] + "\n");
                    String[] CPUusage = CPUusr[0].split("User");
                    String[] SYSusage = CPUusr[1].split("System");
                    tv.append("CPU:" + CPUusage[1].trim() + " length:" + CPUusage[1].trim().length() + "\n");
                    tv.append("SYS:" + SYSusage[1].trim() + " length:" + SYSusage[1].trim().length() + "\n");

                    mUserCPURate = Integer.parseInt(CPUusage[1].trim());
                    mSysCPURate = Integer.parseInt(SYSusage[1].trim());

//                    rate = Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim());
                    rate = mUserCPURate + mSysCPURate;
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("cpu:" + tv);
        return rate;
    }
}
