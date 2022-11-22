package com.ysydhc.remoteview.view;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;

import com.ysydhc.commonlib.LogUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class BaseRemoteViewPresentation extends Presentation {

    private static final String TAG = "BaseRemoteViewPresentation";

    protected long viewId;
    protected PresentationState state;
    private FrameLayout container;
    private BaseRemoteViewPresentation.AccessibilityDelegatingFrameLayout rootView;
    private final RemoteAccessibilityEventsDelegate accessibilityEventsDelegate;
    private boolean startFocused = false;

    private final View.OnFocusChangeListener focusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            LogUtil.i(TAG, " has focus : " + hasFocus);
            //todo send focus to flutter side
//            if(hasFocus) {
//                setInputConnectionTarget(state.childView);
//            }
        }
    };

    public BaseRemoteViewPresentation(Context outerContext, Display display,
            RemoteAccessibilityEventsDelegate accessibilityEventsDelegate, long viewId) {
        this(outerContext, display, accessibilityEventsDelegate, 0, viewId);
    }

    public BaseRemoteViewPresentation(Context outerContext, Display display,
            RemoteAccessibilityEventsDelegate accessibilityEventsDelegate, int theme, long viewId) {
        super(outerContext, display, theme);
        this.viewId = viewId;
        this.state = new PresentationState();
        this.accessibilityEventsDelegate = accessibilityEventsDelegate;
        init();
    }

    protected abstract View getContentView(ViewGroup container);

    private void init() {
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        if (Build.VERSION.SDK_INT >= 19) {
            this.getWindow().setType(WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION);
        }
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.state.fakeWindowViewGroup == null) {
            this.state.fakeWindowViewGroup = new BaseRemoteViewPresentation.FakeWindowViewGroup(this.getContext());
        }
        if (this.state.windowManagerHandler == null) {
            WindowManager windowManagerDelegate = (WindowManager) this.getContext()
                    .getSystemService(Context.WINDOW_SERVICE);
            this.state.windowManagerHandler = new WindowManagerHandler(windowManagerDelegate,
                    this.state.fakeWindowViewGroup);
        }
        this.container = new FrameLayout(this.getContext());
        if (this.state.childView == null) {
            this.state.childView = getContentView(container);
        }
        View embeddedView = this.state.childView;
        ViewParent parent = embeddedView.getParent();
        if (parent == null) {
            this.container.addView(embeddedView);
        } else if (parent != this.container) {
            ((ViewGroup) parent).removeView(embeddedView);
            this.container.addView(embeddedView);
        }

        this.rootView = new AccessibilityDelegatingFrameLayout(this.getContext(), this.accessibilityEventsDelegate,
                embeddedView);
        this.rootView.addView(this.container);
        this.rootView.addView(this.state.fakeWindowViewGroup);
        embeddedView.setOnFocusChangeListener(this.focusChangeListener);
        this.rootView.setFocusableInTouchMode(true);
        if (this.startFocused) {
            embeddedView.requestFocus();
        } else {
            this.rootView.requestFocus();
        }

        this.setContentView(this.rootView);
    }

    abstract protected void plugInHub();

    abstract protected void plugOutHub();

    @MainThread
    @CallSuper
    public void dispose() {
        detachState();
    }

    public PresentationState detachState() {
        this.container.removeAllViews();
        this.rootView.removeAllViews();
        return this.state;
    }

    static class WindowManagerHandler implements InvocationHandler {

        private static final String TAG = "PlatformViewsController";
        private final WindowManager delegate;
        BaseRemoteViewPresentation.FakeWindowViewGroup fakeWindowRootView;

        WindowManagerHandler(WindowManager delegate,
                BaseRemoteViewPresentation.FakeWindowViewGroup fakeWindowViewGroup) {
            this.delegate = delegate;
            this.fakeWindowRootView = fakeWindowViewGroup;
        }

        public WindowManager getWindowManager() {
            return (WindowManager) Proxy.newProxyInstance(WindowManager.class.getClassLoader(),
                    new Class[]{WindowManager.class}, this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            LogUtil.i(TAG, "method  :  " + method.getName());
            switch (method.getName()) {
                case "setLocalFocus":
                    LogUtil.i(TAG, "setLocalFocus===============");
                    LogUtil.exception(new Throwable());
                    LogUtil.i(TAG, "setLocalFocus===============");
                    break;
                case "addView":
                    addView(args);
                    return null;
                case "removeView":
                    removeView(args);
                    return null;
                case "removeViewImmediate":
                    removeViewImmediate(args);
                    return null;
                case "updateViewLayout":
                    updateViewLayout(args);
                    return null;
            }
            try {
                return method.invoke(delegate, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        private void addView(Object[] args) {
            if (this.fakeWindowRootView == null) {
                LogUtil.i("PlatformViewsController", "Embedded view called addView while detached from presentation");
            } else {
                View view = (View) args[0];
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) args[1];
                this.fakeWindowRootView.addView(view, layoutParams);
            }
        }

        private void removeView(Object[] args) {
            if (this.fakeWindowRootView == null) {
                LogUtil.i("PlatformViewsController",
                        "Embedded view called removeView while detached from presentation");
            } else {
                View view = (View) args[0];
                this.fakeWindowRootView.removeView(view);
            }
        }

        private void removeViewImmediate(Object[] args) {
            if (this.fakeWindowRootView == null) {
                LogUtil.i("PlatformViewsController",
                        "Embedded view called removeViewImmediate while detached from presentation");
            } else {
                View view = (View) args[0];
                view.clearAnimation();
                this.fakeWindowRootView.removeView(view);
            }
        }

        private void updateViewLayout(Object[] args) {
            if (this.fakeWindowRootView == null) {
                LogUtil.i("PlatformViewsController",
                        "Embedded view called updateViewLayout while detached from presentation");
            } else {
                View view = (View) args[0];
                WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) args[1];
                this.fakeWindowRootView.updateViewLayout(view, layoutParams);
            }
        }
    }

    static class FakeWindowViewGroup extends ViewGroup {

        private final Rect viewBounds = new Rect();
        private final Rect childRect = new Rect();

        public FakeWindowViewGroup(Context context) {
            super(context);
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            for (int i = 0; i < this.getChildCount(); ++i) {
                View child = this.getChildAt(i);
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) child.getLayoutParams();
                this.viewBounds.set(l, t, r, b);
                Gravity.apply(params.gravity, child.getMeasuredWidth(), child.getMeasuredHeight(), this.viewBounds,
                        params.x, params.y, this.childRect);
                child.layout(this.childRect.left, this.childRect.top, this.childRect.right, this.childRect.bottom);
            }

        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            for (int i = 0; i < this.getChildCount(); ++i) {
                View child = this.getChildAt(i);
                child.measure(atMost(widthMeasureSpec), atMost(heightMeasureSpec));
            }

            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

        private static int atMost(int measureSpec) {
            return MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(measureSpec), MeasureSpec.AT_MOST);
        }
    }

    private static class AccessibilityDelegatingFrameLayout extends FrameLayout {

        private final RemoteAccessibilityEventsDelegate accessibilityEventsDelegate;
        private final View embeddedView;

        public AccessibilityDelegatingFrameLayout(Context context,
                RemoteAccessibilityEventsDelegate accessibilityEventsDelegate, View embeddedView) {
            super(context);
            this.accessibilityEventsDelegate = accessibilityEventsDelegate;
            this.embeddedView = embeddedView;
        }

        public boolean requestSendAccessibilityEvent(View child, AccessibilityEvent event) {
            return this.accessibilityEventsDelegate.requestSendAccessibilityEvent(this.embeddedView, child, event);
        }
    }


    static class PresentationState {

        protected View childView;
        private WindowManagerHandler windowManagerHandler;
        private BaseRemoteViewPresentation.FakeWindowViewGroup fakeWindowViewGroup;

        PresentationState() {
        }
    }


}

