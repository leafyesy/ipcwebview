package com.ysydhc.interfaceipc.proxy;

import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.interfaceipc.IMethodChannelBinder;
import com.ysydhc.interfaceipc.InterfaceIPCConst;
import com.ysydhc.interfaceipc.annotation.IpcMethodFlag;
import com.ysydhc.interfaceipc.model.MethodCallModel;
import com.ysydhc.interfaceipc.model.MethodResultModel;
import com.ysydhc.interfaceipc.utils.MethodUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

public class InterfaceProxy<T> {

    private static final String TAG = "InterfaceProxy";

    private T outProxy; // 外部代理
    private final Class<T> clazz;
    private long key = -1; // 标记位,用于确定调用跨进程的另一方的对象
    private IMethodChannelBinder binder;
    // 记录方法调用的时间戳
    private final CopyOnWriteArrayList<Long> methodCallTimestampList = new CopyOnWriteArrayList<Long>();
    private final ProxyCallbackManager proxyCallbackManager = new ProxyCallbackManager();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ConcurrentHashMap<Integer, MethodResultModel> keyToResultModelMap = new ConcurrentHashMap<>();

    public InterfaceProxy(long key, Class<T> clazz) {
        this.key = key;
        this.clazz = clazz;
    }

    public InterfaceProxy(long key, T outProxy) {
        this.key = key;
        this.clazz = (Class<T>) outProxy.getClass();
        this.outProxy = outProxy;
    }

    public T getOutProxy() {
        return outProxy;
    }

    public void setMethodChannelBinder(IMethodChannelBinder binder) {
        this.binder = binder;
    }

    public T createProxy()
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Class<?> proxyClazz = Proxy.getProxyClass(clazz.getClassLoader(), clazz);
        Constructor<?> proxyConstructor = proxyClazz.getConstructor(InvocationHandler.class);
        Object instance = proxyConstructor.newInstance(new LocalProxyInvocationHandler());
        return (T) instance;
    }

    @Nullable
    private Object markCallback(Method method, Object[] args, long callTimestamp, IpcMethodFlag ipcMethodFlag,
            boolean isClearOld)
            throws ClassNotFoundException, RemoteException {
        // 标记设置了哪些回调有监听
        Class<?> aClass = MethodUtils.getClassByObj(args[0]);
        HashMap<String, Object> argsHashMap = new HashMap<String, Object>();
        argsHashMap.put(InterfaceIPCConst.IPC_KEY_CALLBACK_CLASS_NAME, aClass.getName());
        proxyCallbackManager.addCallback(aClass, args[0], isClearOld);
        MethodCallModel callModel = new MethodCallModel(key, clazz, method.getName(), callTimestamp,
                argsHashMap, (byte) MethodCallModel.TYPE_ADD_OR_SET_CALLBACK_METHOD, (byte) 1);
        callModel.setMainThread((byte) ipcMethodFlag.thread());
        MethodResultModel methodResultModel = binder.invokeMethod(callModel);
        if (methodResultModel == null
                || methodResultModel.getResult() == MethodResultModel.VOID_RESULT) {
            return MethodUtils.getResultByReturnType(method.getReturnType());
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
                classes = MethodUtils.getClasses(objArr);
            }
            Method method = MethodUtils.findMethod(model.getClazz(), model.getMethodName(), classes);
            if (method != null) {
                method.setAccessible(true);
                for (int i = 0; i < callbackList.size(); i++) {
                    Object o = callbackList.get(i);
                    try {
                        Object invoke = method.invoke(o, objArr);
                        if (methodResultModel == MethodResultModel.VOID_RESULT) {
                            methodResultModel = MethodUtils.createResultByInvoke(invoke);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        LogUtil.exception(TAG, "", e);
                    }
                }
            }
        }
        return methodResultModel;
    }


    public MethodResultModel responseRemoteMethodCall(MethodCallModel methodCall) {
        if (outProxy == null) {
            return null;
        }
        LogUtil.i(TAG, "responseRemoteMethodCall" + methodCall.toString());
        HashMap<String, Object> arguments = methodCall.getArguments();
        if (methodCall.getIsSetCallback()) {
            String clazzName = (String) arguments.get(InterfaceIPCConst.IPC_KEY_CALLBACK_CLASS_NAME);
            if (TextUtils.isEmpty(clazzName)) {
                LogUtil.e(TAG, "responseRemoteMethodCall clazzName is empty!");
                return null;
            }
            Class<?> clazz = null;
            try {
                clazz = Class.forName(clazzName);
                Constructor<?> constructor = clazz.getConstructor();
                constructor.newInstance();
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                LogUtil.exception(TAG, "", e);
            }
            if (clazz == null) {
                LogUtil.e(TAG, "responseRemoteMethodCall clazz is null!");
                return null;
            }
            Class<?>[] clazzArr = new Class<?>[]{clazz};
            Method method = MethodUtils.findMethod(outProxy.getClass(), methodCall.getMethodName(), clazzArr);
            if (method == null) {
                LogUtil.e(TAG, "responseRemoteMethodCall method is null!");
                return null;
            }
            method.setAccessible(true);
            Class<?> finalClazz = clazz;
            Object callbackProxyObj = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                    new CallbackInvocationHandler(finalClazz));
            return invokeMethodAndGetResultModel(methodCall, method, callbackProxyObj);
        } else {
            Object[] objArr = (Object[]) arguments.get(InterfaceIPCConst.IPC_KEY_ARGS);
            Class<?>[] clazzArr = MethodUtils.getClasses(objArr);
            Method method = MethodUtils.findMethod(outProxy.getClass(), methodCall.getMethodName(), clazzArr);
            if (method == null) {
                LogUtil.e(TAG, "responseRemoteMethodCall method is null!");
                return null;
            }
            return invokeMethodAndGetResultModel(methodCall, method, objArr);
        }
    }

    @Nullable
    private MethodResultModel invokeMethodAndGetResultModel(MethodCallModel methodCall, Method method,
            Object... objVar) {
        try {
            method.setAccessible(true);
            if (methodCall.getMainThread() == IpcMethodFlag.THREAD_MAIN) {
                CountDownLatch latch = new CountDownLatch(1);
                mainHandler.post(() -> {
                    try {
                        Object invoke = method.invoke(outProxy, objVar);
                        MethodResultModel resultByInvoke = MethodUtils.createResultByInvoke(invoke);
                        keyToResultModelMap.put(methodCall.hashCode(), resultByInvoke);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                    latch.countDown();
                });
                latch.await();
                MethodResultModel resultModel = keyToResultModelMap.remove(methodCall.hashCode());
                return resultModel != null ? resultModel : MethodUtils.createResultByInvoke(null);
            }
            Object invoke = method.invoke(outProxy, objVar);
            return MethodUtils.createResultByInvoke(invoke);
        } catch (IllegalAccessException | InvocationTargetException | InterruptedException e) {
            LogUtil.exception(TAG, "", e);
        }
        return null;
    }

    /**
     * 回调接口的跨进程通讯响应
     */
    private class CallbackInvocationHandler implements InvocationHandler {

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
                        callTimestamp, argsHashMap, MethodCallModel.TYPE_CALLBACK_METHOD_IS_CALLED, (byte) 1);
                MethodResultModel methodResultModel = binder.invokeMethod(callModel);
                if (methodResultModel.getResult() == MethodResultModel.VOID_RESULT) {
                    return MethodResultModel.VOID_RESULT;
                } else {
                    return methodResultModel.getResult();
                }
            }
            Class<?> returnType = method.getReturnType();
            return MethodUtils.getResultByReturnType(returnType);
        }
    }

    /**
     * 本地接口代理
     * 自动转发给另一个进程
     */
    private class LocalProxyInvocationHandler implements InvocationHandler {

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
                        callModel.setMainThread((byte) ipcMethodFlag.thread());
                        MethodResultModel methodResultModel = binder.invokeMethod(callModel);
                        if (methodResultModel == null || methodResultModel.getResult() == MethodResultModel.VOID_RESULT
                                || methodResultModel.getResult() == null) {
                            return MethodUtils.getResultByReturnType(method.getReturnType());
                        } else {
                            return methodResultModel.getResult();
                        }
                    }
                    case IpcMethodFlag.KEY_LOCAL_CALLBACK_ADD: {
                        if (args.length != 1) {
                            return MethodUtils.getResultByReturnType(method.getReturnType());
                        }
                        return markCallback(method, args, callTimestamp, ipcMethodFlag, false);
                    }
                    case IpcMethodFlag.KEY_LOCAL_CALLBACK_SET: {
                        if (args.length != 1) {
                            return MethodUtils.getResultByReturnType(method.getReturnType());
                        }
                        return markCallback(method, args, callTimestamp, ipcMethodFlag, true);
                    }
                }
            }
            return MethodUtils.getResultByReturnType(method.getReturnType());
        }
    }


}
