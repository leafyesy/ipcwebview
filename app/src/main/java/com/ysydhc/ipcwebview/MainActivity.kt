package com.ysydhc.ipcwebview

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.ysydhc.commonlib.LogUtil
import com.ysydhc.ipcwebview.test.ITest
import com.ysydhc.interfaceipc.IObjectConnect
import com.ysydhc.interfaceipc.InterfaceIPCConst
import com.ysydhc.interfaceipc.InterfaceIpcHub
import com.ysydhc.interfaceipc.model.ConnectCell
import com.ysydhc.interfaceipc.proxy.InterfaceProxy
import com.ysydhc.ipcscaffold.IPCInitiator
import com.ysydhc.ipcscaffold.RemoteServicePresenter
import com.ysydhc.ipcwebview.test.TestListener
import com.ysydhc.remoteweb.readerview.WebViewImageReaderView

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var webViewImageReaderView: WebViewImageReaderView? = null
    private var isSetJsBridge: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn1).setOnClickListener {
            IPCInitiator.startRemote(this)
        }
        findViewById<View>(R.id.create_remote_webview).setOnClickListener {
            if (webViewImageReaderView != null) {
                return@setOnClickListener
            }
            webViewImageReaderView = WebViewImageReaderView(this)
            findViewById<ViewGroup>(R.id.remote_webview).addView(webViewImageReaderView)
        }
        findViewById<View>(R.id.set_callback).setOnClickListener {
            if (!isSetJsBridge) {
                webViewImageReaderView?.setJsBridgeListener {
                    LogUtil.i(TAG, "url: $it")
                    true
                }
                isSetJsBridge = true
            }
        }
        findViewById<View>(R.id.load_url).setOnClickListener {
            webViewImageReaderView?.loadUrl("https://www.baidu.com")
        }

    }


}