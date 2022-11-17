package com.ysydhc.ipcscaffold;

import android.os.IBinder;

public interface IBinderProvider {

    int binderCode();

    IBinder binderProvider();

}
