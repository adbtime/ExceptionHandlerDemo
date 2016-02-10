package com.adbtim.exceptionhandlerdemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adbtime on 16/2/9.
 * email: adbtime@outlook.com
 */
public class ExceptionHandler implements UncaughtExceptionHandler {

    public static final String TAG = "ExceptionHandler";
    private static ExceptionHandler INSTANCE = new ExceptionHandler();
    //the default UncaughtException handler
    private UncaughtExceptionHandler mDefaultHandler;

    private Context mContext;


    //for the log name
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    private String mVersion;

    private ExceptionHandler() {

    }

    /**
     * get the single instance of ExceptionHandler
     * Java Single Mode:
     * pay attention for the single mode type ,better than the way :
     *
     *  if(INSTANCE ==null){
     *      INSTANCE = new ExceptionHandler();
     *  }......
     *
     * @return
     */
    public static ExceptionHandler getInstance() {
        return INSTANCE;
    }

    /**
     * @param context
     */
    public void init(Context context) {
        mContext = context;
        //make the ExceptionHandler.class be the default UncaughtExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler(this);
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * 1.whether there is try catch to handler the exception or not
     * 2.collect and save the throwable message
     *
     * @param throwable
     * @return true
     */
    private boolean handleException(Throwable throwable) {
        if (throwable == null) {// no try catch to handler
            return false;
        }
        //show the exit tips by toast.
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext, "Sorry, Exception, Exit.", Toast.LENGTH_LONG).show();
                Looper.loop();
            }
        }.start();

        //collect and save the throwable message
        getDeviceInfo(mContext);
        saveToFile(throwable);
        return true;
    }

    /**
     * the core method,
     * when the UncaughtException happen will take this method
     */
    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        if (!handleException(throwable) && mDefaultHandler != null) {
            //the mDefaultHandler will handle the exception
            mDefaultHandler.uncaughtException(thread, throwable);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
            //exit the app
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }


    /**
     * get the device info
     * @param context
     */
    public void getDeviceInfo(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pinfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pinfo != null) {
                mVersion = pinfo.versionName == null ? "null" : pinfo.versionName;

            }
        } catch (PackageManager.NameNotFoundException e) {

        }
    }

    private boolean saveToFile(Throwable throwable) {
        String time = formatter.format(new Date());
        String fileName = "Crash-" + time + ".log";

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            return false;
        }
        String crashDir = Environment.getExternalStorageDirectory().getPath()+"/crash/";
        String crashPath = crashDir + fileName;

        String androidVersion = Build.VERSION.RELEASE;
        String deviceModel = Build.MODEL;
        String manufacturer = Build.MANUFACTURER;

        File file = new File(crashPath);
        if (file.exists()) {
            file.delete();
        }
        else {
            try {
                new File(crashDir).mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }

        PrintWriter writer;
        try {
            writer = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            return false;
        }
        writer.write("Device: " + manufacturer + ", " + deviceModel + "\n");
        writer.write("Android Version: " + androidVersion + "\n");
        if (mVersion != null) writer.write("App Version: " + mVersion + "\n");
        writer.write("---------------------\n\n");
        throwable.printStackTrace(writer);
        writer.close();

        return true;
    }

}
