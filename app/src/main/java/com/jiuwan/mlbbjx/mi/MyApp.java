package com.jiuwan.mlbbjx.mi;

import android.app.Application;

import com.jiuwan.publication.PublicationSDK;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PublicationSDK.init(this);
    }
}
