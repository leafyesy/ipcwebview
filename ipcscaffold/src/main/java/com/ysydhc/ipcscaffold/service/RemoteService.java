package com.ysydhc.ipcscaffold.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ysydhc.ipcscaffold.RemoteServicePresenter;
import com.ysydhc.ipcscaffold.RemoteServicePresenter.RemoteBinderPoolImpl;
import com.ysydhc.ipcscaffold.initiator.RemoteBinderInitiatorManager;

public class RemoteService extends Service {

    private final RemoteBinderPoolImpl pool = new RemoteBinderPoolImpl();

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        RemoteBinderInitiatorManager.getInstance().notifyInit(pool.getManager());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        RemoteServicePresenter.getInstance().holdContext(this);
        return pool;
    }
}
