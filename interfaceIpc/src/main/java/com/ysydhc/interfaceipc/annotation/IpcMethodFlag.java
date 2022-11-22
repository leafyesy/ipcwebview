package com.ysydhc.interfaceipc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IpcMethodFlag {

    int KEY_IPC_METHOD = 0;
    int KEY_LOCAL_CALLBACK_ADD = 1;
    int KEY_LOCAL_CALLBACK_SET = 2;

    int KEY_PARAM_CALLBACK = 100;

    int THREAD_MAIN = 0;
    int THREAD_SUB = 1;

    int value() default KEY_IPC_METHOD;

    int thread() default THREAD_MAIN;

}
