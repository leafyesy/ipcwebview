package com.ysydhc.remoteview.view;

import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;


public class RemoteAccessibilityEventsDelegate {

    public RemoteAccessibilityEventsDelegate() {
    }

    public boolean requestSendAccessibilityEvent(@NonNull View embeddedView, @NonNull View eventOrigin, @NonNull AccessibilityEvent event) {
//        return this.accessibilityBridge == null ? false : this.accessibilityBridge.externalViewRequestSendAccessibilityEvent(embeddedView, eventOrigin, event);
        return false;
    }

    void setAccessibilityBridge(
//            @Nullable AccessibilityBridge accessibilityBridge
    ) {
//        this.accessibilityBridge = accessibilityBridge;
    }
}
