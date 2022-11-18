package com.ysydhc.ipcwebview;

import android.app.Application;

import com.ysydhc.commonlib.ProcessUtil;
import com.ysydhc.ipcscaffold.initiator.RemoteBinderInitiatorManager;
import com.ysydhc.ipcwebview.ipc.InterfaceIPCInitiator;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 获取进程名
        if (ProcessUtil.getCurrentProcessName(this).endsWith("tool")) {
            RemoteBinderInitiatorManager.getInstance().addTask(new InterfaceIPCInitiator());
        }
    }
}
