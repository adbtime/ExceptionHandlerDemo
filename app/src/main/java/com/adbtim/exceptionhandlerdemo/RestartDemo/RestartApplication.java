package com.adbtim.exceptionhandlerdemo.RestartDemo;

import android.app.Application;

import com.adbtim.crashlib.CrashHandler;

/**
 * Created by adbtime on 16/2/10.
 * email: adbtime@outlook.com
 */
public class RestartApplication extends Application{


    @Override
    public void onCreate() {
        super.onCreate();

        CrashHandler.init(this);

        //设置crash时启动的ErrorActivity
        //CrashHandler.setErrorActivityClass(CustomErrorActivity.class);
        //也可以通过在AndroidManifest的activity中添加
        //  <action android:name="com.adbtime.crashlib.ERROR" />
        //作用一样

        //设置crash后，重启的Activity
        //CrashHandler.setRestartActivityClass(MainActivity.class);
        //也可以通过在AndroidManifest的activity中添加
        //  <action android:name="com.adbtime.crashlib.RESTART" />
        //作用一样

    }
}
