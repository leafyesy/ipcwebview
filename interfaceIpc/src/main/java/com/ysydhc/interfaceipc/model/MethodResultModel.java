package com.ysydhc.interfaceipc.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class MethodResultModel implements Parcelable {

    public static final MethodResultModel VOID_RESULT = new MethodResultModel();

    private Parcelable resultParcelable;
    private Serializable resultSerializable;
    private byte isCalledSuc;

    public MethodResultModel() {
        this.resultParcelable = VOID_RESULT;
    }

    public MethodResultModel(Parcelable result) {
        this.resultParcelable = result;
    }

    public MethodResultModel(Serializable result) {
        this.resultSerializable = result;
    }

    protected MethodResultModel(Parcel in) {
        resultParcelable = in.readParcelable(this.getClass().getClassLoader());
        resultSerializable = in.readSerializable();
        isCalledSuc = in.readByte();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(resultParcelable, flags);
        dest.writeSerializable(resultSerializable);
        dest.writeByte(isCalledSuc);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MethodResultModel> CREATOR = new Creator<MethodResultModel>() {
        @Override
        public MethodResultModel createFromParcel(Parcel in) {
            return new MethodResultModel(in);
        }

        @Override
        public MethodResultModel[] newArray(int size) {
            return new MethodResultModel[size];
        }
    };

    public Object getResult() {
        return resultParcelable == null ? resultSerializable : resultParcelable;
    }


}
