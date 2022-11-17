package com.ysydhc.ipcscaffold.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ysydhc.ipcscaffold.RemoteServicePresenter;
import com.ysydhc.ipcscaffold.RemoteServicePresenter.RemoteBinderPoolImpl;

public class RemoteService extends Service {

    private static RemoteServiceInit init;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        RemoteServicePresenter.getInstance().holdContext(this);
        return new RemoteBinderPoolImpl();
    }

    public interface RemoteServiceInit {

        void init();

    }
}
