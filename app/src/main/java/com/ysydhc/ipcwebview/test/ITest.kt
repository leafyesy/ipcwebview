package com.ysydhc.ipcwebview.test

import com.ysydhc.interfaceipc.annotation.IpcMethodFlag

interface ITest {

    companion object {
        const val KEY_CONNECT = 100L
    }

    @IpcMethodFlag()
    fun countPlus(): Int

}