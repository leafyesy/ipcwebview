package com.ysydhc.ipcscaffold;

import android.app.Service;
import android.os.IBinder;
import android.os.RemoteException;

import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.ipcscaffold.service.RemoteService;

public class RemoteServicePresenter extends ProcessServicePresenter {

    private static final String TAG = RemoteServicePresenter.class.getSimpleName();

    private static volatile RemoteServicePresenter instance = null;

    private RemoteServicePresenter() {

    }

    public static RemoteServicePresenter getInstance() {
        if (instance == null) {
            synchronized (RemoteServicePresenter.class) {
                if (instance == null) {
                    instance = new RemoteServicePresenter();
                }
            }
        }
        return instance;
    }

    @Override
    protected Class<? extends Service> getServiceClass() {
        return RemoteService.class;
    }

    @Override
    protected void serviceConnectedCallback() {
        LogUtil.i(TAG, "remote serviceDisConnected");
//        try {
//            getRemoteProcessBinder().initZygoteActivity();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void serviceDisConnectedCallback() {
        LogUtil.i(TAG, "remote serviceDisConnected");
    }

    public static class RemoteBinderPoolImpl extends IBinderPool.Stub {

        public RemoteBinderPoolImpl() {}

        @Override
        public IBinder queryBinder(int binderCode) throws RemoteException, NullPointerException {
            IBinder binder = null;
            IBinderProvider binderProvider = RemoteServicePresenter.getInstance().binderProviderHashMap.get(binderCode);
            if (binderProvider != null) {
                binder = binderProvider.binderProvider();
            }
            return binder;
        }
    }

}
