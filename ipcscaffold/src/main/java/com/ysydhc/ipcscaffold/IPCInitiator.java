package com.ysydhc.ipcscaffold;

import android.content.Context;
import android.content.Intent;

public class IPCInitiator {

    public static void startRemote(Context context) {
        RemoteServicePresenter.getInstance().holdContext(context);
        RemoteServicePresenter.getInstance().initConnectService();
    }

    public static void startMain(Context context) {
        MainServicePresenter.getInstance().holdContext(context);
        MainServicePresenter.getInstance().initConnectService();
    }

    public static void startZygoteActivity(Context context) {
        Intent intent = new Intent(context, ZygoteActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
