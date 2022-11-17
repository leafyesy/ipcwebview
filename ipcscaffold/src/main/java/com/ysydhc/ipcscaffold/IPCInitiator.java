package com.ysydhc.ipcscaffold;

import android.content.Context;

public class IPCInitiator {

    public static void start(Context context) {
        RemoteServicePresenter.getInstance().initConnectService();
    }



}
