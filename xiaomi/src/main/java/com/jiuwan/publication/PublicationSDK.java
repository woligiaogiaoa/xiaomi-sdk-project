package com.jiuwan.publication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jiuwan.publication.callback.ExitCallback;
import com.jiuwan.publication.callback.LoginCallback;
import com.jiuwan.publication.callback.PayCallback;
import com.jiuwan.publication.callback.ReportCallback;
import com.jiuwan.publication.callback.VerifyCallback;
import com.jiuwan.publication.data.DeviceUtils;
import com.jiuwan.publication.data.GameConfig;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;

import java.util.List;

public class PublicationSDK {

    public static MiAppInfo appInfo;

    public static boolean miSplashEnd = false;

    private static Activity mActivity;

    private static Application mApplication;

    public static void init(Application context) {
        appInfo = new MiAppInfo();
        mApplication=context;
        GameConfig gameConfig = GameConfig.jsonToObject(context, GameConfig.JSON_FILE_NAME);
        if(TextUtils.isEmpty(gameConfig.getMiAppId()) || TextUtils.isEmpty(gameConfig.getMiAppKey())){
            Toast.makeText(context,"请检查配置文件 ,设置 miApp_id,miApp_key",Toast.LENGTH_SHORT).show();
            throw new RuntimeException("请检查配置文件 ,设置 miApp_id,miApp_key");
        }
        appInfo.setAppId(gameConfig.getMiAppId());
        appInfo.setAppKey(gameConfig.getMiAppKey());
        DeviceUtils.setApp(context);
        MiCommplatform.Init(context, appInfo, new OnInitProcessListener() {
            @Override
            public void finishInitProcess(List<String> loginMethod, int gameConfig) {
                Log.i("Demo", "Init success");
            }
            @Override
            public void onMiSplashEnd() {//小米闪屏页结束回调，小米闪屏可配，无闪屏也会返回此回调，游戏的闪屏应当在收到此回调之后开始。
                miSplashEnd = true;//游戏自己的闪屏处理，可参考SplashActivity的实现
            }
        });

    }

    private PublicationSDK(){}




    private static String session;


    private static MiAccountInfo accountInfo;

    private static LoginCallback loginCallback;

    public static void setLoginCallback(LoginCallback a1) {
        loginCallback=a1;
    }

    public static void login(final Context context) {
        MiCommplatform.getInstance().miLogin(mActivity, new OnLoginProcessListener() {
            @Override
            public void finishLoginProcess(int arg0, MiAccountInfo arg1) {
                if (MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS == arg0) {
                    accountInfo = arg1;
                    session = arg1.getSessionId();
                    //handler.sendEmptyMessage(MSG_LOGIN_SUCCESS);
                    //todo : server login
                    loginCallback.onSuccess(session);
                } else if (MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED == arg0) {
                    //handler.sendEmptyMessage(MSG_DO_NOT_REPEAT_OPERATION);
                    loginCallback.onFailure("不要重复操作",MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED);
                } else {
                    //handler.sendEmptyMessage(MSG_LOGIN_FAILED);
                    loginCallback.onFailure("登陆失败",-1);
                }
            }
        });
    }

    public static void onCreate(final Activity activity) {
        mActivity=activity;
    }

    public static void setPayCallback(PayCallback payCallback) {

    }

    public static void pay(Context context, String payInfo) {

    }

    public static void reportUserGameInfoData(String info, ReportCallback reportCallback) {

    }

    public static void verify(final VerifyCallback verifyCallback) {

    }


    public static void exit(Activity context, ExitCallback exitCallback) {

    }
}