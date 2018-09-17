package com.lwy.downloadlib;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.lwy.downloadlib.bean.DownloadEntry;
import com.lwy.downloadlib.utils.FileUtils;
import com.lwy.downloadlib.utils.SharePrefUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

/**
 * Created by lwy on 2018/8/9.
 */

public class DataChanger extends Observable {

    private static DataChanger singleton = null;
    private HashMap<String, DownloadEntry> allDownloadEntryMap;

    private DataChanger() {
        allDownloadEntryMap = new HashMap<>();
    }

    public static DataChanger getInstance() {
        if (singleton == null) {
            synchronized (DataChanger.class) {
                if (singleton == null) {
                    singleton = new DataChanger();
                }
            }
        }
        return singleton;
    }

    public void postStatus(Context context, DownloadEntry entry) {
        if (!allDownloadEntryMap.containsKey(entry.id) && entry.status != DownloadEntry.DownloadStatus.cancel)
            allDownloadEntryMap.put(entry.id, entry);

        if (entry.status == DownloadEntry.DownloadStatus.cancel) {
            allDownloadEntryMap.remove(entry.id);
            if (FileUtils.isFileExist(entry.tempFilePath)) {
                FileUtils.deleteFile(entry.tempFilePath);
            } else {
                if (!TextUtils.isEmpty(entry.destDir) && !TextUtils.isEmpty(entry.name)) {
                    File file = new File(entry.destDir, entry.name);
                    if (FileUtils.isFileExist(file)) {
                        FileUtils.deleteFile(file);
                    }
                }
            }
            SharePrefUtil.saveString(context, entry.id, null);
        } else {
            SharePrefUtil.saveString(context, entry.id, new Gson().toJson(entry));
        }
        setChanged();
        notifyObservers(entry);
    }

    public DownloadEntry queryDownloadEntryByID(String entryID) {
        return allDownloadEntryMap.get(entryID);
    }

    public void addDownloadEntryByID(String entryID, DownloadEntry downloadEntry) {
        allDownloadEntryMap.put(entryID, downloadEntry);
    }

    public List<DownloadEntry> queryDownloadEntryAll() {
        List<DownloadEntry> list = null;
        if (allDownloadEntryMap != null && allDownloadEntryMap.size() > 0) {
            list = new ArrayList<>();
            for (Map.Entry<String, DownloadEntry> stringDownloadEntryEntry : allDownloadEntryMap.entrySet()) {
                list.add(stringDownloadEntryEntry.getValue());
            }
        }
        return list;
    }
}
