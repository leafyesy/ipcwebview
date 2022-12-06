package com.ysydhc.remoteweb.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import com.ysydhc.interfaceipc.model.ConnectCell;
import com.ysydhc.remoteview.view.BaseRemoteViewPresentation;
import com.ysydhc.remoteview.view.PresentationRunningState;
import com.ysydhc.remoteview.view.RemoteAccessibilityEventsDelegate;
import com.ysydhc.remoteweb.interfaces.IRemoteWebView;


public class WebViewPresentation extends BaseRemoteViewPresentation implements IRemoteWebView {

    private static final String TAG = "WebViewPresentation";

    protected ConnectCell connectCell;

    protected WebView mOfflineWebView;
    private JsBridgeListener jsBridgeListener;
    private Context outContext;

    public WebViewPresentation(Context outerContext, ConnectCell connectCell,
            Display display, long surfaceId,
            RemoteAccessibilityEventsDelegate accessibilityEventsDelegate) {
        super(outerContext, display, accessibilityEventsDelegate, surfaceId);
        this.outContext = outerContext;
        this.connectCell = connectCell;
        //RemoteZygoteActivity.activity.getInputToggleDelegate().registerPresentationListener(this);
    }

    @Override
    public View getContentView(ViewGroup container) {
        mOfflineWebView = createTestWebView(container);
        return mOfflineWebView;
    }

    @Override
    protected InputConnection createInputConnection(EditorInfo outAttrs) {
        return mOfflineWebView.onCreateInputConnection(outAttrs);
    }

    protected WebView createTestWebView(View containerView) {
        WebView webView = new CustomWebView(outContext); // 必须使用外部的context,因为需要代理getSystemService方法
        //todo update web view init params  see -> WebViewCreationParamsModel
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setSupportMultipleWindows(true);
        settings.setUseWideViewPort(true);
        //设置可以支持缩放
        settings.setSupportZoom(true);
        //设置出现缩放工具
        settings.setBuiltInZoomControls(true);
        //设定缩放控件隐藏
        settings.setDisplayZoomControls(false);
        //最小缩放等级
        webView.setInitialScale(25);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (jsBridgeListener != null) {
                    if (jsBridgeListener.onBridge(url)) {
                        return true;
                    }
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (jsBridgeListener != null) {
                    jsBridgeListener.onBridge(request.getUrl().toString());
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Log.e("webview", "onPageStarted  " + url);
                runningState = PresentationRunningState.Loading;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.e("webview", "onPageFinished  " + url);
                runningState = PresentationRunningState.Idle;
                final Bundle bundle = new Bundle();
                bundle.putString("url", url);
                //when web page finished, we send a bundle to main process for save.
                //in some exception case, we can restore by it.
                saveViewStateInstance(bundle);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

            }
        });
        return webView;
    }

    @Override
    public void show() {
        super.show();
    }


    private WebView getWebView() {
        return mOfflineWebView;
    }

    @Override
    public void dispose() {
        //RemoteZygoteActivity.activity.getInputToggleDelegate().removePresentationListener(this);
        cancel();
        detachState();
        getWebView().destroy();
        super.dispose();
        dismiss();
    }

    protected void saveViewStateInstance(Bundle bundle) {
//        try {
//            MainServicePresenter.getInstance().getMainProcessBinder().setSavedInstance(bundle);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void loadUrl(String url) {
        if (mOfflineWebView == null) {
            return;
        }
        mOfflineWebView.loadUrl(url);
    }

    @Override
    public void setJsBridgeListener(JsBridgeListener listener) {
        this.jsBridgeListener = listener;
    }
}
