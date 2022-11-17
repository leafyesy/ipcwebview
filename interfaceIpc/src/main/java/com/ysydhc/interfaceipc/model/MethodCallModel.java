package com.ysydhc.interfaceipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class MethodCallModel implements Parcelable {

    // invoke object flag
    private long key;

    private String methodName;

    private long invokeTimestamp;

    private HashMap<String, Object> arguments;

    private byte needCallback;

    public MethodCallModel(long key, String methodName, long invokeTimestamp,
            HashMap<String, Object> arguments, byte needCallback) {
        this.key = key;
        this.methodName = methodName;
        this.invokeTimestamp = invokeTimestamp;
        this.arguments = arguments;
        this.needCallback = needCallback;
    }

    public long getKey() {
        return key;
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

    protected MethodCallModel(Parcel in) {
        key = in.readLong();
        methodName = in.readString();
        invokeTimestamp = in.readLong();
        needCallback = in.readByte();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(key);
        dest.writeString(methodName);
        dest.writeLong(invokeTimestamp);
        dest.writeByte(needCallback);
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
