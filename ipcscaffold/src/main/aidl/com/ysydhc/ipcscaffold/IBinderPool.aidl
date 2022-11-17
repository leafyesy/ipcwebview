// IBinderPool.aidl
package com.ysydhc.ipcscaffold;

// Declare any non-default types here with import statements

interface IBinderPool {
    IBinder queryBinder(int binderCode);
}