package com.ysydhc.ipcwebview.ipc;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Surface;

import com.ysydhc.interfaceipc.InterfaceIpcHub;
import com.ysydhc.interfaceipc.connect.IConnectObjectCreator;
import com.ysydhc.interfaceipc.model.ConnectCell;
import com.ysydhc.ipcscaffold.ProcessServicePresenter.BinderManager;
import com.ysydhc.ipcscaffold.initiator.IIPCInitiatorTask;
import com.ysydhc.remoteview.view.RemoteAccessibilityEventsDelegate;
import com.ysydhc.remoteweb.view.WebViewPresentation;

public class RemoteWebViewInitiator implements IIPCInitiatorTask {

    final RemoteAccessibilityEventsDelegate remoteAccessibilityEventsDelegate = new RemoteAccessibilityEventsDelegate();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void init(BinderManager manager) {
        // 绑定 Object key -> Object创建
        InterfaceIpcHub.getInstance().setConnectObjectCreateList(new IConnectObjectCreator() {
            @Override
            public Object create(ConnectCell cell) {
                if (cell != null && cell.getArguments() != null) {
                    Object flagRemoteWeb = cell.getArguments().get("flag_remote_web");
                    int pw = (int) cell.getArguments().get("physical_width");
                    int ph = (int) cell.getArguments().get("physical_height");

                    if (flagRemoteWeb == Boolean.TRUE) {
                        final DisplayMetrics dm = manager.getContext().getResources().getDisplayMetrics();
                        int densityDpi = dm.densityDpi;
                        DisplayManager displayManager = (DisplayManager) manager.getContext().getSystemService(
                                Context.DISPLAY_SERVICE);
                        final VirtualDisplay vd = displayManager.createVirtualDisplay(
                                "remote_web_view_" + cell.getKey(), pw, ph, densityDpi, (Surface) cell.getExt(), 0);
                        WebViewPresentation viewPresentation = new WebViewPresentation(manager.getContext(), cell,
                                vd.getDisplay(), cell.getKey(),
                                remoteAccessibilityEventsDelegate);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                viewPresentation.create();
                                viewPresentation.show();
                            }
                        });
                        return viewPresentation;
                    }
                }
                return null;
            }
        });
    }
}
