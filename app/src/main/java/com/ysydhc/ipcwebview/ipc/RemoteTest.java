package com.ysydhc.ipcwebview.ipc;

import androidx.annotation.NonNull;

import com.ysydhc.ipcwebview.test.ITest;
import com.ysydhc.ipcwebview.test.TestListener;

public class RemoteTest implements ITest {

    int count = 1000;
    private TestListener listener;

    @Override
    public int countPlus() {
        if (listener != null) {
            listener.onConnect(count);
        }
        return count++;
    }

    @Override
    public void setListener(@NonNull TestListener listener) {
        this.listener = listener;
    }
}
