package com.ysydhc.remoteweb.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CustomWebView extends WebView {

    public CustomWebView(@NonNull Context context) {
        this(context, null);
    }

    public CustomWebView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CustomWebView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        //inputConnection.commitText()
        return inputConnection;
    }

    @Override
    public boolean checkInputConnectionProxy(View view) {
        return super.checkInputConnectionProxy(view);
    }
}
