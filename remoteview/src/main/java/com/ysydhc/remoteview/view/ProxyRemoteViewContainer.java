package com.ysydhc.remoteview.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.ysydhc.remoteview.interfaces.IRemoteView;

public abstract class ProxyRemoteViewContainer extends FrameLayout implements IRemoteView {

    private final EditTextViewProxy editTextViewProxy = new EditTextViewProxy(this.getContext());
    private InputListener inputListener;
    TouchListener listener;
    private float lastX = 0;
    private float lastY = 0;

    public ProxyRemoteViewContainer(@NonNull Context context) {
        super(context);
        initEditTextViewProxy();
    }

    public ProxyRemoteViewContainer(@NonNull Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
        initEditTextViewProxy();
    }

    public ProxyRemoteViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEditTextViewProxy();
    }

    private void initEditTextViewProxy() {
        addView(editTextViewProxy, new LayoutParams(1, 1));
        editTextViewProxy.setRemoteView(this);
        editTextViewProxy.setVisibility(View.INVISIBLE);
    }

    public void setListener(TouchListener listener) {
        this.listener = listener;
    }

    protected boolean isNeedProxyTouchEvent() {
        return true;
    }

    protected boolean isFixHorizontalScroll() {
        return true;
    }

    protected boolean isFixVerticalScroll() {
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isNeedProxyTouchEvent()) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                lastX = ev.getX();
                lastY = ev.getY();
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (isFixHorizontalScroll() && isFixVerticalScroll()) {
                    requestDisallowInterceptTouchEvent(true);
                } else if (isFixHorizontalScroll()) {
                    if (Math.abs(ev.getX() - lastX) > Math.abs(ev.getY() - lastY)) {
                        requestDisallowInterceptTouchEvent(true);
                    }
                } else if (isFixVerticalScroll()) {
                    if (Math.abs(ev.getX() - lastX) < Math.abs(ev.getY() - lastY)) {
                        requestDisallowInterceptTouchEvent(true);
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                requestDisallowInterceptTouchEvent(false);
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isNeedProxyTouchEvent()) {
            if (listener != null) {
                return listener.onTouchEvent(event);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void showInput() {
        postDelayed(() -> {
            editTextViewProxy.setVisibility(View.VISIBLE);
            editTextViewProxy.requestFocus();
            InputMethodManager input = (InputMethodManager) getContext().getApplicationContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            input.showSoftInput(editTextViewProxy, InputMethodManager.SHOW_FORCED);
            setRemoteViewInputListener((text, index) -> post(new Runnable() {
                @Override
                public void run() {
                    editTextViewProxy.setText(text);
                    editTextViewProxy.setSelection(index);
                }
            }));
        }, 1);
    }

    @Override
    public void hideInput() {
        postDelayed(() -> {
            editTextViewProxy.setVisibility(View.INVISIBLE);
            editTextViewProxy.clearFocus();
            InputMethodManager input = (InputMethodManager) getContext().getApplicationContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(editTextViewProxy.getWindowToken(), InputMethodManager.SHOW_FORCED);
            setRemoteViewInputListener(null);
        }, 1);
    }

    public interface TouchListener {

        boolean onTouchEvent(MotionEvent event);
    }
}
