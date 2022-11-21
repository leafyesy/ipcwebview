package com.ysydhc.ipcscaffold;

import java.util.HashMap;

public class BinderCode2Class {

    private static volatile BinderCode2Class instance = null;

    public static BinderCode2Class getInstance() {
        if (instance == null) {
            synchronized (BinderCode2Class.class) {
                if (instance == null) {
                    instance = new BinderCode2Class();
                }
            }
        }
        return instance;
    }

    public final HashMap<Integer, String> codeToClassMap = new HashMap<Integer, String>();

    public void put(int code, String className) {
        codeToClassMap.put(code, className);
    }

}
