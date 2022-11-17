package com.ysydhc.interfaceipc;

import androidx.annotation.Nullable;
import com.ysydhc.interfaceipc.connect.IConnectObjectCreator;
import com.ysydhc.interfaceipc.connect.ObjectConnectBinderImpl;
import java.util.concurrent.ConcurrentHashMap;

public class InterfaceIpcHub {

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
    private final ConcurrentHashMap<Long, Object> keyToIpcImplMap = new ConcurrentHashMap<Long, Object>();

    public ObjectConnectBinderImpl getObjectConnectBinder() {
        return objectConnectBinder;
    }

    public void setConnectObjectCreateList(IConnectObjectCreator creator) {
        objectConnectBinder.setConnectObjectCreateList(creator);
    }

    public void putIpcImpl(long key, Object value) {
        keyToIpcImplMap.put(key, value);
    }

    @Nullable
    public Object fetchCallObject(long key) {
        return keyToIpcImplMap.get(key);
    }


}
