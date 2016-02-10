package com.adbtim.crashlib;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by adbtime on 16/2/10.
 * email: adbtime@outlook.com
 */
public final class CrashHandler {

    private final static String TAG = "CrashHandler";
    private static Application application;

    //Extras passed to the error activity
    private static final String EXTRA_RESTART_ACTIVITY_CLASS = "crashlib.EXTRA_RESTART_ACTIVITY_CLASS";
    private static final String EXTRA_SHOW_ERROR_DETAILS = "crashlib.EXTRA_SHOW_ERROR_DETAILS";
    private static final String EXTRA_STACK_TRACE = "crashlib.EXTRA_STACK_TRACE";
    private static final String EXTRA_IMAGE_DRAWABLE_ID = "crashlib.EXTRA_IMAGE_DRAWABLE_ID";

    private static final String INTENT_ACTION_ERROR_ACTIVITY = "crashlib.ERROR";
    private static final String INTENT_ACTION_RESTART_ACTIVITY = "crashlib.RESTART";

    private static boolean showErrorDetails = true;
    private static int defaultErrorActivityDrawableId = R.drawable.default_error_image;
    private static Class<? extends Activity> errorActivityClass = null;
    private static Class<? extends Activity> restartActivityClass = null;

    /**
     * init the CrashHandler
     *
     * @param context
     */
    public static void init(Context context) {
        try {
            if (context == null) {
                return;
            }
            application = (Application) context.getApplicationContext();

            //the DefaultUncaughtExceptionHandler
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, final Throwable throwable) {
                    Log.e(TAG, "UncaughtExceptionHandler", throwable);

                    if (errorActivityClass == null) {
                        errorActivityClass = getErrorActivityClass(application);
                    }

                    final Intent intent = new Intent(application, errorActivityClass);
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    String stackTraceString = sw.toString();

                    restartActivityClass = getRestartActivityClass(application);

                    intent.putExtra(EXTRA_STACK_TRACE, stackTraceString);
                    intent.putExtra(EXTRA_RESTART_ACTIVITY_CLASS, restartActivityClass);
                    intent.putExtra(EXTRA_SHOW_ERROR_DETAILS, showErrorDetails);
                    intent.putExtra(EXTRA_IMAGE_DRAWABLE_ID, defaultErrorActivityDrawableId);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    application.startActivity(intent);

                    killCurrentProcess();
                }
            });


        } catch (Exception e) {

        }

    }


    /**
     * get exception message formated
     *
     * @param context
     * @param intent
     * @return errorDetails
     */
    public static String getAllErrorDetailsFromIntent(Context context, Intent intent) {

        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        //Get app version
        String versionName = getVersionName(context);

        String errorDetails = "";

        errorDetails += "Build version: " + versionName + " \n";
        errorDetails += "Current date: " + dateFormat.format(currentDate) + " \n";
        errorDetails += "Device: " + getDeviceName() + " \n\n";
        errorDetails += "Stack trace:  \n";
        errorDetails += getStackTraceFromIntent(intent);
        return errorDetails;
    }


    /**
     * get the VersionName
     *
     * @param context
     * @return versionName
     */
    private static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            return "Unknown";
        }
    }

    /**
     * get the DeviceName
     * The device name, format :"Samsung GT-N7102"
     * @return deviceName
     */
    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    /**
     * make the first char of string value uppercase
     *
     * @param s
     * @return The capitalized string
     */
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    /**
     * getStackTraceFromIntent
     *
     * @param intent
     * @return The stacktrace
     */
    public static String getStackTraceFromIntent(Intent intent) {
        return intent.getStringExtra(CrashHandler.EXTRA_STACK_TRACE);
    }

    /**
     * judge whether to show detail error message or not
     *
     * @param intent
     * @return
     */
    public static boolean isShowErrorDetailsFromIntent(Intent intent) {
        return intent.getBooleanExtra(CrashHandler.EXTRA_SHOW_ERROR_DETAILS, true);
    }
    /**
     * Closes the app.
     *
     * @param activity The current error activity.
     */
    public static void closeApplication(Activity activity) {
        activity.finish();
        killCurrentProcess();
    }


    /**
     * restartApplicationWithIntent
     *
     * @param activity The current error activity.
     * @param intent   The Intent
     */
    public static void restartApplicationWithIntent(Activity activity, Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.finish();
        activity.startActivity(intent);
        killCurrentProcess();
    }

    /**
     * get Restart Activity Class From Intent, returns the restart activity class extra from it.
     *
     * @param intent The Intent.
     * @return The restart activity class
     */
    @SuppressWarnings("unchecked")
    public static Class<? extends Activity> getRestartActivityClassFromIntent(Intent intent) {
        Serializable serializedClass = intent.getSerializableExtra(CrashHandler.EXTRA_RESTART_ACTIVITY_CLASS);

        if (serializedClass != null && serializedClass instanceof Class) {
            return (Class<? extends Activity>) serializedClass;
        } else {
            return null;
        }
    }

    /**
     * get RestartActivity Class
     *
     * It will first get activities from the AndroidManifest with intent filter
     * 首先将从AndroidManifest的intent filter中查找
     * <action android:name="com.adbtime.crashlib.RESTART" />,
     * 如果找不到,将会返回 default lancher
     *
     * @param context
     * @return restart activity class
     */
    private static Class<? extends Activity> getRestartActivityClass(Context context) {
        Class<? extends Activity> resolvedActivityClass;

        //If action is defined, use that
        resolvedActivityClass = CrashHandler.getRestartActivityClassWithIntentFilter(context);

        //Else, get the default launcher activity
        if (resolvedActivityClass == null) {
            resolvedActivityClass = getLauncherActivity(context);
        }

        return resolvedActivityClass;
    }

    /**
     * used to get the first activity with an intent-filter
     * <action android:name="com.adbtime.crashlib.RESTART" />,
     * If there is no activity with that intent filter, returns null.
     *
     * @param context
     * @return activity class
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getRestartActivityClassWithIntentFilter(Context context) {
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(
                new Intent().setAction(INTENT_ACTION_RESTART_ACTIVITY),
                PackageManager.GET_RESOLVED_FILTER);

        if (resolveInfos != null && resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            try {
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {

            }
        }

        return null;
    }

    /**
     * used to get the default launcher activity for the app.
     * If there is no ,returns null.
     *
     * @param context
     * @return activity class
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getLauncherActivity(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            try {
                return (Class<? extends Activity>) Class.forName(intent.getComponent().getClassName());
            } catch (ClassNotFoundException e) {

            }
        }

        return null;
    }

    /**
     * get Error Activity Class
     * 将从 AndroidManifest的intent filter中查找
     * <action android:name="com.adbtime.crashlib.ERROR" />,
     * 如果没有,使用 DefaultErrorActivity
     *
     * @param context
     * @return activity class
     */
    private static Class<? extends Activity> getErrorActivityClass(Context context) {
        Class<? extends Activity> resolvedActivityClass;

        resolvedActivityClass = CrashHandler.getErrorActivityClassWithIntentFilter(context);

        if (resolvedActivityClass == null) {
            resolvedActivityClass = DefaultErrorActivity.class;
        }

        return resolvedActivityClass;
    }

    /**
     * used to get the first activity with an intent-filter
     * <action android:name="com.adbtime.crashlib.ERROR" />,
     * If there is no activity with that intent filter, returns null.
     *
     * @param context
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Class<? extends Activity> getErrorActivityClassWithIntentFilter(Context context) {
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(
                new Intent().setAction(INTENT_ACTION_ERROR_ACTIVITY),
                PackageManager.GET_RESOLVED_FILTER);

        if (resolveInfos != null && resolveInfos.size() > 0) {
            ResolveInfo resolveInfo = resolveInfos.get(0);
            try {
                return (Class<? extends Activity>) Class.forName(resolveInfo.activityInfo.name);
            } catch (ClassNotFoundException e) {

            }
        }

        return null;
    }

    /**
     * set RestartActivityClass
     *
     */
    public static void setRestartActivityClass(Class<? extends Activity> restartActivityClass) {
        CrashHandler.restartActivityClass = restartActivityClass;
    }

    /**
     * set ErrorActivityClass
     *
     */
    public static void setErrorActivityClass(Class<? extends Activity> errorActivityClass) {
        CrashHandler.errorActivityClass = errorActivityClass;
    }

    /**
     * kills the current process.
     * It is used after restarting or killing the app.
     */
    private static void killCurrentProcess() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
