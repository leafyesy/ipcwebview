package com.ysydhc.interfaceipc;

import com.ysydhc.ipcscaffold.ProcessServicePresenter.BinderManager;
import com.ysydhc.ipcscaffold.ProcessServicePresenter.ServiceConnectionCallback;
import com.ysydhc.ipcscaffold.initiator.IIPCInitiatorTask;

public class RemoteCallbackSupportInitiator implements IIPCInitiatorTask {

    @Override
    public void init(BinderManager manager) {
        manager.presenter.addServiceConnectionCallback(new ServiceConnectionCallback() {
            @Override
            public void onServiceConnected() {
                InterfaceIpcHub.getInstance().bindBinderPool(manager.presenter);
            }

            @Override
            public void onServiceDisconnected() {
                InterfaceIpcHub.getInstance().bindBinderPool(null);
            }
        });
    }
}
