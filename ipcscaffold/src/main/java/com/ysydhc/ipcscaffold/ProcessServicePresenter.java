package com.ysydhc.ipcscaffold;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ysydhc.commonlib.LogUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ProcessServicePresenter {

    private static final String TAG = "ProcessServicePresenter";

    protected Context context;
    protected IBinderPool binderPool;
    private final List<ServiceConnectionCallback> serviceConnectionCallbackList = new ArrayList<>();


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

    public <T> T queryBinderByCode(int binderCode) {
        IBinder binder = null;
        try {
            if (binderPool != null) {
                binder = binderPool.queryBinder(binderCode);
            }
        } catch (Exception e) {
            LogUtil.exception(TAG, "", e);
        }
        return asInterface(binder, binderCode);
    }

    @Nullable
    public IBinderPool getBinderPool() {
        return binderPool;
    }

    public static <T> T asInterface(android.os.IBinder obj, int binderCode) {
        if ((obj == null)) {
            return null;
        }
        try {
            Class<?> forName = Class.forName(
                    BinderCode2Class.getInstance().codeToClassMap.get(binderCode) + "$Stub$Proxy");
            Constructor<?> constructor = forName.getDeclaredConstructor(android.os.IBinder.class);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(obj);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
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

    public static class BinderManager {

        public final ProcessServicePresenter presenter;
        private Context context;

        public Context getContext() {
            if (ZygoteActivity.activity != null) {
                return ZygoteActivity.activity;
            }
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }

        protected final ConcurrentHashMap<Integer, IBinderProvider> binderProviderHashMap = new ConcurrentHashMap<>();

        public BinderManager(ProcessServicePresenter presenter) {
            this.presenter = presenter;
        }

        public ConcurrentHashMap<Integer, IBinderProvider> getBinderProviderHashMap() {
            return binderProviderHashMap;
        }

        public void addBinderProvider(@NonNull IBinderProvider binderProvider) {
            binderProviderHashMap.put(binderProvider.binderCode(), binderProvider);
        }

        public void removeBinderProvider(@NonNull IBinderProvider binderProvider) {
            binderProviderHashMap.remove(binderProvider.binderCode());
        }
    }


}
