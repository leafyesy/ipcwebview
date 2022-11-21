package com.ysydhc.ipcwebview.ipc;

import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.connect.IConnectObjectCreator;
import com.ysydhc.interfaceipc.model.ConnectCell;
import com.ysydhc.ipcscaffold.ProcessServicePresenter.BinderManager;
import com.ysydhc.ipcscaffold.initiator.IIPCInitiatorTask;

public class RemoteTestInitiator implements IIPCInitiatorTask {

    @Override
    public void init(BinderManager manager) {
        // 绑定 Object key -> Object创建
        InterfaceIpcHub.getInstance().setConnectObjectCreateList(new IConnectObjectCreator() {
            @Override
            public Object create(ConnectCell cell) {
                if (cell != null && cell.getKey() == 100L) {
                    return new RemoteTest();
                }
                return null;
            }
        });
    }
}
