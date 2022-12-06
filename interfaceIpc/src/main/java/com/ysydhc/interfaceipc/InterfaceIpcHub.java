package com.ysydhc.interfaceipc;

import androidx.annotation.Nullable;

import com.ysydhc.interfaceipc.connect.IConnectObjectCreator;
import com.ysydhc.interfaceipc.connect.ObjectConnectBinderImpl;
import com.ysydhc.interfaceipc.proxy.InterfaceProxy;
import com.ysydhc.ipcscaffold.ProcessServicePresenter;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InterfaceIpcHub {

    private static final String TAG = "InterfaceIpcHub";

    private volatile ProcessServicePresenter presenter;

    private InterfaceIpcHub() {
    }

    private static final class InstanceHolder {

        static final InterfaceIpcHub instance = new InterfaceIpcHub();
    }

    public static InterfaceIpcHub getInstance() {
        return InstanceHolder.instance;
    }

    private final ObjectConnectBinderImpl objectConnectBinder = new ObjectConnectBinderImpl();
    private final ConcurrentHashMap<Long, InterfaceProxy<?>> keyToIpcImplMap = new ConcurrentHashMap<Long, InterfaceProxy<?>>();

    public ObjectConnectBinderImpl getObjectConnectBinder() {
        return objectConnectBinder;
    }

    public void setConnectObjectCreateList(IConnectObjectCreator creator) {
        objectConnectBinder.setConnectObjectCreateList(creator);
    }

    public void putIpcImpl(long key, Class<?> clazz, Object value) {
        // 设置代理
        InterfaceProxy<?> interfaceProxy =
                value instanceof InterfaceProxy ? (InterfaceProxy) value : new InterfaceProxy(key, clazz, value);
        synchronized (this) {
            keyToIpcImplMap.put(key, interfaceProxy);
            if (presenter != null) {
                IMethodChannelBinder binder = (IMethodChannelBinder) presenter.queryBinderByCode(
                        InterfaceIPCConst.BINDER_CODE_METHOD_CALL);
                interfaceProxy.setMethodChannelBinder(binder);
            }
        }
    }

    @Nullable
    public InterfaceProxy<?> fetchCallObject(long key) {
        return keyToIpcImplMap.get(key);
    }

    public boolean removeIpcImpl(long key) {
        synchronized (this) {
            return keyToIpcImplMap.remove(key) != null;
        }
    }

    public void bindBinderPool(ProcessServicePresenter presenter) {
        this.presenter = presenter;
        if (presenter == null) {
            return;
        }
        IMethodChannelBinder binder = presenter.queryBinderByCode(InterfaceIPCConst.BINDER_CODE_METHOD_CALL);
        if (binder == null) {
            return;
        }
        synchronized (this) {
            Set<Entry<Long, InterfaceProxy<?>>> entries = keyToIpcImplMap.entrySet();
            for (Entry<Long, InterfaceProxy<?>> next : entries) {
                next.getValue().setMethodChannelBinder(binder);
            }
        }
    }


}
