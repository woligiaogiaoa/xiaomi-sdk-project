package com.jiuwan.mlbbjx.mi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jiuwan.publication.PublicationSDK;

public class SplashActivity extends Activity {
    private static final int CHECK_INTERVAL_MS = 200;
    private static final int SPLASH_TIME_MS = 2 * 1000;

    private static final int MSG_CHECK_MISPLASH = 1000;
    private static final int MSG_FINISH_MY_SPLASH = 2000;

    private class SplashHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_CHECK_MISPLASH:
                    if (true) {
                        if (PublicationSDK.miSplashEnd) {
                            sendEmptyMessageDelayed(MSG_FINISH_MY_SPLASH, SPLASH_TIME_MS);
                        } else {
                            sendEmptyMessageDelayed(MSG_CHECK_MISPLASH, CHECK_INTERVAL_MS);
                        }
                    } else {
                        sendEmptyMessageDelayed(MSG_FINISH_MY_SPLASH, SPLASH_TIME_MS);
                    }
                    break;

                case MSG_FINISH_MY_SPLASH: {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            }
        }
    }

    private SplashHandler mHandler = new SplashHandler();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler.sendEmptyMessage(MSG_CHECK_MISPLASH);

    }
}
