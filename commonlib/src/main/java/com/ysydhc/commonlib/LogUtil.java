package com.ysydhc.commonlib;

import android.util.Log;

public class LogUtil {

    public static void i(String tag, String message) {
        Log.i(tag, message);
    }

    public static void exception(String tag, String message, Throwable t) {
        Log.e(tag, message, t);
    }

}
