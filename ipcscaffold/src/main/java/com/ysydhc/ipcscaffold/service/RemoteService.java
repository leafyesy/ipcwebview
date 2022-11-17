package com.ysydhc.ipcscaffold.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ysydhc.ipcscaffold.RemoteServicePresenter.RemoteBinderPoolImpl;

public class RemoteService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new RemoteBinderPoolImpl();
    }
}
