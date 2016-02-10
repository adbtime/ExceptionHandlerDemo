package com.adbtim.exceptionhandlerdemo;

import android.app.Application;

/**
 * Created by adbtime on 16/2/9.
 * email: adbtime@outlook.com
 */
public class ExceptionApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ExceptionHandler exceptionHandler = ExceptionHandler.getInstance();
        exceptionHandler.init(getApplicationContext());
    }
}
