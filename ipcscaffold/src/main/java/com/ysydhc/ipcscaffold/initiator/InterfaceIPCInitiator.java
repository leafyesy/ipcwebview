package com.ysydhc.ipcscaffold.initiator;

import android.os.IBinder;
import com.ysydhc.interfaceipc.InterfaceIPCConst;
import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.proxy.MethodChannelBinderImpl;
import com.ysydhc.ipcscaffold.IBinderProvider;
import com.ysydhc.ipcscaffold.RemoteServicePresenter;

public class InterfaceIPCInitiator implements IIPCInitiatorTask {

    @Override
    public void init() {
        RemoteServicePresenter.getInstance().addBinderProvider(new IBinderProvider() {

            @Override
            public int binderCode() {
                return InterfaceIPCConst.BINDER_CODE_OBJ_CONNECT;
            }

            @Override
            public IBinder binderProvider() {
                return InterfaceIpcHub.getInstance().getObjectConnectBinder();
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
}
