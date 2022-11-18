package com.ysydhc.ipcwebview.ipc;

import com.ysydhc.ipcwebview.test.ITest;

public class RemoteTest implements ITest {

    int count = 1000;

    @Override
    public int countPlus() {
        return count ++;
    }
}
