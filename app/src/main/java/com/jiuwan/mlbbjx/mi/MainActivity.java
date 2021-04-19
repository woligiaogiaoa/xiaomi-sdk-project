package com.jiuwan.mlbbjx.mi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jiuwan.publication.PublicationSDK;
import com.jiuwan.publication.callback.LoginCallback;
import com.jiuwan.publication.pay.HuaweiPayParam;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;

public class MainActivity extends Activity {
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity=this;
        MiCommplatform.getInstance().onUserAgreed(this);
        PublicationSDK.onCreate(this);
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

    public void pay(View view) {
      /*  HuaweiPayParam build = new HuaweiPayParam.Builder()
                .callbackUrl("http://test")
                .extendData("http://test")
                .gameOrderNum("testorder123")
                .price("600")
                .productId("mlbb1")
                .productName("6块钱测试")
                .roleID("测试1")
                .roleLevel("1")
                .roleName("测试13123")
                .serverID("server001")
                .serverName("server001")
                .build();
        PublicationSDK.pay(this,new Gson().toJson(build));*/
        try
        {
            MiBuyInfo miBuyInfo = PublicationSDK.createMiBuyInfo( "mlbb1", 1 );
            MiCommplatform.getInstance().miUniPay(this, miBuyInfo, new OnPayProcessListener() {
                @Override
                public void finishPayProcess(int i) {
                    switch (i){
                        case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                            Toast.makeText( mActivity,"支付成功", Toast.LENGTH_LONG ).show();
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL:
                            Toast.makeText( mActivity, "支付取消", Toast.LENGTH_LONG ).show();
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE:
                            Toast.makeText( mActivity, "支付失败", Toast.LENGTH_LONG ).show();
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_REPEAT:
                            Toast.makeText( mActivity, "you have purchased", Toast.LENGTH_LONG ).show();
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                            Toast.makeText( mActivity, "正在处理中，不要重复操作", Toast.LENGTH_SHORT ).show();
                            break;
                        case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                            Toast.makeText( mActivity, "请先登录", Toast.LENGTH_LONG ).show();
                            break;
                        default:
                            break;
                    }
                }
            });
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}