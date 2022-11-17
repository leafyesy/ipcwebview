package com.ysydhc.interfaceipc;

import android.os.IBinder;

import androidx.annotation.Nullable;

import com.ysydhc.interfaceipc.connect.IConnectObjectCreator;
import com.ysydhc.interfaceipc.connect.ObjectConnectBinderImpl;
import com.ysydhc.interfaceipc.proxy.MethodChannelBinderImpl;
import com.ysydhc.ipcscaffold.IBinderProvider;
import com.ysydhc.ipcscaffold.RemoteServicePresenter;

import java.util.concurrent.ConcurrentHashMap;

public class InterfaceIPCInitiator {

    private InterfaceIPCInitiator() {
    }

    private static volatile InterfaceIPCInitiator sInstance;
    private final ObjectConnectBinderImpl objectConnectBinder = new ObjectConnectBinderImpl();
    private final ConcurrentHashMap<Long, Object> keyToIpcImplMap = new ConcurrentHashMap<Long, Object>();

    public static InterfaceIPCInitiator getInstance() {
        if (sInstance == null) {
            synchronized (InterfaceIPCInitiator.class) {
                if (sInstance == null) {
                    sInstance = new InterfaceIPCInitiator();
                }
            }
        }
        return sInstance;
    }

    public void start() {
        RemoteServicePresenter.getInstance().addBinderProvider(new IBinderProvider() {

            @Override
            public int binderCode() {
                return InterfaceIPCConst.BINDER_CODE_OBJ_CONNECT;
            }

            @Override
            public IBinder binderProvider() {
                return objectConnectBinder;
            }
        });

        RemoteServicePresenter.getInstance().addBinderProvider(new IBinderProvider() {

            private IBinder binder;

            @Override
            public int binderCode() {
                return InterfaceIPCConst.BINDER_CODE_METHOD_CALL;
            }

            @Override
            public IBinder binderProvider() {
                if (binder == null) {
                    binder = new MethodChannelBinderImpl();
                }
                return binder;
            }
        });
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
