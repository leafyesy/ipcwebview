package com.ysydhc.interfaceipc.proxy;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProxyCallbackManager {

    /**
     * 回调列表存储
     */
    private final ConcurrentHashMap<Class<?>, List<Object>> callbackListCacheMap = new ConcurrentHashMap<Class<?>, List<Object>>();


    public void addCallback(Object callback, boolean isOnlyOne) {
        List<Object> list = callbackListCacheMap.get(callback.getClass());
        if (list == null) {
            list = new CopyOnWriteArrayList<>();
        }
        if (list.contains(callback)) {
            return;
        }
        if (isOnlyOne) {
            list.clear();
        }
        list.add(callback);
        callbackListCacheMap.put(callback.getClass(), list);
    }

    public void removeCallback(Object callback) {
        List<Object> list = callbackListCacheMap.get(callback.getClass());
        if (list != null) {
            list.remove(callback);
        }
    }

    public <T> List<T> getCallbackList(Class<T> clazz) {
        List<Object> list = callbackListCacheMap.get(clazz);
        return (List<T>) list;
    }

}
