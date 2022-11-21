package com.ysydhc.ipcwebview.test

import com.ysydhc.interfaceipc.annotation.CallbackParam
import com.ysydhc.interfaceipc.annotation.IpcMethodFlag

interface ITest {

    companion object {
        const val KEY_CONNECT = 100L
    }

    @IpcMethodFlag()
    fun countPlus(): Int

    @IpcMethodFlag(IpcMethodFlag.KEY_LOCAL_CALLBACK_SET)
    fun setListener(@CallbackParam listener: TestListener)

}

interface TestListener {

    fun onConnect(count: Int): Boolean

}