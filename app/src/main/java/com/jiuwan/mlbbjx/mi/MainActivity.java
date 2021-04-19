package com.jiuwan.mlbbjx.mi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jiuwan.publication.PublicationSDK;
import com.jiuwan.publication.callback.LoginCallback;
import com.xiaomi.gamecenter.sdk.MiCommplatform;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MiCommplatform.getInstance().onUserAgreed(this);
        PublicationSDK.setLoginCallback(new LoginCallback() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(MainActivity.this,"session:"+result,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String msg, int code) {
                Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void login(View view) {
        PublicationSDK.login(this);
    }
}