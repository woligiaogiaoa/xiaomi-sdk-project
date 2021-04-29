package com.jiuwan.mlbbjx.mi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jiuwan.publication.PublicationSDK;
import com.jiuwan.publication.callback.LoginCallback;
import com.jiuwan.publication.pay.HuaweiPayParam;
import com.xiaomi.gamecenter.sdk.GameInfoField;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnExitListner;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;

import org.json.JSONObject;

import java.util.UUID;

public class MainActivity extends Activity {
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActivity=this;
        PublicationSDK.onCreate(this);
        PublicationSDK.setLoginCallback(new LoginCallback() {
            @Override
            public void onSuccess(String result) {
                Toast.makeText(MainActivity.this,"session:"+result,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String msg, int code) {
                Toast.makeText(MainActivity.this,"hahhahahahaahhaha"+msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void login(View view) {
        PublicationSDK.login(this);
    }

    public void pay(View view) {
       /* HuaweiPayParam build = new HuaweiPayParam.Builder()
                .callbackUrl("http://test")
                .extendData("http://test")
                .gameOrderNum("testorder123")
                .price("10")
                .productId("cs1")
                .productName("1毛钱测试")
                .roleID("测试1")
                .roleLevel("1")
                .roleName("测试13123")
                .serverID("server001")
                .serverName("server001")
                .build();
        PublicationSDK.paramsPay(build);*/
        test();
     /*   try
        {
            //MiBuyInfo miBuyInfo = PublicationSDK.createMiBuyInfo( "mlbb1", 1 );
            MiBuyInfo miBuyInfo = new MiBuyInfo();
            miBuyInfo.setProductCode( "cs1" );
            miBuyInfo.setCount( 1 );
            //todo:创建订单
            miBuyInfo.setCpOrderId( "xiaomi-test-"+UUID.randomUUID().toString() );
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
        }*/
    }

    private void test() {
        Gson gson=new Gson();
        String s = gson.toJson(new H5testBean(
                "test1234234",
                "1200",
                "test6kuaiqian",
                "role_123213",
                "role_shuaige",
                "server_213213",
                "server_name12321",
                "1",
                "https://www.justtest.cn",
                ""

        ));
        PublicationSDK.pay(this,s);
    }

    @Override
    public void onBackPressed() {
        MiCommplatform.getInstance().miAppExit( this, new OnExitListner()
        {
            @Override
            public void onExit( int code )
            {
                if ( code == MiErrorCode.MI_XIAOMI_EXIT )
                {
                    android.os.Process.killProcess( android.os.Process.myPid() );
                }
            }
        } );

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        PublicationSDK.onActivityResult(requestCode, resultCode, data);
    }




























    /*-----useless------*/
    public void amountPay(View view) {
        int money = Integer.parseInt( "6" );

        MiBuyInfo miBuyInfo = new MiBuyInfo();
        miBuyInfo.setCpOrderId( UUID.randomUUID().toString() );
        miBuyInfo.setCpUserInfo( "xiaomi_test_001" );
        miBuyInfo.setAmount( money );
//                    Gson gson = new Gson();
//                    Toast.makeText(MiAppPaymentActivity.this,gson.a(miBuyInfo),Toast.LENGTH_LONG).show();
    /*    //用户信息，网游必须设置、单机游戏或应用可选
        Bundle mBundle = new Bundle();
        mBundle.putString( GameInfoField.GAME_USER_BALANCE, "1000" );   //用户余额
        mBundle.putString( GameInfoField.GAME_USER_GAMER_VIP, "vip0" );  //vip等级
        mBundle.putString( GameInfoField.GAME_USER_LV, "20" );           //角色等级
        mBundle.putString( GameInfoField.GAME_USER_PARTY_NAME, "猎人" );  //工会，帮派
        mBundle.putString( GameInfoField.GAME_USER_ROLE_NAME, "meteor" ); //角色名称
        mBundle.putString( GameInfoField.GAME_USER_ROLEID, "123456" );    //角色id
        mBundle.putString( GameInfoField.GAME_USER_SERVER_NAME, "峡谷" );  //所在服务器
        miBuyInfo.setExtraInfo( mBundle ); //设置用户信息*/
        try
        {
            MiCommplatform.getInstance().miUniPay(this, miBuyInfo, new OnPayProcessListener() {
                @Override
                public void finishPayProcess(int i) {
                    Log.e("fuckpay", "errorCode"+i);
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