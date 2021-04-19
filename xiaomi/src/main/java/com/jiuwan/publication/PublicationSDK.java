package com.jiuwan.publication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.jiuwan.publication.callback.ExitCallback;
import com.jiuwan.publication.callback.LoginCallback;
import com.jiuwan.publication.callback.PayCallback;
import com.jiuwan.publication.callback.ReportCallback;
import com.jiuwan.publication.callback.VerifyCallback;
import com.jiuwan.publication.data.DeviceUtils;
import com.jiuwan.publication.data.GameConfig;
import com.lzy.okgo.OkGo;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;

import java.util.List;
import java.util.UUID;

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
                                            //code:小米后台商品code
    public static void pay(Context context, String payInfo ) {
        //fixme:创建订单
        MiBuyInfo miBuyInfo = createMiBuyInfo( payInfo, 1 );
        try
        {
            MiCommplatform.getInstance().miUniPay(mActivity, miBuyInfo, new OnPayProcessListener() {
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


    public static void reportUserGameInfoData(String info, ReportCallback reportCallback) {

    }

    public static void verify(final VerifyCallback verifyCallback) {

    }


    public static void exit(Activity context, ExitCallback exitCallback) {

    }


    private static MiBuyInfo createMiBuyInfo(String productCode, int count )
    {
        MiBuyInfo miBuyInfo = new MiBuyInfo();
        miBuyInfo.setProductCode( productCode );
        miBuyInfo.setCount( count );
        //todo:创建订单
        miBuyInfo.setCpOrderId( UUID.randomUUID().toString() );
//        Gson gson = new Gson();
//        Toast.makeText(this,gson.a(miBuyInfo),Toast.LENGTH_LONG).show();

        return miBuyInfo;
    }


    /*--------------------back end functions---------------------------*/
    private static String baseUrl="https://api.xinglaogame.com/";

    private static String loginApi=baseUrl+"publisher/sdk/v1/huawei/user";
    ///
    private static String deliverApi=baseUrl+"publisher/sdk/v1/order/huawei/successful";

    public static final String ORDER_CREATE = baseUrl+"publisher/sdk/v1/order";

 /*   private static void serverLogin(String huaweiToken,String openId, JsonCallback<LzyResponse<SlugBean>> callback){
        OkGo.<LzyResponse<SlugBean>> post(loginApi)
                .tag(loginApi)
                .params("accesstoken",huaweiToken)
                .params("openid",openId)
                .execute(callback);
    }

    public static void deliverProduct(String purchaseToken,String productId, String orderNumber,JsonCallback<SimpleResponse> callback){
        OkGo.<SimpleResponse> post(deliverApi)
                .tag(deliverApi)
                .params("purchaseToken",purchaseToken)
                .params("productId",productId)
                .params("order_no",orderNumber)
                .execute(callback);
    }*/
}