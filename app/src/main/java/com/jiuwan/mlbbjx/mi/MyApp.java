package com.jiuwan.mlbbjx.mi;

import android.app.Application;

import com.jiuwan.publication.PublicationSDK;
import com.jiuwan.publication.http.JsonCallback;
import com.jiuwan.publication.http.LzyResponse;
import com.lzy.okgo.model.Response;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PublicationSDK.init(this);

    }
}
