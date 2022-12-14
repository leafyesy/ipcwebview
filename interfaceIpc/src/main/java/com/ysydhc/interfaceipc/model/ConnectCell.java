package com.ysydhc.interfaceipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

public class ConnectCell implements Parcelable {

    long key;

    Class<?> clazz;

    Parcelable ext;

    HashMap<String, Object> arguments;

    public ConnectCell(long key, Class<?> clazz, Parcelable ext,
            HashMap<String, Object> arguments) {
        this.key = key;
        this.clazz = clazz;
        this.ext = ext;
        this.arguments = arguments;
    }

    public long getKey() {
        return key;
    }

    public Parcelable getExt() {
        return ext;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public HashMap<String, Object> getArguments() {
        return arguments;
    }

    protected ConnectCell(Parcel in) {
        key = in.readLong();
        clazz = (Class<?>) in.readSerializable();
        ext = in.readParcelable(in.getClass().getClassLoader());
        arguments = in.readHashMap(in.getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(key);
        dest.writeSerializable(clazz);
        dest.writeParcelable(ext, flags);
        dest.writeMap(arguments);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ConnectCell> CREATOR = new Creator<ConnectCell>() {
        @Override
        public ConnectCell createFromParcel(Parcel in) {
            return new ConnectCell(in);
        }

        @Override
        public ConnectCell[] newArray(int size) {
            return new ConnectCell[size];
        }
    };
}
