package com.ysydhc.interfaceipc;

import androidx.annotation.Nullable;

import com.ysydhc.interfaceipc.connect.IConnectObjectCreator;
import com.ysydhc.interfaceipc.connect.ObjectConnectBinderImpl;
import com.ysydhc.interfaceipc.proxy.InterfaceProxy;

import java.util.concurrent.ConcurrentHashMap;

public class InterfaceIpcHub {

    private static final String TAG = "InterfaceIpcHub";

    private static InterfaceIpcHub instance;

    private InterfaceIpcHub() {
    }

    public static InterfaceIpcHub getInstance() {
        if (instance == null) {
            synchronized (InterfaceIpcHub.class) {
                if (instance == null) {
                    instance = new InterfaceIpcHub();
                }
            }
        }
        return instance;
    }

    private final ObjectConnectBinderImpl objectConnectBinder = new ObjectConnectBinderImpl();
    private final ConcurrentHashMap<Long, InterfaceProxy<?>> keyToIpcImplMap = new ConcurrentHashMap<Long, InterfaceProxy<?>>();

    public ObjectConnectBinderImpl getObjectConnectBinder() {
        return objectConnectBinder;
    }

    public void setConnectObjectCreateList(IConnectObjectCreator creator) {
        objectConnectBinder.setConnectObjectCreateList(creator);
    }

    public void putIpcImpl(long key, Object value) {
        // 设置代理
        keyToIpcImplMap.put(key, new InterfaceProxy(key, value));
    }

    @Nullable
    public InterfaceProxy fetchCallObject(long key) {
        return keyToIpcImplMap.get(key);
    }


}
