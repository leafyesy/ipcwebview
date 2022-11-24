package com.ysydhc.interfaceipc;

import android.os.IBinder;
import com.ysydhc.interfaceipc.proxy.MethodChannelBinderImpl;
import com.ysydhc.ipcscaffold.BinderCode2Class;
import com.ysydhc.ipcscaffold.IBinderProvider;
import com.ysydhc.ipcscaffold.ProcessServicePresenter.BinderManager;
import com.ysydhc.ipcscaffold.initiator.IIPCInitiatorTask;

public class InterfaceIPCInitiator implements IIPCInitiatorTask {

    @Override
    public void init(BinderManager manager) {
        BinderCode2Class.getInstance()
                .put(InterfaceIPCConst.BINDER_CODE_OBJ_CONNECT, InterfaceIPCConst.BINDER_CODE_OBJ_CONNECT_PACKAGE);
        BinderCode2Class.getInstance()
                .put(InterfaceIPCConst.BINDER_CODE_METHOD_CALL, InterfaceIPCConst.BINDER_CODE_METHOD_CALL_PACKAGE);
        // 绑定BinderCode -> Binder的创建
        manager.addBinderProvider(new IBinderProvider() {

            @Override
            public int binderCode() {
                return InterfaceIPCConst.BINDER_CODE_OBJ_CONNECT;
            }

            @Override
            public IBinder binderProvider() {
                return (IBinder)InterfaceIpcHub.getInstance().getObjectConnectBinder();
            }
        });

        manager.addBinderProvider(new IBinderProvider() {

            private IBinder binder;

            @Override
            public int binderCode() {
                return InterfaceIPCConst.BINDER_CODE_METHOD_CALL;
            }

            @Override
            public IBinder binderProvider() {
                if (binder == null) {
                    binder = (IBinder) new MethodChannelBinderImpl();
                }
                return binder;
            }
        });
    }
}
