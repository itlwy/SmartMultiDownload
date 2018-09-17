package com.lwy.downloadlib.utils;


import android.util.Log;

import com.lwy.downloadlib.Config;

/**
 * Created by lwy on 2018/8/12.
 */

public class TraceUtil {
    public static final String TAG = "com.lwy.downloadlib";

    public static void d(String msg) {
        if (Config.isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg) {
        if (Config.isDebug)
            Log.e(TAG, msg);
    }


    public static void i(String msg) {
        if (Config.isDebug)
            Log.i(TAG, msg);
    }

    public static void w(String msg) {
        if (Config.isDebug)
            Log.w(TAG, msg);
    }
}
