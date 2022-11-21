package com.ysydhc.interfaceipc;

public class InterfaceIPCConst {

    public static final String IPC_KEY_ARGS = "args_obj"; // 跨进程通信传递参数的key
    public static final String IPC_KEY_CALLBACK_CLASS_NAME = "callback_class_name"; // 跨进程通信 当改方法设置的是回调时传递回调类类名的key

    public static final int BINDER_CODE_OBJ_CONNECT = 100;
    public static final String BINDER_CODE_OBJ_CONNECT_PACKAGE = "com.ysydhc.interfaceipc.IObjectConnect";

    public static final int BINDER_CODE_METHOD_CALL = 101;
    public static final String BINDER_CODE_METHOD_CALL_PACKAGE = "com.ysydhc.interfaceipc.IMethodChannelBinder";


}
