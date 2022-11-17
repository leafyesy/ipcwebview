package com.ysydhc.interfaceipc.proxy;

import android.os.RemoteException;
import android.text.TextUtils;
import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.interfaceipc.IMethodChannelBinder;
import com.ysydhc.interfaceipc.InterfaceIPCConst;
import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.model.MethodCallModel;
import com.ysydhc.interfaceipc.model.MethodResultModel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class MethodChannelBinderImpl extends IMethodChannelBinder.Stub {

    private static final String TAG = "MethodChannelBinderImpl";

    @Override
    public MethodResultModel invokeMethod(MethodCallModel model) throws RemoteException {
        Object o = InterfaceIpcHub.getInstance().fetchCallObject(model.getKey());
        return innerCallMethod(model, o);
    }

    private MethodResultModel innerCallMethod(MethodCallModel methodCall, Object obj) {
        if (obj == null) {
            return null;
        }
        LogUtil.i(TAG, "innerCallMethod" + methodCall.toString());
        Class<?> clazz = obj.getClass();
        HashMap arguments = methodCall.getArguments();
        Object[] objArr = (Object[]) arguments.get(InterfaceIPCConst.IPC_KEY_ARGS);
        Class<?>[] clazzArr = null;
        if (objArr != null) {
            clazzArr = new Class[objArr.length];
            for (int i = 0; i < objArr.length; i++) {
                clazzArr[i] = objArr[i].getClass();
            }
        }
        Method method = findMethod(clazz, methodCall.getMethodName(), clazzArr);
        if (method != null) {
            try {
                method.setAccessible(true);
                Object invoke = method.invoke(obj, objArr);
                if (invoke == null) {
                    return MethodResultModel.VOID_RESULT;
                } else {
                    return new MethodResultModel(invoke);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogUtil.exception(TAG, "", e);
            }
        }
        return MethodResultModel.VOID_RESULT;
    }

    // todo 优化寻找时间
    private static Method findMethod(Class<?> target, String methodName, Class<?>[] classes) {
        Method[] declaredMethods = target.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (TextUtils.equals(method.getName(), methodName)) {
                if (classes == null) {
                    return method;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != classes.length) {
                    continue;
                }
                // 对比每个参数的类型
                boolean isAllEqual = true;
                for (int j = 0; j < parameterTypes.length; j++) {
                    if (parameterTypes[j] != classes[j]) {
                        isAllEqual = false;
                        break;
                    }
                }
                if (isAllEqual) {
                    return method;
                }
            }
        }
        return null;
    }
}
