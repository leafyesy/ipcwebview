package com.ysydhc.ipcscaffold;

import android.app.Service;
import android.os.IBinder;
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

        private final BinderManager manager = new BinderManager();

        public RemoteBinderPoolImpl() {
        }

        public BinderManager getManager() {
            return manager;
        }

        @Override
        public IBinder queryBinder(int binderCode) throws NullPointerException {
            IBinder binder = null;
            IBinderProvider binderProvider = manager.binderProviderHashMap.get(binderCode);
            if (binderProvider != null) {
                binder = (IBinder) binderProvider.binderProvider();
            }
            return binder;
        }
    }

}
