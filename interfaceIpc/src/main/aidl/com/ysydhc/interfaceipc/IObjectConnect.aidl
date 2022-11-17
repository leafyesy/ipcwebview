// IObjectConnect.aidl
package com.ysydhc.interfaceipc;

import com.ysydhc.interfaceipc.model.ConnectCell;

interface IObjectConnect {

    boolean connect(in ConnectCell cell);

}