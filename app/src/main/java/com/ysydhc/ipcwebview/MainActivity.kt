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

    private var interfaceProxy: InterfaceProxy<ITest>? = null
    private var webViewImageReaderView: WebViewImageReaderView? = null
    private var test: ITest? = null
    private var isSetJsBridge: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn1).setOnClickListener {
            IPCInitiator.startRemote(this)
        }
        findViewById<View>(R.id.create_remote).setOnClickListener {
            createRemoteObjectAndConnect()
            createRemoteProxy()
        }
        findViewById<View>(R.id.set_callback).setOnClickListener {
            if (!isSetJsBridge) {
                webViewImageReaderView?.setJsBridgeListener {
                    LogUtil.i(TAG, "url: $it")
                    true
                }
                //isSetJsBridge = true
            }
            webViewImageReaderView?.loadUrl("https://www.baidu.com")
        }
        findViewById<View>(R.id.call_remote).setOnClickListener {
            if (webViewImageReaderView != null) {
                return@setOnClickListener
            }
            webViewImageReaderView = WebViewImageReaderView(this)
            findViewById<ViewGroup>(R.id.remote_webview).addView(webViewImageReaderView)

        }


    }

    private fun createRemoteObjectAndConnect() {
        val connectBinder = RemoteServicePresenter.getInstance()
            .queryBinderByCode<IObjectConnect>(InterfaceIPCConst.BINDER_CODE_OBJ_CONNECT)
        connectBinder.connect(ConnectCell(ITest.KEY_CONNECT))
    }

    private fun createRemoteProxy() {
        interfaceProxy = InterfaceProxy(ITest.KEY_CONNECT, ITest::class.java)
        InterfaceIpcHub.getInstance().putIpcImpl(ITest.KEY_CONNECT, interfaceProxy)
        test = interfaceProxy?.createProxy()
    }


}