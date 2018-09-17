package com.lwy.downloadlib;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.lwy.downloadlib.bean.DownloadEntry;
import com.lwy.downloadlib.utils.CryptoUtil;
import com.lwy.downloadlib.utils.TraceUtil;

import java.util.List;

/**
 * Created by lwy on 2018/8/9.
 */

public class DownloadManager {

    private static DownloadManager singleton = null;
    Config config;

    private DownloadManager() {

    }

    public static DownloadManager getInstance() {
        if (singleton == null) {
            synchronized (DownloadManager.class) {
                if (singleton == null) {
                    singleton = new DownloadManager();
                }
            }
        }
        return singleton;
    }

    public synchronized void init(Config config) {
        if (config == null) {
            throw new IllegalArgumentException("Config can not be initialized with null");
        } else {
            if (this.config == null) {
                TraceUtil.d("Initialize DownloadManager with config");
                this.config = config;
            } else {
                TraceUtil.w("Try to initialize DownloadManager which had already been initialized before");
            }

        }
    }

    private void checkConfiguration() {
        if (this.config == null) {
            throw new IllegalStateException("DownloadManager must be init with Config before using");
        }
    }

    public void add(Context context, DownloadEntry downloadEntry) {
        checkConfiguration();
        if (TextUtils.isEmpty(downloadEntry.url)) {
            throw new RuntimeException("DownloadEntry's url can't be null");
        }
        if (TextUtils.isEmpty(downloadEntry.id))
            downloadEntry.id = CryptoUtil.md5(downloadEntry.url);
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_ADD);
        context.startService(intent);
    }

    public void pause(Context context, DownloadEntry downloadEntry) {
        checkConfiguration();
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_PAUSE);
        context.startService(intent);
    }

    public void resume(Context context, DownloadEntry downloadEntry) {
        checkConfiguration();
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_RESUME);
        context.startService(intent);
    }

    public void cancel(Context context, DownloadEntry downloadEntry) {
        checkConfiguration();
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(Constants.KEY_DOWNLOAD_ENTRY, downloadEntry);
        intent.putExtra(Constants.KEY_DOWNLOAD_ACTION, Constants.KEY_DOWNLOAD_ACTION_CANCEL);
        context.startService(intent);
    }

    public void addObserver(DataWatcher watcher) {
        DataChanger.getInstance().addObserver(watcher);
    }

    public void removeObserver(DataWatcher watcher) {
        DataChanger.getInstance().deleteObserver(watcher);
    }

    public void queryDownloadEntryByID(String entryID) {
        DataChanger.getInstance().queryDownloadEntryByID(entryID);
    }

    public List<DownloadEntry> queryDownloadEntryAll() {
        return DataChanger.getInstance().queryDownloadEntryAll();
    }
}
