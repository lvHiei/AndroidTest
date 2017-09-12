package com.lvhiei.androidtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.lvhiei.androidtest.log.ATLog;
import com.lvhiei.androidtest.test.AudioHardDecoderTest;
import com.lvhiei.androidtest.test.AudioSoftDecoderTest;
import com.lvhiei.androidtest.test.BaseTest;
import com.lvhiei.androidtest.test.ITest;

public class MainActivity extends AppCompatActivity {
    private ATLog logger = new ATLog(this.getClass().getName());


    private BaseTest mTester = null;

    private BaseTest.ITestCallback mTestCallback = new BaseTest.ITestCallback() {
        @Override
        public void onTest(long duration) {
            logger.i(String.format("test end cost %d ms", duration));
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(null != mTester && mTester.isTesting()){
                return;
            }

            switch (v.getId()){
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
                default:
                {
                    mTester = new BaseTest();
                }
                    break;
            }

            mTester.setTestCallback(mTestCallback);
            mTester.doTest();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        findViewById(R.id.btn_test_audioSoftDecode).setOnClickListener(mOnClickListener);
        findViewById(R.id.btn_test_audioHardDecode).setOnClickListener(mOnClickListener);
    }

}
