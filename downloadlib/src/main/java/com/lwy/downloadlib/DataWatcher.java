package com.lwy.downloadlib;

import com.lwy.downloadlib.bean.DownloadEntry;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by lwy on 2018/8/9.
 */

public abstract class DataWatcher implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof DownloadEntry){
            notifyUpdate((DownloadEntry)arg);
        }
    }

    protected abstract void notifyUpdate(DownloadEntry data);
}
