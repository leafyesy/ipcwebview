package com.ysydhc.remoteweb.readerview;

import android.content.Context;
import android.graphics.Color;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ysydhc.commonlib.LogUtil;
import com.ysydhc.interfaceipc.IObjectConnect;
import com.ysydhc.interfaceipc.InterfaceIPCConst;
import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.model.ConnectCell;
import com.ysydhc.interfaceipc.proxy.InterfaceProxy;
import com.ysydhc.ipcscaffold.RemoteServicePresenter;
import com.ysydhc.remoteweb.interfaces.IRemoteWebView;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class WebViewImageReaderView extends ProxyRemoteViewContainer implements IRemoteWebView, ISurfaceCreateCallback {

    private static final String TAG = "WebViewImageReaderView";
    private IRemoteWebView remoteProxy;
    private FlutterImageView imageView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Surface surface;

    private final Runnable drawRunnable = new Runnable() {

        @Override
        public void run() {
            if (imageView != null) {
                imageView.acquireLatestImage();
            }
        }
    };

    public WebViewImageReaderView(@NonNull Context context) {
        super(context);
        init();
    }

    public WebViewImageReaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WebViewImageReaderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        imageView = new FlutterImageView(getContext());
        imageView.setBackgroundColor(Color.RED);
        imageView.getImageReader().setOnImageAvailableListener(new OnImageAvailableListener() {

            @Override
            public void onImageAvailable(ImageReader reader) {
                if (reader == null) {
                    return;
                }
                handler.removeCallbacks(drawRunnable);
                handler.postDelayed(drawRunnable, 0);
            }
        }, handler);
        addView(imageView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        tryInit();
    }

    private void tryInit() {
        if (surface != null) {
            return;
        }
        surface = imageView.getSurface();
        IObjectConnect connectBinder = RemoteServicePresenter.getInstance()
                .queryBinderByCode(InterfaceIPCConst.BINDER_CODE_OBJ_CONNECT);
        try {
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            HashMap<String, Object> arguments = new HashMap<String, Object>();
            arguments.put("flag_remote_web", true);
            arguments.put("physical_width", (int) displayMetrics.widthPixels);
            arguments.put("physical_height", (int) (displayMetrics.density * 200 + 0.5F));
            ConnectCell connectCell = new ConnectCell(surface.hashCode(), surface, arguments);
            connectBinder.connect(connectCell);
        } catch (RemoteException e) {
            LogUtil.exception(TAG, e);
        }
        InterfaceProxy<IRemoteWebView> interfaceProxy = new InterfaceProxy<>(surface.hashCode(), IRemoteWebView.class);
        InterfaceIpcHub.getInstance().putIpcImpl(surface.hashCode(), interfaceProxy);
        try {
            remoteProxy = interfaceProxy.createProxy();
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            LogUtil.exception(TAG, e);
        }
    }

    @Override
    public void loadUrl(String url) {
        if (remoteProxy != null) {
            remoteProxy.loadUrl(url);
        }
    }

    @Override
    public void setJsBridgeListener(JsBridgeListener listener) {
        if (remoteProxy != null) {
            remoteProxy.setJsBridgeListener(listener);
        }
    }

    @Override
    public void createSuccess() {

    }

    @Override
    public void createError(String s1, String s2) {

    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (remoteProxy != null) {
            return remoteProxy.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (remoteProxy != null) {
            return remoteProxy.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return super.onCreateInputConnection(outAttrs);
    }

    @Override
    public boolean checkInputConnectionProxy(View view) {
        return super.checkInputConnectionProxy(view);
    }

    @Override
    public void release() {
        if (remoteProxy != null) {
            remoteProxy.release();
        }
    }
}
