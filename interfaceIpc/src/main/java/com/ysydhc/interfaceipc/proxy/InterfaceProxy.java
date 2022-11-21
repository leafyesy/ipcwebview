package com.ysydhc.interfaceipc.proxy;

import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.interfaceipc.IMethodChannelBinder;
import com.ysydhc.interfaceipc.InterfaceIPCConst;
import com.ysydhc.interfaceipc.annotation.IpcMethodFlag;
import com.ysydhc.interfaceipc.model.MethodCallModel;
import com.ysydhc.interfaceipc.model.MethodResultModel;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import kotlin.Metadata;

public class InterfaceProxy<T> {

    private static final String TAG = "InterfaceProxy";

    private T outProxy; // 外部代理
    private final Class<T> clazz;
    private long key = -1; // 标记位,用于确定调用跨进程的另一方的对象
    private IMethodChannelBinder binder;
    // 记录方法调用的时间戳
    private final CopyOnWriteArrayList<Long> methodCallTimestampList = new CopyOnWriteArrayList<Long>();
    private final ProxyCallbackManager proxyCallbackManager = new ProxyCallbackManager();

    public InterfaceProxy(long key, Class<T> clazz) {
        this.key = key;
        this.clazz = clazz;
    }

    public InterfaceProxy(long key, T outProxy) {
        this.key = key;
        this.clazz = (Class<T>) outProxy.getClass();
        this.outProxy = outProxy;
    }

    public void setMethodChannelBinder(IMethodChannelBinder binder) {
        this.binder = binder;
    }

    public T createProxy()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class<?> proxyClazz = Proxy.getProxyClass(clazz.getClassLoader(), clazz);
        Constructor<?> proxyConstructor = proxyClazz.getConstructor(InvocationHandler.class);
        Object instance = proxyConstructor.newInstance(new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (key == -1) {
                    throw new IllegalArgumentException("key is a valid proxy key");
                }
                if (binder == null && outProxy == null) {
                    throw new IllegalStateException("binder and out proxy is both are all null");
                }
                if (outProxy != null) {
                    return method.invoke(outProxy, args);
                }
                long callTimestamp = System.currentTimeMillis();
                methodCallTimestampList.add(callTimestamp);
                if (binder == null) {
                    return method.invoke(outProxy, args);
                }
                IpcMethodFlag ipcMethodFlag = method.getAnnotation(IpcMethodFlag.class);
                if (ipcMethodFlag != null) { // 标记为需要跨进程调用的方法
                    switch (ipcMethodFlag.value()) {
                        case IpcMethodFlag.KEY_IPC_METHOD: {
                            HashMap<String, Object> argsHashMap = new HashMap<String, Object>();
                            argsHashMap.put(InterfaceIPCConst.IPC_KEY_ARGS, args);
                            MethodCallModel callModel = new MethodCallModel(key, clazz, method.getName(), callTimestamp,
                                    argsHashMap, MethodCallModel.TYPE_NORMAL_METHOD_CALL, (byte) 1);
                            MethodResultModel methodResultModel = binder.invokeMethod(callModel);
                            if (methodResultModel.getResult() == MethodResultModel.VOID_RESULT
                                    || methodResultModel.getResult() == null) {
                                return getResultByReturnType(method.getReturnType());
                            } else {
                                return methodResultModel.getResult();
                            }
                        }
                        case IpcMethodFlag.KEY_LOCAL_CALLBACK_ADD: {
                            if (args.length != 1) {
                                return getResultByReturnType(method.getReturnType());
                            }
                            return markCallback(method, args, callTimestamp, false);
                        }
                        case IpcMethodFlag.KEY_LOCAL_CALLBACK_SET: {
                            if (args.length != 1) {
                                return getResultByReturnType(method.getReturnType());
                            }
                            return markCallback(method, args, callTimestamp, true);
                        }
                    }
                }
                return null;
            }
        });
        return (T) instance;
    }

    @Nullable
    private Object markCallback(Method method, Object[] args, long callTimestamp, boolean isClearOld)
            throws ClassNotFoundException, RemoteException {
        // 标记设置了哪些回调有监听

        Metadata annotation = args[0].getClass().getAnnotation(Metadata.class);
        Class<?> aClass = null;
        if (annotation != null) {
            String clazzName = annotation.d2()[1];
            String realClazzName = clazzName.substring(1, clazzName.length() - 1).replace("/", ".");
            aClass = Class.forName(realClazzName);
        }
        if (aClass == null) {
            aClass = args[0].getClass();
        }
        HashMap<String, Object> argsHashMap = new HashMap<String, Object>();
        argsHashMap.put(InterfaceIPCConst.IPC_KEY_CALLBACK_CLASS_NAME, aClass.getName());
        proxyCallbackManager.addCallback(aClass, args[0], isClearOld);
        MethodCallModel callModel = new MethodCallModel(key, clazz, method.getName(), callTimestamp,
                argsHashMap, (byte) MethodCallModel.TYPE_ADD_OR_SET_CALLBACK_METHOD, (byte) 1);
        MethodResultModel methodResultModel = binder.invokeMethod(callModel);
        if (methodResultModel == null
                || methodResultModel.getResult() == MethodResultModel.VOID_RESULT) {
            return getResultByReturnType(method.getReturnType());
        } else {
            return methodResultModel.getResult();
        }
    }

    public MethodResultModel responseCallbackCall(MethodCallModel model) {
        if (!model.getIsCallbackCalled()) {
            return MethodResultModel.VOID_RESULT;
        }
        MethodResultModel methodResultModel = MethodResultModel.VOID_RESULT;
        List<?> callbackList = proxyCallbackManager.getCallbackList(model.getClazz());
        if (callbackList != null && !callbackList.isEmpty()) {
            HashMap<String, Object> arguments = model.getArguments();
            Object[] objArr = null;
            Class<?>[] classes = null;
            if (arguments != null) {
                objArr = (Object[]) arguments.get(InterfaceIPCConst.IPC_KEY_ARGS);
                classes = getClasses(objArr);
            }
            Method method = findMethod(model.getClazz(), model.getMethodName(), classes);
            method.setAccessible(true);
            for (int i = 0; i < callbackList.size(); i++) {
                Object o = callbackList.get(i);
                try {
                    Object invoke = method.invoke(o, objArr);
                    if (methodResultModel == MethodResultModel.VOID_RESULT) {
                        methodResultModel = createResultByInvoke(invoke);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LogUtil.exception(TAG, "", e);
                }
            }
        }
        return methodResultModel;
    }

    class CallbackInvocationHandler implements InvocationHandler {

        private final Class<?> finalClazz;

        public CallbackInvocationHandler(Class<?> finalClazz) {
            this.finalClazz = finalClazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 把回调转发给另一个进程
            if (binder != null) {
                long callTimestamp = System.currentTimeMillis();
                HashMap<String, Object> argsHashMap = new HashMap<String, Object>();
                argsHashMap.put(InterfaceIPCConst.IPC_KEY_ARGS, args);
                MethodCallModel callModel = new MethodCallModel(key, finalClazz, method.getName(),
                        callTimestamp,
                        argsHashMap, MethodCallModel.TYPE_CALLBACK_METHOD_IS_CALLED, (byte) 1);
                MethodResultModel methodResultModel = binder.invokeMethod(callModel);
                if (methodResultModel.getResult() == MethodResultModel.VOID_RESULT) {
                    return MethodResultModel.VOID_RESULT;
                } else {
                    return methodResultModel.getResult();
                }
            }
            Class<?> returnType = method.getReturnType();
            return getResultByReturnType(returnType);
        }
    }

    public MethodResultModel innerCallMethod(MethodCallModel methodCall) {
        if (outProxy == null) {
            return null;
        }
        LogUtil.i(TAG, "innerCallMethod" + methodCall.toString());
        HashMap arguments = methodCall.getArguments();
        if (methodCall.getIsSetCallback()) {
            String clazzName = (String) arguments.get(InterfaceIPCConst.IPC_KEY_CALLBACK_CLASS_NAME);
            Class<?> clazz = null;
            try {
                clazz = Class.forName(clazzName);
                Constructor<?> constructor = clazz.getConstructor();
                constructor.newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                LogUtil.exception(TAG, "", e);
            }
            if (clazz == null) {
                return null;
            }
            Class<?>[] clazzArr = new Class<?>[]{clazz};
            Method method = findMethod(outProxy.getClass(), methodCall.getMethodName(), clazzArr);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            try {
                Class<?> finalClazz = clazz;
                Object callbackProxyObj = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                        new CallbackInvocationHandler(finalClazz));
                Object invoke = method.invoke(outProxy, callbackProxyObj);
                return createResultByInvoke(invoke);
            } catch (IllegalAccessException | InvocationTargetException e) {
                LogUtil.exception(TAG, "", e);
            }
        } else {
            Object[] objArr = (Object[]) arguments.get(InterfaceIPCConst.IPC_KEY_ARGS);
            Class<?>[] clazzArr = getClasses(objArr);
            Method method = findMethod(outProxy.getClass(), methodCall.getMethodName(), clazzArr);
            if (method != null) {
                try {
                    method.setAccessible(true);
                    Object invoke = method.invoke(outProxy, objArr);
                    return createResultByInvoke(invoke);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LogUtil.exception(TAG, "", e);
                }
            }
        }
        return MethodResultModel.VOID_RESULT;
    }

    @NonNull
    private MethodResultModel createResultByInvoke(Object invoke) {
        if (invoke instanceof Parcelable) {
            return new MethodResultModel((Parcelable) invoke);
        } else if (invoke instanceof Serializable) {
            return new MethodResultModel((Serializable) invoke);
        } else {
            return MethodResultModel.VOID_RESULT;
        }
    }

    @Nullable
    private Class<?>[] getClasses(Object[] objArr) {
        Class<?>[] clazzArr = null;
        if (objArr != null) {
            clazzArr = new Class[objArr.length];
            for (int i = 0; i < objArr.length; i++) {
                clazzArr[i] = objArr[i].getClass();
            }
        }
        return clazzArr;
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
                    if (parameterTypes[j] != classes[j] && !dataTypeEquals(parameterTypes[j], classes[j])) {
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

    private static boolean dataTypeEquals(Class<?> c1, Class<?> c2) {
        if (c1 == int.class && c2 == Integer.class || c1 == Integer.class && c2 == int.class) {
            return true;
        }
        if (c1 == byte.class && c2 == Byte.class || c1 == Byte.class && c2 == byte.class) {
            return true;
        }
        if (c1 == short.class && c2 == Short.class || c1 == Short.class && c2 == short.class) {
            return true;
        }
        if (c1 == float.class && c2 == Float.class || c1 == Float.class && c2 == float.class) {
            return true;
        }
        if (c1 == long.class && c2 == Long.class || c1 == Long.class && c2 == long.class) {
            return true;
        }
        if (c1 == boolean.class && c2 == Boolean.class || c1 == Boolean.class && c2 == boolean.class) {
            return true;
        }
        return false;
    }

    @Nullable
    private Object getResultByReturnType(Class<?> returnType) {
        if (returnType == byte.class || returnType == Byte.class) {
            return (byte) 0;
        }
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        if (returnType == short.class || returnType == Short.class) {
            return 0;
        }
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == float.class || returnType == Float.class) {
            return 0F;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0;
        }
        return null;
    }


}
