package com.ysydhc.interfaceipc.proxy;

import com.ysydhc.interfaceipc.IMethodChannelBinder;
import com.ysydhc.interfaceipc.InterfaceIPCConst;
import com.ysydhc.interfaceipc.annotation.IpcMethodFlag;
import com.ysydhc.interfaceipc.model.MethodCallModel;
import com.ysydhc.interfaceipc.model.MethodResultModel;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InterfaceProxy<T> {

    private T outProxy; // 外部代理
    private Class<T> clazz;
    private T innerProxy; // 内部代理
    private long key = -1; // 标记位,用于确定调用跨进程的另一方的对象
    private IMethodChannelBinder binder;
    // 记录方法调用的时间戳
    private final CopyOnWriteArrayList<Long> methodCallTimestampList = new CopyOnWriteArrayList<Long>();

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
                            MethodCallModel callModel = new MethodCallModel(
                                    key, method.getName(), callTimestamp, argsHashMap, (byte) 1);
                            MethodResultModel methodResultModel = binder.invokeMethod(callModel);
                            if (methodResultModel.getResult() == MethodResultModel.VOID_RESULT) {
                                return null;
                            } else {
                                return methodResultModel.getResult();
                            }
                        }
                    }
                }
                return null;
            }
        });
        innerProxy = (T) instance;
        return (T) instance;
    }


}
