package com.ysydhc.interfaceipc.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MethodResultModel implements Parcelable {

    public static final MethodResultModel VOID_RESULT = new MethodResultModel();

    private Object result;

    public MethodResultModel() {
        this.result = VOID_RESULT;
    }

    public MethodResultModel(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    protected MethodResultModel(Parcel in) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
}
