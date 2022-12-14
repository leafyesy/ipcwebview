package com.ysydhc.remoteview.view;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.remoteview.interfaces.IPresentationListener;
import com.ysydhc.remoteview.interfaces.IRemoteView;
import java.util.Stack;
import java.util.function.Function;


/**
 * I try to hook the {@link InputMethodManager} and listen soft-input's state, but failed cause cache.
 * Than i wanna extends {@link InputMethodManager} but also failed because final.
 * <p>
 * I retry to hook {@link InputMethodManager} constructor or static method, and didn't work
 * because system-hide api.
 * <p>
 * TODO So may be try native hook.
 * <p>
 * This is plan B.
 */
public class InputToggleDelegate {

    private static final String TAG = "InputToggleDelegate";

    private static final String LAZY_THREAD = "LazyThread";

    private String targetClassName = "ImeAdapterImpl";

    private String targetMethod = "updateState";

    private final Debounce debounce = new Debounce();

    private WindowManager windowManager;

    private IRemoteView remoteView;

    private final Handler.Callback lazyCall = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            debounce.lastCall = System.currentTimeMillis();
            if (getTopListener().getPresentationRunningState() == PresentationRunningState.Idle) {
                requestToggleSoftInput();
            }
            return false;
        }
    };

    private final HandlerThread ht = new HandlerThread(LAZY_THREAD);

    private final Handler lazyHandler;


    private final Stack<IPresentationListener> stateListener = new Stack<>();

    public InputToggleDelegate() {
        ht.start();
        lazyHandler = new Handler(ht.getLooper(), lazyCall);
    }

    public void setRemoteView(IRemoteView remoteView) {
        this.remoteView = remoteView;
    }

    public void registerPresentationListener(IPresentationListener presentationListener) {
        stateListener.push(presentationListener);
    }

    public void removePresentationListener(IPresentationListener listener) {
        stateListener.remove(listener);
    }

    public void setWindowManager(WindowManager manager) {
        this.windowManager = manager;
    }

    private IPresentationListener getTopListener() {
        return stateListener.peek();
    }

    /**
     * This is a way for toggle soft input.
     * <p>
     * In the start i though hook {@linkplain InputMethodManager} to
     * listen softInput status(show/hide), but in some High lvl api, system class is @hide
     * or @guard.
     * <p>
     * As stated above, i chosen(temporary) override
     * {@linkplain android.content.Context#getSystemService(String)}, and listen-parse the invoke
     * to toggle soft-input. But in actual use, the performance is not goods.
     * <p>
     * TODO : MAKE IT BETTER
     *
     * @see InputMethodManager
     * @see android.hardware.display.VirtualDisplay
     */
    @MainThread
    public void inputServiceCall() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        LogUtil.exception(TAG, "", new Throwable());
        boolean hit = parse(stackTraceElements);
        try {
            if (hit) {
                windowManager.getDefaultDisplay();
                debounce.handle((a) -> {
                    lazyHandler.sendEmptyMessageDelayed(250, 500);
                    return null;
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestToggleSoftInput() {
        if (remoteView != null) {
            remoteView.showInput();
        }
//        try {
//            // TODO
//            //MainServicePresenter.getInstance().getMainProcessBinder().showSoftInput();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    private boolean parse(StackTraceElement[] stackTraceElements) {
        for (int i = 0; i < stackTraceElements.length; i++) {
            if (stackTraceElements[i].getClassName().contains(targetClassName)
                    && stackTraceElements[i].getMethodName().contains(targetMethod)) {
                int c = i;
                while (c >= 0) {
                    if (stackTraceElements[c].getClassName().equals(stackTraceElements[c - 1].getClassName())) {
                        c--;
                    } else {
                        break;
                    }
                }
                try {
                    if (stackTraceElements[c - 1].getClassName().contains(stackTraceElements[c - 2].getClassName())) {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

    private static class Debounce {

        private final int limit = 1000;

        long lastCall = 0;

        public void handle(Function function) {
            long n = System.currentTimeMillis();
            if (n - lastCall > limit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    function.apply(null);
                }
            }
        }
    }


}
