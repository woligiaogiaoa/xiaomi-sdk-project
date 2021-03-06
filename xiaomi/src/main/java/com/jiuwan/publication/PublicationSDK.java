        package com.jiuwan.publication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.print.PageRange;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jiuwan.publication.basedialog.AgreementDialogFragment;
import com.jiuwan.publication.basedialog.DialogActivity;
import com.jiuwan.publication.callback.ExitCallback;
import com.jiuwan.publication.callback.LoginCallback;
import com.jiuwan.publication.callback.PayCallback;
import com.jiuwan.publication.callback.ReportCallback;
import com.jiuwan.publication.callback.VerifyCallback;
import com.jiuwan.publication.data.DeviceUtils;
import com.jiuwan.publication.data.GameConfig;
import com.jiuwan.publication.http.JsonCallback;
import com.jiuwan.publication.http.LzyResponse;
import com.jiuwan.publication.login.ChannelUser;
import com.jiuwan.publication.login.SlugBean;
import com.jiuwan.publication.pay.GoodsAndPrivacy;
import com.jiuwan.publication.pay.HuaweiPayParam;
import com.jiuwan.publication.pay.OrderNumberBean;
import com.jiuwan.publication.pay.OrderUtil;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.xiaomi.gamecenter.sdk.GameInfoField;
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

import java.security.PrivateKey;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.UUID;

import static android.app.Activity.RESULT_OK;
import static com.jiuwan.publication.basedialog.AgreementDialogFragment.AGREE_KEY;

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
            Toast.makeText(context,"????????????????????? ,?????? miApp_id,miApp_key",Toast.LENGTH_SHORT).show();
            throw new RuntimeException("????????????????????? ,?????? miApp_id,miApp_key");
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
            public void onMiSplashEnd() {//??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                miSplashEnd = true;//???????????????????????????????????????SplashActivity?????????
            }
        });
        getGoodsAndPrivavy();

    }

    public static GoodsAndPrivacy goodsAndPrivacy;

    public static void getGoodsAndPrivavy() {
        channelInit(new JsonCallback<LzyResponse<GoodsAndPrivacy>>() {
            @Override
            public void onSuccess(Response<LzyResponse<GoodsAndPrivacy>> response) {
                if(response!=null ){
                    if(response.body()!=null){
                        goodsAndPrivacy=response.body().data;
                    }
                }
            }
        });
    }

    private PublicationSDK(){}




    private static String session;

    private static final int CODE_PRIVACY= 12121;


    private static MiAccountInfo accountInfo;

    private static LoginCallback loginCallback;

    public static void setLoginCallback(LoginCallback a1) {
        loginCallback=a1;
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode ==CODE_PRIVACY ){
            if(resultCode == RESULT_OK){
                MiCommplatform.getInstance().onUserAgreed(mActivity);
                MiCommplatform.getInstance().miLogin(mActivity, new OnLoginProcessListener() {
                    @Override
                    public void finishLoginProcess(int arg0, MiAccountInfo arg1) {
                        Log.e(TAG, "finishLoginProcess: code:"+arg0 );
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
                                        String auth = response.getRawResponse().header(KEY_HTTP_AUTH_HEADER);
                                        loginCallback.onSuccess(new Gson().toJson(
                                                new ChannelUser(data.getSlug(),auth!=null?  auth :""  )
                                        ));
                                        //mayebe we also want to get goods here if previous rerquest failed
                                        if(goodsAndPrivacy==null)
                                            getGoodsAndPrivavy();
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
                            loginCallback.onFailure("??????????????????",MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED);
                        } else {
                            //handler.sendEmptyMessage(MSG_LOGIN_FAILED);
                            loginCallback.onFailure("????????????",-1);
                        }
                    }
                });
            }
            else {
                loginCallback.onFailure("user privacy error",-1);
            }
        }
    }

    public static void login(final Context context) {
        boolean agree= PreferenceManager.getDefaultSharedPreferences(context).getBoolean(AGREE_KEY,false);
        if(!agree){
            try {
                Intent intent=new Intent(mActivity, DialogActivity.class);
                mActivity.startActivityForResult(intent,CODE_PRIVACY);
            } catch (Exception e) {
                e.printStackTrace();
                loginCallback.onFailure("user privacy error",-1);
            }
        }else {
            MiCommplatform.getInstance().onUserAgreed(mActivity);
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
                                    String auth = response.getRawResponse().header(KEY_HTTP_AUTH_HEADER);
                                    loginCallback.onSuccess(new Gson().toJson(
                                            new ChannelUser(data.getSlug(),auth!=null?  auth :""  )
                                    ));
                                    //mayebe we also want to get goods here if previous rerquest failed
                                    if(goodsAndPrivacy==null)
                                        getGoodsAndPrivavy();
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
                        loginCallback.onFailure("??????????????????",MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED);
                    } else {
                        //handler.sendEmptyMessage(MSG_LOGIN_FAILED);
                        loginCallback.onFailure("????????????",-1);
                    }
                }
            });
        }

    }

    public static void onCreate(final Activity activity) {
        mActivity=activity;
    }

    public static void setPayCallback(PayCallback payCallback) {

    }

    //json pay
    //fixme:????????????
    public static void pay(Context context, String payInfo ) {
        jsonPay(payInfo);
    }

    //PAY  by json order info
    public static void jsonPay(String payInfo ) {
        //????????????????????????
        //h5OrderJsonPay(payInfo);
        //????????????
        amountJsonPay(payInfo);
    }

    private static void amountJsonPay(String payInfo) {
        try {
           /* if(goodsAndPrivacy==null){
                Log.e(TAG, "h5OrderJsonPay: ????????????????????????" );
                return;
            }*/
            JSONObject jsonObject = new JSONObject(payInfo);
            String gameNum = jsonObject.optString("game_num", "");
            String amount = jsonObject.optString("fs_value", "");
            if(TextUtils.isEmpty(amount)){
                amount=jsonObject.optString("value", "");
            }

            String productName = jsonObject.optString("props_name", "");
            String roleName = jsonObject.optString("role_name", "");
            String roleId = jsonObject.optString("role_id", "");
            String serverId = jsonObject.optString("server_id", "");
            String serverName = jsonObject.optString("server_name", "");
            String productID = jsonObject.optString("productID", "");
            String callbackUrl = jsonObject.optString("callback_url", "");
            String extendData = jsonObject.optString("extend_data", "");

            String currency=jsonObject.optString("currency", ""); //??????
            String roleLevel=jsonObject.optString("role_level", "");//??????
            String vipLevel=jsonObject.optString("vip_level", ""); //VIP??????
            String partyName=jsonObject.optString("guild_name", ""); //??????
            String role_power=jsonObject.optString("role_power", ""); //??????
            HuaweiPayParam huaweiPayParam = new HuaweiPayParam.Builder()
                    .gameOrderNum(gameNum)
                    .price(amount)
                    .productName(productName)
                    .roleName(roleName)
                    .roleID(roleId)
                    .serverID(serverId)
                    .serverName(serverName)
                    .callbackUrl(callbackUrl)
                    .productId(productID) //inapppay id
                    .extendData(extendData)
                    .build();
            paramsAmoutPay(huaweiPayParam,currency,roleLevel,vipLevel,partyName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void paramsAmoutPay(HuaweiPayParam platformPayParam, String currency, String roleLevel, String vipLevel, String partyName){
        Log.e(TAG, "paramsPay: "+platformPayParam.toString() );
        TreeMap paramsMap =  new TreeMap<String, String>();
        paramsMap.put("game_num", platformPayParam.getGameOrderNum());
        paramsMap.put("value",platformPayParam.getPrice()); //???
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
        String gameProId= platformPayParam.productId ;           //platformPayParam.productId; //todo:??????????????? product code
        OkGo.<LzyResponse<OrderNumberBean>>post(ORDER_CREATE)
                .tag(ORDER_CREATE)
                .params(paramsMap)
                .execute(new JsonCallback<LzyResponse<OrderNumberBean>>() {
                    @Override
                    public void onSuccess(Response<LzyResponse<OrderNumberBean>> response) {
                        if(response.body()!=null && response.body().data!=null){
                            OrderNumberBean data = response.body().data;
                           /* MiBuyInfo miBuyInfo = new MiBuyInfo();
                            miBuyInfo.setProductCode( gameProId );
                            miBuyInfo.setCount( 1 );
                            miBuyInfo.setCpOrderId( data.getNumber());
                            miBuyInfo.setCpUserInfo(data.getNumber());*/
                            int fen = Integer.parseInt( platformPayParam.getPrice() );
                            int yuan= (int)((float)fen /100);
                            Log.e(TAG, "paramsAmoutPay yuan: "+yuan);

                            MiBuyInfo miBuyInfo = new MiBuyInfo();
                            miBuyInfo.setCpOrderId( data.getNumber() );
                            miBuyInfo.setCpUserInfo( data.getNumber() );
                            miBuyInfo.setAmount( yuan );
                            Bundle mBundle = new Bundle();
                            mBundle.putString(GameInfoField.GAME_USER_BALANCE, currency); //????????????
                            mBundle.putString(GameInfoField.GAME_USER_GAMER_VIP,vipLevel); //vip ??????
                            mBundle.putString(GameInfoField.GAME_USER_LV, roleLevel); //????????????
                            mBundle.putString(GameInfoField.GAME_USER_PARTY_NAME, partyName); //???????????????
                            mBundle.putString(GameInfoField.GAME_USER_ROLE_NAME,platformPayParam.getRoleName());
                            mBundle.putString(GameInfoField.GAME_USER_ROLEID, platformPayParam.getRoleID()); //??????id
                            mBundle.putString(GameInfoField.GAME_USER_SERVER_NAME,platformPayParam.getServerName() ); //???????????????
                            miBuyInfo.setExtraInfo(mBundle); //??????????????????
                            try
                            {
                                MiCommplatform.getInstance().miUniPay(mActivity, miBuyInfo, new OnPayProcessListener() {
                                    @Override
                                    public void finishPayProcess(int i) {
                                        switch (i){
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                                                Toast.makeText( mActivity,"????????????", Toast.LENGTH_LONG ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL:
                                                Toast.makeText( mActivity, "????????????", Toast.LENGTH_LONG ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE:
                                                Toast.makeText( mActivity, "????????????", Toast.LENGTH_LONG ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_REPEAT:
                                                Toast.makeText( mActivity, "you have purchased", Toast.LENGTH_LONG ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                                                Toast.makeText( mActivity, "????????????????????????????????????", Toast.LENGTH_SHORT ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                                                Toast.makeText( mActivity, "????????????", Toast.LENGTH_LONG ).show();
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

    public static String TAG="SDK-XIAOMI";

    private static String checkAmount(String amount,String ProductId){
        if(goodsAndPrivacy==null) return null;

        try {
            GoodsAndPrivacy.Good target=null;

            for (GoodsAndPrivacy.Good good : goodsAndPrivacy.getGoods()) {
                if(good.getCp_id().equals(ProductId)){
                    target =good;
                    break;
                }
            }
            if(target==null) return null;
            Float yuan=Float.parseFloat(target.getMoney());
            Integer fen=(int)(yuan * 100);
            return fen.toString().equals(amount) ? target.getC_id() : null;
        }catch (Exception e ){
            return  null;
        }
    }

    public static void h5OrderJsonPay(String orderJson){ //????????????????????? huawei ??? productId
        try {
            if(goodsAndPrivacy==null){
                Log.e(TAG, "h5OrderJsonPay: ????????????????????????" );
                return;
            }
            JSONObject jsonObject = new JSONObject(orderJson);
            String gameNum = jsonObject.optString("game_num", "");
            String amount = jsonObject.optString("fs_value", "");
            if(TextUtils.isEmpty(amount)){
                amount=jsonObject.optString("value", "");
            }
            String productName = jsonObject.optString("props_name", "");
            String roleName = jsonObject.optString("role_name", "");
            String roleId = jsonObject.optString("role_id", "");
            String serverId = jsonObject.optString("server_id", "");
            String serverName = jsonObject.optString("server_name", "");
            String productID = jsonObject.optString("productID", "");
            String callbackUrl = jsonObject.optString("callback_url", "");
            String extendData = jsonObject.optString("extend_data", "");
            String inAppId=checkAmount(amount,productID);
            if(TextUtils.isEmpty(checkAmount(amount,productID))){
                Log.e(TAG, "h5OrderJsonPay: ??????????????????" );
                return;
            }
            HuaweiPayParam huaweiPayParam = new HuaweiPayParam.Builder()
                    .gameOrderNum(gameNum)
                    .price(amount)
                    .productName(productName)
                    .roleName(roleName)
                    .roleID(roleId)
                    .serverID(serverId)
                    .serverName(serverName)
                    .callbackUrl(callbackUrl)
                    .productId(inAppId) //inapppay id
                    .extendData(extendData)
                    .build();
            paramsPay(huaweiPayParam);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    //we use it if we already validated amount and product id
    public static void paramsPay(HuaweiPayParam platformPayParam) {
        Log.e(TAG, "paramsPay: "+platformPayParam.toString() );
        TreeMap paramsMap =  new TreeMap<String, String>();
        paramsMap.put("game_num", platformPayParam.getGameOrderNum());
        paramsMap.put("value",platformPayParam.getPrice()); //???
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
        String inAppProductId= platformPayParam.productId ;           //platformPayParam.productId; //todo:??????????????? product code
        OkGo.<LzyResponse<OrderNumberBean>>post(ORDER_CREATE)
                .tag(ORDER_CREATE)
                .params(paramsMap)
                .execute(new JsonCallback<LzyResponse<OrderNumberBean>>() {
                    @Override
                    public void onSuccess(Response<LzyResponse<OrderNumberBean>> response) {
                        if(response.body()!=null && response.body().data!=null){
                            OrderNumberBean data = response.body().data;
                            MiBuyInfo miBuyInfo = new MiBuyInfo();
                            miBuyInfo.setProductCode( inAppProductId );
                            miBuyInfo.setCount( 1 );
                            //todo:????????????
                            miBuyInfo.setCpOrderId( data.getNumber());
                            miBuyInfo.setCpUserInfo(data.getNumber());
                            try
                            {
                                MiCommplatform.getInstance().miUniPay(mActivity, miBuyInfo, new OnPayProcessListener() {
                                    @Override
                                    public void finishPayProcess(int i) {
                                        switch (i){
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                                                Toast.makeText( mActivity,"????????????", Toast.LENGTH_LONG ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL:
                                                Toast.makeText( mActivity, "????????????", Toast.LENGTH_LONG ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE:
                                                Toast.makeText( mActivity, "????????????", Toast.LENGTH_LONG ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_REPEAT:
                                                Toast.makeText( mActivity, "you have purchased", Toast.LENGTH_LONG ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                                                Toast.makeText( mActivity, "????????????????????????????????????", Toast.LENGTH_SHORT ).show();
                                                break;
                                            case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                                                Toast.makeText( mActivity, "????????????", Toast.LENGTH_LONG ).show();
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
        //todo:????????????
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

    public static final String CHANNEL_INIT = baseUrl+"publisher/sdk/v1/channel";


    public static void channelInit(JsonCallback<LzyResponse<GoodsAndPrivacy>> callback){
        OkGo.<LzyResponse<GoodsAndPrivacy>>
                get(CHANNEL_INIT)
                .tag(CHANNEL_INIT)
                .execute(callback);
    }


}















