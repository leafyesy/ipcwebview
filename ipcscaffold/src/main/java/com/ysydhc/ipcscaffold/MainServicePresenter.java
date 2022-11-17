package com.ysydhc.ipcscaffold;

import android.annotation.SuppressLint;
import android.app.Service;
import android.os.IBinder;
import android.os.RemoteException;

import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.ipcscaffold.service.MainService;

public class MainServicePresenter extends ProcessServicePresenter {

    @SuppressLint("StaticFieldLeak")
    private static volatile MainServicePresenter instance = null;

    public static MainServicePresenter getInstance() {
        if (instance == null) {
            synchronized (MainServicePresenter.class) {
                if (instance == null) {
                    instance = new MainServicePresenter();
                }
            }
        }
        return instance;
    }

    private MainServicePresenter() {
    }

    @Override
    protected Class<? extends Service> getServiceClass() {
        return MainService.class;
    }

    @Override
    protected void serviceConnectedCallback() {
        LogUtil.i("MainServicePresenter", "main serviceConnected");
    }

    @Override
    protected void serviceDisConnectedCallback() {
        LogUtil.i("MainServicePresenter", "main serviceDisConnected");
    }

    public static class MainBinderPoolImpl extends IBinderPool.Stub {

        public MainBinderPoolImpl() {}

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException, NullPointerException {
            IBinder binder = null;
            IBinderProvider binderProvider = MainServicePresenter.getInstance().binderProviderHashMap.get(binderCode);
            if (binderProvider != null) {
                binder = binderProvider.binderProvider();
            }
            return binder;
        }
    }


}
