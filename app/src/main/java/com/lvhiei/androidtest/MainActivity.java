package com.lvhiei.androidtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lvhiei.androidtest.log.ATLog;
import com.lvhiei.androidtest.test.AACHe2LcTest;
import com.lvhiei.androidtest.test.AudioHardDecoderTest;
import com.lvhiei.androidtest.test.AudioSoftDecoderTest;
import com.lvhiei.androidtest.test.BaseTest;
import com.lvhiei.androidtest.test.ITest;
import com.lvhiei.androidtest.test.MediaPlayer4AudioTest;

public class MainActivity extends AppCompatActivity {
    private ATLog logger = new ATLog(this.getClass().getName());


    private BaseTest mTester = null;
    private TextView mTvTestStatus = null;

    private BaseTest.ITestCallback mTestCallback = new BaseTest.ITestCallback() {
        @Override
        public void onTest(int errcode, long duration) {
            logger.i(String.format("test end cost %d ms, err:%d", duration, errcode));
            mTvTestStatus.setText(String.format("Test ended, err:%d,duration:%d", errcode, duration));
            mTvTestStatus.setTextColor(mTvTestStatus.getResources().getColor(R.color.green));
        }

        @Override
        public void onCPUUseRate(int userRate, int sysRate) {
            mTvTestStatus.setText(String.format("Testing ucpu:%d,scpu:%d", userRate, sysRate));
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(null != mTester && mTester.isTesting()){
                return;
            }

            switch (v.getId()){
                case R.id.btn_test_aacHe2lc:
                {
                    mTester = new AACHe2LcTest();
                }
                    break;
                case R.id.btn_test_audioSoftDecode:
                {
                    mTester = new AudioSoftDecoderTest();
                }
                    break;
                case R.id.btn_test_audioHardDecode:
                {
                    mTester = new AudioHardDecoderTest();
                }
                    break;
                case R.id.btn_test_audio4MediaPlayer:
                {
                    mTester = new MediaPlayer4AudioTest();
                }
                    break;
                default:
                {
                    mTester = new BaseTest();
                }
                    break;
            }

            mTester.setTestCallback(mTestCallback);
            mTester.doTest();
            mTvTestStatus.setText("Testing.....");
            mTvTestStatus.setTextColor(mTvTestStatus.getResources().getColor(R.color.red));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mTvTestStatus = (TextView) findViewById(R.id.tv_test_status);
        findViewById(R.id.btn_test_aacHe2lc).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_test_audioSoftDecode).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_test_audioHardDecode).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_test_audio4MediaPlayer).setOnClickListener(mOnClickListener);
    }

}
