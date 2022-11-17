package com.ysydhc.ipcwebview

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysydhc.aninterface.test.ITest
import com.ysydhc.interfaceipc.IMethodChannelBinder
import com.ysydhc.interfaceipc.IObjectConnect
import com.ysydhc.interfaceipc.InterfaceIPCConst
import com.ysydhc.interfaceipc.model.ConnectCell
import com.ysydhc.interfaceipc.proxy.InterfaceProxy
import com.ysydhc.ipcscaffold.IPCInitiator
import com.ysydhc.ipcscaffold.RemoteServicePresenter

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private var interfaceProxy: InterfaceProxy<ITest>? = null
    private var test: ITest? = null


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
        findViewById<View>(R.id.call_remote).setOnClickListener {
            callCountPlus()
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
        interfaceProxy = InterfaceProxy<ITest>(ITest.KEY_CONNECT)
        val methodCallBinder = RemoteServicePresenter.getInstance()
            .queryBinderByCode<IMethodChannelBinder>(InterfaceIPCConst.BINDER_CODE_METHOD_CALL)
        interfaceProxy?.setMethodChannelBinder(methodCallBinder)
        test = interfaceProxy?.createProxy()
    }


}