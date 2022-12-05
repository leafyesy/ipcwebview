package com.ysydhc.commonlib;

import android.util.Log;

public class LogUtil {

    public static void i(String tag, String message) {
        Log.i(tag, message);
    }

    public static void e(String tag, String message) {
        Log.e(tag, message);
    }

    public static void exception(String tag, String message, Throwable t) {
        Log.e(tag, message, t);
    }

    public static void exception(String tag, Throwable t) {
        Log.e(tag, "", t);
    }

    public static void exception(Throwable t) {
        Log.e("exception", "", t);
    }

    public static void logStackTree(String tag, StackTraceElement[] stackTraceElements) {
        for (int i = 0; i < stackTraceElements.length; i++) {
            Log.d(tag,
                    "getClassName   " + stackTraceElements[i].getClassName() + '\n' +
                            "getFileName   " + stackTraceElements[i].getFileName() + '\n' +
                            "getMethodName   " + stackTraceElements[i].getMethodName() + '\n' +
                            "getLineNumber   " + stackTraceElements[i].getLineNumber() + '\n'
            );
        }
    }

}
