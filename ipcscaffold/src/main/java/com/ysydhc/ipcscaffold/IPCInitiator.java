package com.ysydhc.ipcscaffold;

import android.content.Context;

public class IPCInitiator {

    public static void startRemote(Context context) {
        RemoteServicePresenter.getInstance().holdContext(context);
        RemoteServicePresenter.getInstance().initConnectService();
    }

    public static void startMain(Context context) {
        MainServicePresenter.getInstance().holdContext(context);
        MainServicePresenter.getInstance().initConnectService();
    }


}
