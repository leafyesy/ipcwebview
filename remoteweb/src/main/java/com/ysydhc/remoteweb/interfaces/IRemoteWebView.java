package com.ysydhc.remoteweb.interfaces;

import com.ysydhc.interfaceipc.annotation.IpcMethodFlag;

public interface IRemoteWebView {

    int KEY_REMOTE_WEB_VIEW = 11111;

    @IpcMethodFlag
    void loadUrl(String url);


}
