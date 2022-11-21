package com.ysydhc.ipcscaffold;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class ZygoteActivity extends Activity {

    public static ZygoteActivity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ZygoteActivity.activity = activity;
        super.onCreate(savedInstanceState);
    }
}
