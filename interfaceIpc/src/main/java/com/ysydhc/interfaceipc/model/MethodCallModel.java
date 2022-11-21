package com.ysydhc.interfaceipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class MethodCallModel implements Parcelable {

    public static final byte TYPE_NORMAL_METHOD_CALL = 0; // 非回调的正常方法调用
    public static final byte TYPE_ADD_OR_SET_CALLBACK_METHOD = 1; // 添加或者设置回调方法
    public static final byte TYPE_CALLBACK_METHOD_IS_CALLED = 2; // 回调方法被触发

    // invoke object flag
    private long key;

    private String methodName;

    private long invokeTimestamp;

    private Class<?> clazz;

    private HashMap<String, Object> arguments;

    private byte methodType = (byte) 0; // 是否设置回调的方法

    private byte needCallback;

    public MethodCallModel(long key, Class<?> clazz, String methodName, long invokeTimestamp,
            HashMap<String, Object> arguments, byte methodType, byte needCallback) {
        this.key = key;
        this.methodName = methodName;
        this.invokeTimestamp = invokeTimestamp;
        this.arguments = arguments;
        this.needCallback = needCallback;
        this.methodType = methodType;
        this.clazz = clazz;
    }

    public long getKey() {
        return key;
    }

    public boolean getIsSetCallback() {
        return methodType == TYPE_ADD_OR_SET_CALLBACK_METHOD;
    }

    public boolean getIsCallbackCalled() {
        return methodType == TYPE_CALLBACK_METHOD_IS_CALLED;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getInvokeTimestamp() {
        return invokeTimestamp;
    }

    public HashMap<String, Object> getArguments() {
        return arguments;
    }

    public byte getNeedCallback() {
        return needCallback;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    protected MethodCallModel(Parcel in) {
        key = in.readLong();
        methodName = in.readString();
        invokeTimestamp = in.readLong();
        needCallback = in.readByte();
        arguments = in.readHashMap(HashMap.class.getClassLoader());
        methodType = in.readByte();
        clazz = (Class<?>) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(key);
        dest.writeString(methodName);
        dest.writeLong(invokeTimestamp);
        dest.writeByte(needCallback);
        dest.writeMap(arguments);
        dest.writeByte(methodType);
        dest.writeSerializable(clazz);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MethodCallModel> CREATOR = new Creator<MethodCallModel>() {
        @Override
        public MethodCallModel createFromParcel(Parcel in) {
            return new MethodCallModel(in);
        }

        @Override
        public MethodCallModel[] newArray(int size) {
            return new MethodCallModel[size];
        }
    };
}
