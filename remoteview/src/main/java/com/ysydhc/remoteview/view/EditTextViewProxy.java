package com.ysydhc.remoteview.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.remoteview.interfaces.IRemoteView;

public class EditTextViewProxy extends androidx.appcompat.widget.AppCompatEditText {


    private static final String TAG = "EditTextViewProxy";
    private IRemoteView remoteView;

    public EditTextViewProxy(@NonNull Context context) {
        super(context);
        initTextListener();
    }

    public EditTextViewProxy(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTextListener();
    }

    public EditTextViewProxy(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTextListener();
    }

    public void setRemoteView(IRemoteView remoteView) {
        this.remoteView = remoteView;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    private void initTextListener() {
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                LogUtil.i(TAG, "beforeTextChanged s:" + s + ", start:" + start + ", count:" + count + ", after:");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                LogUtil.i(TAG, "onTextChanged s:" + s + ", start:" + start + ", count:" + count + ", after:");
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (remoteView != null) {
                    remoteView.remoteSetInputText(s.toString(), getSelectionStart());
                }
            }
        });
    }

    @Nullable
    @Override
    public InputConnection onCreateInputConnection(@NonNull EditorInfo outAttrs) {
        if (remoteView != null) {
            remoteView.onCreateInputConnectionProxy(outAttrs);
        }
        return super.onCreateInputConnection(outAttrs);
    }
}
