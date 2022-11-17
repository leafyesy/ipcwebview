package com.ysydhc.ipcscaffold;

import android.os.IBinder;
import android.os.RemoteException;

public interface IBinderProvider {

    int binderCode();

    IBinder binderProvider() throws RemoteException;

}
