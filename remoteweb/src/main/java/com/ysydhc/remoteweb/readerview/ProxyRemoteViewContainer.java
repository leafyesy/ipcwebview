package com.ysydhc.remoteweb.readerview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProxyRemoteViewContainer extends FrameLayout {

    public ProxyRemoteViewContainer(@NonNull Context context) {
        super(context);
    }

    public ProxyRemoteViewContainer(@NonNull Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ProxyRemoteViewContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    TouchListener listener;
    private float lastX = 0;
    private float lastY = 0;

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

    public interface TouchListener {

        boolean onTouchEvent(MotionEvent event);
    }
}
