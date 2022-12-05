package com.ysydhc.remoteweb.view;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ysydhc.ipcscaffold.ZygoteActivity;
import com.ysydhc.ipcscaffold.ZygoteActivity.SystemServiceCallback;
import com.ysydhc.remoteview.view.InputToggleDelegate;

public class CustomWebView extends WebView {

    private InputToggleDelegate inputToggleDelegate = new InputToggleDelegate();

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
        if (context instanceof ZygoteActivity) {
            ((ZygoteActivity) context).addSystemServiceCallback(new SystemServiceCallback() {
                @Override
                public void callGetSystemService(String name) {
                    if (name.equals(INPUT_METHOD_SERVICE)) {
                        inputToggleDelegate.inputServiceCall();
                        // 手动设置一下文案
                        InputMethodManager input = (InputMethodManager) context.getApplicationContext()
                                .getSystemService(INPUT_METHOD_SERVICE);
                        ExtractedText text = new ExtractedText();
                        text.text = "哈哈哈";
                        input.updateExtractedText(CustomWebView.this, 0, text);
                    }
                }
            });
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return super.onCreateInputConnection(outAttrs);
    }

    @Override
    public boolean checkInputConnectionProxy(View view) {
        return super.checkInputConnectionProxy(view);
    }
}
