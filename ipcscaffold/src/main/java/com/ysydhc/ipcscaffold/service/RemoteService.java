package com.ysydhc.ipcscaffold.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ysydhc.ipcscaffold.RemoteServicePresenter;
import com.ysydhc.ipcscaffold.RemoteServicePresenter.RemoteBinderPoolImpl;
import com.ysydhc.ipcscaffold.initiator.InterfaceIPCInitiator;

public class RemoteService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        new InterfaceIPCInitiator().init();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        RemoteServicePresenter.getInstance().holdContext(this);
        return new RemoteBinderPoolImpl();
    }
}
