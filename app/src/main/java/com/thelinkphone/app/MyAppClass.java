package com.thelinkphone.app;

import android.app.Application;
import android.content.Context;

public class MyAppClass extends Application {

    public static Context myContext;
    @Override
    public void onCreate() {
        super.onCreate();
        myContext = this;
    }
}
