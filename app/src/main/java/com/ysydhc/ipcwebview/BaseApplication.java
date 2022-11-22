package com.ysydhc.ipcwebview;

import android.app.Application;

import com.ysydhc.commonlib.ProcessUtil;
import com.ysydhc.interfaceipc.InterfaceIPCInitiator;
import com.ysydhc.interfaceipc.RemoteCallbackSupportInitiator;
import com.ysydhc.ipcscaffold.initiator.BinderInitiatorManager;
import com.ysydhc.ipcwebview.ipc.RemoteTestInitiator;
import com.ysydhc.ipcwebview.ipc.RemoteWebViewInitiator;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 获取进程名
        if (ProcessUtil.getCurrentProcessName(this).endsWith("tool")) {
            BinderInitiatorManager.getInstance().addTask(new InterfaceIPCInitiator());
            BinderInitiatorManager.getInstance().addTask(new RemoteCallbackSupportInitiator());
            BinderInitiatorManager.getInstance().addTask(new RemoteTestInitiator());
            BinderInitiatorManager.getInstance().addTask(new RemoteWebViewInitiator());
        } else {
            BinderInitiatorManager.getInstance().addTask(new InterfaceIPCInitiator());
            BinderInitiatorManager.getInstance().addTask(new RemoteCallbackSupportInitiator());
        }
    }
}
