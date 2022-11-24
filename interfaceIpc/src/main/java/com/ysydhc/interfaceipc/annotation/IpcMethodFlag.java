package com.ysydhc.interfaceipc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IpcMethodFlag {

    int KEY_IPC_METHOD = 0;
    int KEY_LOCAL_CALLBACK_ADD = 1; // 设置回调-add的方式
    int KEY_LOCAL_CALLBACK_SET = 2; // 设置回调-set的方式
    int KEY_LOCAL_CALLBACK_REMOVE = 3; // 移除回调

    int THREAD_MAIN = 0; // 需要在UI线程进行调用的方法
    int THREAD_SUB = 1; // 可以在子线程进行调用的方法

    int SYNCHRONOUS = 0;
    int ASYNCHRONOUS = 1;

    int value() default KEY_IPC_METHOD;

    int thread() default THREAD_MAIN;

    int synchronous() default SYNCHRONOUS; // 方法是否同步,默认同步

}
