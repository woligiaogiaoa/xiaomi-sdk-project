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
import com.jiuwan.publication.http.JsonCallback;
import com.jiuwan.publication.http.LzyResponse;
import com.jiuwan.publication.login.SlugBean;
import com.jiuwan.publication.pay.HuaweiPayParam;
import com.jiuwan.publication.pay.OrderNumberBean;
import com.jiuwan.publication.pay.OrderUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnInitProcessListener;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.TreeMap;
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
                    Log.e("FUC", ""
                            + "\nuid:" + accountInfo.getUid()
                            + "\nsessionId:" + accountInfo.getSessionId()
                            + "\nnikeName:" + accountInfo.getNikename());
                    //todo :called after server login
                    serverLogin(accountInfo.getUid(), accountInfo.getSessionId(), new JsonCallback<LzyResponse<SlugBean>>() {
                        @Override
                        public void onSuccess(Response<LzyResponse<SlugBean>> response) {
                            if(response!=null && response.body()!=null&& response.body().data!=null){
                                SlugBean data = response.body().data;
                                loginCallback.onSuccess(data.getSlug());
                            }
                            else {
                                loginCallback.onFailure("empty user",-1);
                            }
                        }

                        @Override
                        public void onError(String errorMsg, int code) {
                            super.onError(errorMsg, code);
                            loginCallback.onFailure(errorMsg,code);
                        }
                    });
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

    //json pay
    //fixme:对外暴露 ？
    public static void pay(Context context, String payInfo ) {
        jsonPay(payInfo);
    }

    //PAY  by json order info
    public static void jsonPay(String payInfo ) {
        //fixme:创建订单
        h5OrderJsonPay(payInfo);
    }

    public static String TAG="SDK-XIAOMI";

    public static void h5OrderJsonPay(String orderJson){ //需要游戏传过来 huawei 的 productId
        try {
            JSONObject jsonObject = new JSONObject(orderJson);
            String gameNum = jsonObject.optString("game_num", "");
            String amount = jsonObject.optString("value", "");
            //String slug = jsonObject.optString("slug", "");
            String productName = jsonObject.optString("props_name", "");
            String roleName = jsonObject.optString("role_name", "");
            String roleId = jsonObject.optString("role_id", "");
            String serverId = jsonObject.optString("server_id", "");
            String serverName = jsonObject.optString("server_name", "");
            String productID = jsonObject.optString("productID", "-1");
            String callbackUrl = jsonObject.optString("callback_url", "");
            String extendData = jsonObject.optString("extend_data", "");
            HuaweiPayParam huaweiPayParam = new HuaweiPayParam.Builder()
                    .gameOrderNum(gameNum)
                    .price(amount)
                    .productName(productName)
                    .roleName(roleName)
                    .roleID(roleId)
                    .serverID(serverId)
                    .serverName(serverName)
                    .callbackUrl(callbackUrl)
                    .productId(productID)
                    .extendData(extendData)
                    .build();
            paramsPay(huaweiPayParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void paramsPay(HuaweiPayParam platformPayParam) {
        Log.e(TAG, "paramsPay: "+platformPayParam.toString() );
        TreeMap paramsMap =  new TreeMap<String, String>();
        paramsMap.put("game_num", platformPayParam.getGameOrderNum());
        paramsMap.put("value",platformPayParam.getPrice()); //分
        paramsMap.put("props_name",platformPayParam.getProductName());
        paramsMap.put("callback_url",platformPayParam.getCallbackUrl());
        paramsMap.put("extend_data",platformPayParam.getExtendData());
        paramsMap.put("server_id",platformPayParam.getServerID());
        paramsMap.put("server_name",platformPayParam.getServerName());
        paramsMap.put("role_id",platformPayParam.getRoleID());
        paramsMap.put("role_name",platformPayParam.getRoleName());
        paramsMap.put("sign", OrderUtil.encryptPaySign(mActivity, paramsMap));
        //platformPayParam.price=fen2yuan(platformPayParam.price) //price String ext :6.00
        //mainActivity?.showProgress("")
        String inAppProductId= "cs1" ;           //platformPayParam.productId; //todo:匹配小米的 product code
        OkGo.<LzyResponse<OrderNumberBean>>post(ORDER_CREATE)
                .tag(ORDER_CREATE)
                .params(paramsMap)
                .execute(new JsonCallback<LzyResponse<OrderNumberBean>>() {
                    @Override
                    public void onSuccess(Response<LzyResponse<OrderNumberBean>> response) {
                        if(response.body()!=null && response.body().data!=null){
                            OrderNumberBean data = response.body().data;
                            //fixme:根据支付参数 去匹配 code
                            //MiBuyInfo miBuyInfo = createMiBuyInfo( inAppProductId, 1 );
                            MiBuyInfo miBuyInfo = new MiBuyInfo();
                            miBuyInfo.setProductCode( inAppProductId );
                            miBuyInfo.setCount( 1 );
                            //todo:创建订单
                            miBuyInfo.setCpOrderId( data.getNumber());
                            miBuyInfo.setCpUserInfo(data.getNumber());
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
                    }

                    @Override
                    public void onError(String errorMsg, int code) {
                        Toast.makeText(
                             mActivity,errorMsg,Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public static void reportUserGameInfoData(String info, ReportCallback reportCallback) {

    }

    public static void verify(final VerifyCallback verifyCallback) {

    }


    public static void exit(Activity context, ExitCallback exitCallback) {

    }


    public static MiBuyInfo createMiBuyInfo(String productCode, int count )
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

    private static String loginApi=baseUrl+"publisher/sdk/v1/xm/user";
    ///
    private static String deliverApi=baseUrl+"publisher/sdk/v1/order/huawei/successful";

    public static final String ORDER_CREATE = baseUrl+"publisher/sdk/v1/order";

    private static void serverLogin(String uid,String session, JsonCallback<LzyResponse<SlugBean>> callback){
        OkGo.<LzyResponse<SlugBean>> post(loginApi)
                .tag(loginApi)
                .params("uid",uid)
                .params("m_session",session)
                .execute(callback);
    }

  /*  public static void deliverProduct(String purchaseToken,String productId, String orderNumber,JsonCallback<SimpleResponse> callback){
        OkGo.<SimpleResponse> post(deliverApi)
                .tag(deliverApi)
                .params("purchaseToken",purchaseToken)
                .params("productId",productId)
                .params("order_no",orderNumber)
                .execute(callback);
    }*/
}