package com.ysydhc.ipcwebview

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysydhc.interfaceipc.InterfaceIPCInitiator
import com.ysydhc.ipcscaffold.IPCInitiator
import com.ysydhc.ipcwebview.test.ITest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.btn1).setOnClickListener {
            IPCInitiator.start(this)
        }
        findViewById<View>(R.id.call_remote).setOnClickListener {

        }
    }


}