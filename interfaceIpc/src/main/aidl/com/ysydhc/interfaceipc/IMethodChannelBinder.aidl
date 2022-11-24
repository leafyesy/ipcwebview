// IMethodChannelBinder.aidl
package com.ysydhc.interfaceipc;

import com.ysydhc.interfaceipc.model.MethodResultModel;
import com.ysydhc.interfaceipc.model.MethodCallModel;

interface IMethodChannelBinder {

    MethodResultModel invokeMethod(in MethodCallModel model);

    oneway void invokeMethodAsync(in MethodCallModel model);

}