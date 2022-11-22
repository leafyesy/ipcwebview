package com.ysydhc.interfaceipc.proxy;

import android.os.RemoteException;

import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.interfaceipc.IMethodChannelBinder;
import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.model.MethodCallModel;
import com.ysydhc.interfaceipc.model.MethodResultModel;

public class MethodChannelBinderImpl extends IMethodChannelBinder.Stub {

    private static final String TAG = "MethodChannelBinderImpl";

    @Override
    public MethodResultModel invokeMethod(MethodCallModel model) throws RemoteException {
        InterfaceProxy<?> wrapper = InterfaceIpcHub.getInstance().fetchCallObject(model.getKey());
        if (wrapper == null) {
            LogUtil.i(TAG, "invokeMethod not found object! name=" + model.getMethodName()
                    + " target ObjClass=" + model.getClazz().getName());
            return null;
        }
        return wrapper.responseRemote(model);
    }

}
