package com.ysydhc.remoteweb.interfaces;

import com.ysydhc.interfaceipc.annotation.IpcMethodFlag;
import com.ysydhc.remoteview.interfaces.IRemoteView;

public interface IRemoteWebView extends IRemoteView {

    @IpcMethodFlag
    void loadUrl(String url);

}
