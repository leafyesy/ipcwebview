package com.ysydhc.remoteweb.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ysydhc.interfaceipc.model.ConnectCell;
import com.ysydhc.remoteview.interfaces.IPresentationListener;
import com.ysydhc.remoteview.view.BaseRemoteViewPresentation;
import com.ysydhc.remoteview.view.PresentationRunningState;
import com.ysydhc.remoteview.view.RemoteAccessibilityEventsDelegate;
import com.ysydhc.remoteweb.interfaces.IRemoteWebView;


public class WebViewPresentation extends BaseRemoteViewPresentation implements IPresentationListener, IRemoteWebView {

    private static final String TAG = "WebViewPresentation";

    protected ConnectCell connectCell;

    protected PresentationRunningState runningState = PresentationRunningState.Idle;
    protected WebView mOfflineWebView;

    public WebViewPresentation(Context outerContext, ConnectCell connectCell,
            Display display, long surfaceId,
            RemoteAccessibilityEventsDelegate accessibilityEventsDelegate) {
        super(outerContext, display, accessibilityEventsDelegate, surfaceId);
        this.connectCell = connectCell;
        plugInHub();
        //RemoteZygoteActivity.activity.getInputToggleDelegate().registerPresentationListener(this);
    }

    @Override
    public View getContentView(ViewGroup container) {
        mOfflineWebView = createTestWebView(container);
        return mOfflineWebView;
    }

    protected WebView createTestWebView(View containerView) {
        WebView webView = new WebView(containerView.getContext());
        //todo update web view init params  see -> WebViewCreationParamsModel
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setSupportMultipleWindows(true);
        webView.setWebViewClient(new WebViewClient() {
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
        webView.setBackgroundColor(Color.BLUE);
//        webView.getView().setBackgroundColor(Color.TRANSPARENT);

//        webView.setJsBridgeListener(new IJsBridgeListener() {
//            @Override
//            public void onJsBridge(String cmd, String subcmd, Map<String, String> params) {
//                LogUtil.i(TAG, "cmd: " + cmd + " subcmd: " + subcmd + " params: " + params);
//            }
//        });
        return webView;
    }

    @Override
    public void show() {
//        if (initialParams.getUrl() != null && !initialParams.getUrl().isEmpty()) {
////            if (getWebView() != null) {
////                getWebView().loadUrl(initialParams.getUrl());
////            }
//        }
        super.show();
    }

    @Override
    protected void plugInHub() {
        //RemoteBinderCommHub.getInstance().plugInMethodHandler(viewId, this);
    }

    @Override
    protected void plugOutHub() {
        //RemoteBinderCommHub.getInstance().plugOutMethodHandler(viewId);
    }

    private WebView getWebView() {
        return mOfflineWebView;
    }

    @Override
    public void dispose() {
        //RemoteZygoteActivity.activity.getInputToggleDelegate().removePresentationListener(this);
        plugOutHub();
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
    public PresentationRunningState getPresentationRunningState() {
        return runningState;
    }

    @Override
    public void loadUrl(String url) {
        if (mOfflineWebView == null) {
            return;
        }
        mOfflineWebView.loadUrl(url);
    }
}
