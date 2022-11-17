package com.ysydhc.interfaceipc.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IpcMethodFlag {

    int KEY_IPC_METHOD = 0;
    int KEY_LOCAL_LISTENER = 1;

    int value() default KEY_IPC_METHOD;

}
