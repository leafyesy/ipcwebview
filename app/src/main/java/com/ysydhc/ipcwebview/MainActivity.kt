package com.ysydhc.ipcwebview

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.ysydhc.ipcwebview.test.ITest
import com.ysydhc.interfaceipc.IMethodChannelBinder
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
    private val listener = object : TestListener {
        override fun onConnect(count: Int): Boolean {
            Log.i(TAG, "TestListener :$count")
            return count % 2 == 0
        }
    }

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
            //test?.setListener(listener)
            webViewImageReaderView?.loadUrl("https://www.baidu.com")
        }
        findViewById<View>(R.id.call_remote).setOnClickListener {
            //callCountPlus()
            if (webViewImageReaderView != null) {
                return@setOnClickListener
            }
            webViewImageReaderView = WebViewImageReaderView(this)
            findViewById<ViewGroup>(R.id.remote_webview).addView(webViewImageReaderView)

        }


    }

    private fun callCountPlus() {
        val result = test?.countPlus() ?: 0
        Log.i(TAG, "result:$result")
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