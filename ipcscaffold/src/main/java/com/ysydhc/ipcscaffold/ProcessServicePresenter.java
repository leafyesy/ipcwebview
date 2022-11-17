package com.ysydhc.ipcscaffold;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;

import com.ysydhc.commonlib.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ProcessServicePresenter {

    private static final String TAG = "ProcessServicePresenter";

    protected Context context;
    protected IBinderPool binderPool;
    private final List<ServiceConnectionCallback> serviceConnectionCallbackList = new ArrayList<>();
    protected final ConcurrentHashMap<Integer, IBinderProvider> binderProviderHashMap = new ConcurrentHashMap<>();

    ProcessServicePresenter() {
    }

    public void holdContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    protected abstract Class<? extends Service> getServiceClass();

    protected abstract void serviceConnectedCallback();

    protected abstract void serviceDisConnectedCallback();

    public void initConnectService() {
        connectRemoteService();
    }

    public void addServiceConnectionCallback(ServiceConnectionCallback callback) {
        if (serviceConnectionCallbackList.contains(callback)) {
            return;
        }
        serviceConnectionCallbackList.add(callback);
    }

    public void removeServiceConnectionCallback(ServiceConnectionCallback callback) {
        serviceConnectionCallbackList.remove(callback);
    }

    public void addBinderProvider(@NonNull IBinderProvider binderProvider) {
        binderProviderHashMap.put(binderProvider.binderCode(), binderProvider);
    }

    public void removeBinderProvider(@NonNull IBinderProvider binderProvider) {
        binderProviderHashMap.remove(binderProvider.binderCode());
    }

    public IBinder queryBinderByCode(int binderCode) {
        IBinder binder = null;
        try {
            if (binderPool != null) {
                binder = binderPool.queryBinder(binderCode);
            }
        } catch (Exception e) {
            LogUtil.exception(TAG, "", e);
        }
        return binder;
    }

    private void connectRemoteService() {
        Intent service = new Intent(context, getServiceClass());
        context.bindService(service, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtil.i("serviceConnection", "  onServiceConnected");
            binderPool = IBinderPool.Stub.asInterface(service);
            serviceConnectedCallback();
            try {
                binderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (ServiceConnectionCallback serviceConnectionCallback : serviceConnectionCallbackList) {
                serviceConnectionCallback.onServiceConnected();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.i("serviceConnection", "  onServiceDisconnected");
            serviceDisConnectedCallback();
            for (ServiceConnectionCallback serviceConnectionCallback : serviceConnectionCallbackList) {
                serviceConnectionCallback.onServiceDisconnected();
            }
        }
    };

    private final IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            binderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
            binderPool = null;
            connectRemoteService();
        }
    };

    public interface ServiceConnectionCallback {

        void onServiceConnected();

        void onServiceDisconnected();
    }


}
