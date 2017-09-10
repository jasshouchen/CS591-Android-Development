package com.example.fiveguys.trip_buddy_v0.main;


import android.app.Application;

import com.sendbird.android.SendBird;


public class BaseApplication extends Application {

    // connecting to our specific sendbird database
    private static final String APP_ID = "5B353E81-510B-4EA5-A537-8D6D5112C3CF"; // Sendbird Project APP ID
    public static final String VERSION = "3.0.27";

    @Override
    public void onCreate() {
        super.onCreate();
        SendBird.init(APP_ID, getApplicationContext());
    }
}
