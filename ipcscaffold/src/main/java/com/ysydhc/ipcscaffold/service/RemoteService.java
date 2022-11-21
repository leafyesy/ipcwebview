package com.ysydhc.ipcscaffold.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ysydhc.ipcscaffold.IPCInitiator;
import com.ysydhc.ipcscaffold.RemoteServicePresenter;
import com.ysydhc.ipcscaffold.RemoteServicePresenter.RemoteBinderPoolImpl;
import com.ysydhc.ipcscaffold.initiator.BinderInitiatorManager;

public class RemoteService extends Service {

    private final RemoteBinderPoolImpl pool = new RemoteBinderPoolImpl();

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        BinderInitiatorManager.getInstance().notifyInit(pool.getManager());
        // 绑定主进程服务
        IPCInitiator.startMain(this);
        // 启动远程ZygoteActivity

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        RemoteServicePresenter.getInstance().holdContext(this);
        return pool;
    }
}
