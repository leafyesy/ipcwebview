package com.ysydhc.interfaceipc.connect;

import android.os.RemoteException;
import com.ysydhc.interfaceipc.IObjectConnect;
import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.model.ConnectCell;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObjectConnectBinderImpl extends IObjectConnect.Stub {

    private final CopyOnWriteArrayList<IConnectObjectCreator> connectObjectCreateList = new CopyOnWriteArrayList<>();

    public void setConnectObjectCreateList(IConnectObjectCreator creator) {
        connectObjectCreateList.add(creator);
    }

    @Override
    public boolean connect(ConnectCell cell) throws RemoteException {
        for (IConnectObjectCreator creator : connectObjectCreateList) {
            Object result = creator.create(cell);
            if (result != null) {
                InterfaceIpcHub.getInstance().putIpcImpl(cell.getKey(), cell.getClazz(), result);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean dispose(long key) throws RemoteException {
        return InterfaceIpcHub.getInstance().removeIpcImpl(key);
    }
}
