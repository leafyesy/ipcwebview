package com.ysydhc.ipcscaffold;

import android.content.Context;

public class IPCInitiator {

    public static void startRemote(Context context) {
        RemoteServicePresenter.getInstance().initConnectService();
    }


}
