package com.ysydhc.ipcscaffold;

import android.app.Activity;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ysydhc.commonlib.LogUtil;
import java.util.ArrayList;
import java.util.List;

public class ZygoteActivity extends Activity {

    public static ZygoteActivity activity;
    private List<SystemServiceCallback> systemServiceCallbackList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ZygoteActivity.activity = this;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }

    @Override
    public Object getSystemService(@NonNull String name) {
        LogUtil.i("RemoteZygoteActivity", "getSystemService ====  " + name);
        if (systemServiceCallbackList != null) {
            for (SystemServiceCallback callback : systemServiceCallbackList) {
                callback.callGetSystemService(name);
            }
        }
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            String systemServiceName = getSystemServiceName(InputMethodManager.class);
            if (TextUtils.equals(systemServiceName, name)) {
                LogUtil.i("RemoteZygoteActivity", "input method Manager get " + systemServiceName);
            }
        }
        return super.getSystemService(name);
    }

    public void addSystemServiceCallback(SystemServiceCallback systemServiceCallback) {
        if (systemServiceCallbackList == null) {
            systemServiceCallbackList = new ArrayList<>();
        }
        if (!systemServiceCallbackList.contains(systemServiceCallback)) {
            systemServiceCallbackList.add(systemServiceCallback);
        }
    }

    public void removeSystemServiceCallback(SystemServiceCallback systemServiceCallback) {
        if (systemServiceCallbackList != null) {
            systemServiceCallbackList.remove(systemServiceCallback);
        }
    }

    public interface SystemServiceCallback {

        void callGetSystemService(String name);

    }

}
