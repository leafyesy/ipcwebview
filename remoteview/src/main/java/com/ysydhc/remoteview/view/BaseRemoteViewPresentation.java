package com.ysydhc.remoteview.view;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Presentation;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.proxy.InterfaceProxy;
import com.ysydhc.ipcscaffold.ZygoteActivity;
import com.ysydhc.remoteview.interfaces.IPresentationListener;
import com.ysydhc.remoteview.interfaces.IRemoteView;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class BaseRemoteViewPresentation extends Presentation implements IRemoteView, IPresentationListener {

    private static final String TAG = "BaseRemoteViewPresentation";

    protected long viewId;
    protected PresentationState state;
    private FrameLayout container;
    private BaseRemoteViewPresentation.AccessibilityDelegatingFrameLayout rootView;
    private final RemoteAccessibilityEventsDelegate accessibilityEventsDelegate;
    private boolean startFocused = false;
    private IRemoteView remoteProxy;
    protected PresentationRunningState runningState = PresentationRunningState.Idle;

    private InputToggleDelegate inputToggleDelegate = new InputToggleDelegate();
    private InputListener inputListener;

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
        init(outerContext);
    }

    protected abstract View getContentView(ViewGroup container);

    private void init(Context context) {
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        if (Build.VERSION.SDK_INT >= 19) {
            this.getWindow().setType(WindowManager.LayoutParams.TYPE_PRIVATE_PRESENTATION);
        }
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        inputToggleDelegate.setRemoteView(this);

        checkRemoteView();
        if (context instanceof ZygoteActivity) {
            ((ZygoteActivity) context).addSystemServiceCallback(name -> {
                if (name.equals(INPUT_METHOD_SERVICE)) {
                    inputToggleDelegate.inputServiceCall();
                }
            });
        }
    }

    private void checkRemoteView() {
        if (remoteProxy == null) {
            InterfaceProxy<?> interfaceProxy = InterfaceIpcHub.getInstance().fetchCallObject(viewId);
            if (interfaceProxy != null && interfaceProxy.getOutProxy() != null) {
                try {
                    remoteProxy = (IRemoteView) interfaceProxy.createProxy();
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String getCurInputText() {
        if (lastInputConnection != null) {
            String pre = (String) lastInputConnection.getTextBeforeCursor(Integer.MAX_VALUE,
                    InputConnection.GET_TEXT_WITH_STYLES);
            String after = (String) lastInputConnection.getTextAfterCursor(Integer.MAX_VALUE,
                    InputConnection.GET_TEXT_WITH_STYLES);
            return (pre == null ? "" : pre) + (after == null ? "" : after);
        }
        return "";
    }

    @Override
    public void showInput() {
        checkRemoteView();
        if (remoteProxy != null) {
            remoteProxy.showInput();
        }
    }

    @Override
    public void hideInput() {
        checkRemoteView();
        if (remoteProxy != null) {
            remoteProxy.hideInput();
        }
    }

    @Override
    public void release() {

    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        return super.dispatchKeyEvent(event);
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
            inputToggleDelegate.setWindowManager(windowManagerDelegate);
            this.state.windowManagerHandler = new WindowManagerHandler(windowManagerDelegate,
                    this.state.fakeWindowViewGroup);
        }
        inputToggleDelegate.registerPresentationListener(this);
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

    private InputConnection lastInputConnection = null;
    private String showText = "";
    private int curIndex = -1;

    @Override
    public void onCreateInputConnectionProxy(@NonNull EditorInfo outAttrs) {
        lastInputConnection = createInputConnection(outAttrs);
    }

    protected abstract InputConnection createInputConnection(EditorInfo outAttrs);

    @Override
    public void remoteSetInputText(String text, int index) {
        if (lastInputConnection != null) {
            String pre = (String) lastInputConnection.getTextBeforeCursor(Integer.MAX_VALUE,
                    InputConnection.GET_TEXT_WITH_STYLES);
            String after = (String) lastInputConnection.getTextAfterCursor(Integer.MAX_VALUE,
                    InputConnection.GET_TEXT_WITH_STYLES);
            String curInputText = (pre == null ? "" : pre) + (after == null ? "" : after);
            if (TextUtils.isEmpty(curInputText)) {
                curIndex = -1;
            }
            if (!TextUtils.equals(curInputText, showText)) {
                text = text.replace(showText, curInputText);
                // ???????????????????????????
                showText = text;
                if (inputListener != null) {
                    inputListener.textChanged(showText, showText.length());
                }
            }
            lastInputConnection.deleteSurroundingText(showText.length(), 0);
            lastInputConnection.commitText(text, text == null ? 0 : text.length());
            showText = text;
        }
    }


    @MainThread
    @CallSuper
    public void dispose() {
        inputToggleDelegate.removePresentationListener(this);
        detachState();
    }

    public PresentationState detachState() {
        this.container.removeAllViews();
        this.rootView.removeAllViews();
        return this.state;
    }

    @Override
    public PresentationRunningState getPresentationRunningState() {
        return runningState;
    }

    @Override
    public void setRemoteViewInputListener(@NonNull InputListener listener) {
        inputListener = listener;
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

