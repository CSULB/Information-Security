package com.gauravbhor.securechat;

import android.app.Application;

import io.realm.Realm;

/**
 * Created by bhorg on 12/4/2016.
 */

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}