package com.ysydhc.remoteweb.interfaces;

import com.ysydhc.interfaceipc.annotation.IpcMethodFlag;
import com.ysydhc.remoteview.interfaces.IRemoteView;

public interface IRemoteWebView extends IRemoteView {

    @IpcMethodFlag
    void loadUrl(String url);


    @IpcMethodFlag(value = IpcMethodFlag.KEY_LOCAL_CALLBACK_SET)
    void setJsBridgeListener(JsBridgeListener listener);

    interface JsBridgeListener {

        boolean onBridge(String url);

    }

}
