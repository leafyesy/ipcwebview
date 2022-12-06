package com.ysydhc.remoteview.interfaces;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ysydhc.interfaceipc.annotation.IpcMethodFlag;

public interface IRemoteView {

    @IpcMethodFlag()
    void release();

    @IpcMethodFlag()
    boolean dispatchTouchEvent(MotionEvent event);

    @IpcMethodFlag()
    boolean dispatchKeyEvent(KeyEvent keyEvent);

    @IpcMethodFlag()
    void onCreateInputConnectionProxy(@NonNull EditorInfo outAttrs);

    @IpcMethodFlag()
    void showInput();

    @IpcMethodFlag()
    void hideInput();

    @IpcMethodFlag()
    void remoteSetInputText(String text, int index);

    @IpcMethodFlag(value = IpcMethodFlag.KEY_LOCAL_CALLBACK_SET)
    void setRemoteViewInputListener(@Nullable InputListener listener);

    interface InputListener {

        void textChanged(String text, int index);

    }

}
