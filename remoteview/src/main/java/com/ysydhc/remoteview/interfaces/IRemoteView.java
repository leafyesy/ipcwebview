package com.ysydhc.remoteview.interfaces;

import android.view.KeyEvent;
import android.view.MotionEvent;
import com.ysydhc.interfaceipc.annotation.IpcMethodFlag;

public interface IRemoteView {

    @IpcMethodFlag()
    void release();

    @IpcMethodFlag()
    boolean dispatchTouchEvent(MotionEvent event);

    @IpcMethodFlag()
    boolean dispatchKeyEvent(KeyEvent keyEvent);
}
