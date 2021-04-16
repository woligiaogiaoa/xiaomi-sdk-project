package com.jz.publication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.jz.publication.callback.ExitCallback;
import com.jz.publication.callback.LoginCallback;
import com.jz.publication.callback.PayCallback;
import com.jz.publication.callback.ReportCallback;
import com.jz.publication.callback.VerifyCallback;

public class PublicationSDK {

    public static void init(Application context) {

    }

    public static void setLoginCallback(LoginCallback loginCallback) {

    }

    public static void login(final Context context) {

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