package com.ysydhc.ipcscaffold.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.ipcscaffold.MainServicePresenter;
import com.ysydhc.ipcscaffold.MainServicePresenter.MainBinderPoolImpl;
import com.ysydhc.ipcscaffold.initiator.BinderInitiatorManager;

public class MainService extends Service {

    private static final String TAG = MainService.class.getSimpleName();

    private final MainBinderPoolImpl pool = new MainBinderPoolImpl();

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        pool.getManager().setContext(this);
        BinderInitiatorManager.getInstance().notifyInit(pool.getManager());
        LogUtil.i(TAG, "main service init");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MainServicePresenter.getInstance().holdContext(this);
        return pool;
    }
}
