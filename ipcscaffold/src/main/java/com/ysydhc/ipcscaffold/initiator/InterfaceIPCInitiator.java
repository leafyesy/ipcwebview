package com.ysydhc.ipcscaffold.initiator;

import android.os.IBinder;
import com.ysydhc.aninterface.test.ITest;
import com.ysydhc.interfaceipc.InterfaceIPCConst;
import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.connect.IConnectObjectCreator;
import com.ysydhc.interfaceipc.model.ConnectCell;
import com.ysydhc.interfaceipc.proxy.MethodChannelBinderImpl;
import com.ysydhc.ipcscaffold.IBinderProvider;
import com.ysydhc.ipcscaffold.ProcessServicePresenter.BinderManager;

public class InterfaceIPCInitiator implements IIPCInitiatorTask {

    @Override
    public void init(BinderManager manager) {
        // 绑定BinderCode -> Binder的创建
        manager.addBinderProvider(new IBinderProvider() {

            @Override
            public int binderCode() {
                return InterfaceIPCConst.BINDER_CODE_OBJ_CONNECT;
            }

            @Override
            public IBinder binderProvider() {
                return InterfaceIpcHub.getInstance().getObjectConnectBinder();
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
                    binder = new MethodChannelBinderImpl();
                }
                return binder;
            }
        });
        // 绑定 Object key -> Object创建
        InterfaceIpcHub.getInstance().setConnectObjectCreateList(new IConnectObjectCreator() {
            @Override
            public Object create(ConnectCell cell) {
                if (cell != null && cell.getKey() == 100L) {
                    return new ITest() {

                        int count = 1000;

                        @Override
                        public int countPlus() {
                            return count++;
                        }
                    };
                }
                return null;
            }
        });
    }
}
