package com.ysydhc.ipcscaffold.initiator;

import com.ysydhc.ipcscaffold.ProcessServicePresenter.BinderManager;

import java.util.ArrayList;

public class RemoteBinderInitiatorManager {

    private static volatile RemoteBinderInitiatorManager instance = null;

    private RemoteBinderInitiatorManager() {

    }

    public static RemoteBinderInitiatorManager getInstance() {
        if (instance == null) {
            synchronized (RemoteBinderInitiatorManager.class) {
                if (instance == null) {
                    instance = new RemoteBinderInitiatorManager();
                }
            }
        }
        return instance;
    }

    private final ArrayList<IIPCInitiatorTask> ipcInitiatorTaskList = new ArrayList<IIPCInitiatorTask>();

    public void notifyInit(BinderManager manager) {
        for (IIPCInitiatorTask task : ipcInitiatorTaskList) {
            task.init(manager);
        }
    }

    public void addTask(IIPCInitiatorTask task) {
        if (task == null) {
            return;
        }
        if (!ipcInitiatorTaskList.contains(task)) {
            ipcInitiatorTaskList.add(task);
        }
    }

    public void removeTask(IIPCInitiatorTask task) {
        if (task == null) {
            return;
        }
        ipcInitiatorTaskList.remove(task);
    }


}
