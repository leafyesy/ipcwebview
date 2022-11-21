package com.ysydhc.interfaceipc.proxy;

import android.os.RemoteException;

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
            return null;
        }
        if (model.getIsCallbackCalled()) {
            return wrapper.responseCallbackCall(model);
        }
        return wrapper.innerCallMethod(model);
    }

}
