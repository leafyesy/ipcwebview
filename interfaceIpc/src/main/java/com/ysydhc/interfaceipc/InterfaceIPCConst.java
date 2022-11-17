package com.ysydhc.interfaceipc;

import java.util.HashMap;

public class InterfaceIPCConst {

    public static final String IPC_KEY_ARGS = "args_obj";

    public static final int BINDER_CODE_OBJ_CONNECT = 100;
    public static final String BINDER_CODE_OBJ_CONNECT_PACKAGE = "com.ysydhc.interfaceipc.IObjectConnect";

    public static final int BINDER_CODE_METHOD_CALL = 101;
    public static final String BINDER_CODE_METHOD_CALL_PACKAGE = "com.ysydhc.interfaceipc.IMethodChannelBinder";

    public static final HashMap<Integer, String> codeToClassMap = new HashMap<Integer, String>();

    static {
        codeToClassMap.put(BINDER_CODE_OBJ_CONNECT, BINDER_CODE_OBJ_CONNECT_PACKAGE);
        codeToClassMap.put(BINDER_CODE_METHOD_CALL, BINDER_CODE_METHOD_CALL_PACKAGE);
    }

}
