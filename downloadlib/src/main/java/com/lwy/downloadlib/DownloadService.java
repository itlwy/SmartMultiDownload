package com.lwy.downloadlib;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.lwy.downloadlib.bean.DownloadEntry;
import com.lwy.downloadlib.thread_task.DownloadTask;
import com.lwy.downloadlib.utils.SharePrefUtil;
import com.lwy.downloadlib.utils.TraceUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by lwy on 2018/8/9.
 */

public class DownloadService extends Service implements DownloadTask.TaskCallbak {
    private ConcurrentHashMap<String, DownloadTask> taskMap = new ConcurrentHashMap();

    private LinkedBlockingQueue<DownloadEntry> mWaitingQueue = new LinkedBlockingQueue<>();

    private ExecutorService mExecutor = Executors.newCachedThreadPool();

    private Handler mHandler;


    static class ServiceHandler extends Handler {

        private final WeakReference<DownloadService> mDownloadService;

        public ServiceHandler(DownloadService downloadService) {
            mDownloadService = new WeakReference<>(downloadService);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadEntry downloadEntry = (DownloadEntry) msg.obj;
            switch (downloadEntry.status) {
                case cancel:
                case completed:
                case pause:
                    if (mDownloadService.get() != null) {
//                        mDownloadService.get().completeDownload(downloadEntry);
                        mDownloadService.get().checkNext(downloadEntry);
                    }
                    break;
            }
            if (mDownloadService.get() != null)
                DataChanger.getInstance().postStatus(mDownloadService.get().getApplicationContext(), downloadEntry);
        }

    }

    private static final String TAG = "DownloadService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new ServiceHandler(this);
        // 取出sp存储的Entry
        List<DownloadEntry> entrys = new ArrayList<>();
        Map<String, ?> map = SharePrefUtil.getAll(getApplicationContext());
        Set<? extends Map.Entry<String, ?>> entrySet = map.entrySet();
        Gson gson = new Gson();
        for (Map.Entry<String, ?> stringEntry : entrySet) {
            String value = (String) stringEntry.getValue();
            DownloadEntry entry = gson.fromJson(value, DownloadEntry.class);
            // 重新创建服务后，所有的任务都要处于静止状态，然后再一次加入下载集合中
            if (entry.status == DownloadEntry.DownloadStatus.downloading || entry.status == DownloadEntry.DownloadStatus.waiting) {
                entry.status = DownloadEntry.DownloadStatus.pause;
                // TODO add a config if need to recover download
                // 加入到下载队列中
                if (DownloadManager.getInstance().config != null && DownloadManager.getInstance().config.isAutoRecover) {
                    addDownload(entry);
                }

            }
            DataChanger.getInstance().addDownloadEntryByID(entry.id, entry);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            DownloadEntry entry = intent.getParcelableExtra(Constants.KEY_DOWNLOAD_ENTRY);
            int action = intent.getIntExtra(Constants.KEY_DOWNLOAD_ACTION, -1);
            doAction(action, entry);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void doAction(int action, DownloadEntry entry) {
        DownloadEntry realEntry = DataChanger.getInstance().queryDownloadEntryByID(entry.id);
        if (realEntry != null) {
            entry = realEntry;
        }
        if (action == Constants.KEY_DOWNLOAD_ACTION_ADD) {
            if (taskMap.containsKey(entry.id) || mWaitingQueue.contains(entry))
                return;
            entry.reset();
            addDownload(entry);
        } else if (action == Constants.KEY_DOWNLOAD_ACTION_PAUSE) {
            pauseDownload(entry);
        } else if (action == Constants.KEY_DOWNLOAD_ACTION_CANCEL) {
            cancelDownload(entry);
        } else if (action == Constants.KEY_DOWNLOAD_ACTION_RESUME) {
            resumeDownload(entry);
        }
    }

    private void addDownload(DownloadEntry entry) {
//        if (entry.status != DownloadEntry.DownloadStatus.pause) {
//            entry.reset();
//        }
        TraceUtil.d(String.format(Locale.CHINA, "entry id: %s: addDownload,status:%s", entry.id, entry.status));
        if (taskMap.keySet().size() > Constants.MASK_DOWNLOAD_TASKS) {
            mWaitingQueue.offer(entry);
            entry.status = DownloadEntry.DownloadStatus.waiting;
            DataChanger.getInstance().postStatus(getApplicationContext(), entry);
        } else {
            startDownload(entry);
        }
    }

    private void resumeDownload(DownloadEntry entry) {
        if (entry.status == DownloadEntry.DownloadStatus.pause) {
            addDownload(entry);
        }
    }

    private void cancelDownload(DownloadEntry entry) {
        DownloadTask task = taskMap.remove(entry.id);
        if (task != null) {
            task.cancel();
        } else {
            mWaitingQueue.remove(entry);
            entry.status = DownloadEntry.DownloadStatus.cancel;
            DataChanger.getInstance().postStatus(getApplicationContext(), entry);
        }
    }

    private void pauseDownload(DownloadEntry entry) {
        DownloadTask task = taskMap.remove(entry.id);
        if (task != null) {
            task.pause();
        } else {
            mWaitingQueue.remove(entry);
            if (entry.status == DownloadEntry.DownloadStatus.downloading) {
                entry.status = DownloadEntry.DownloadStatus.pause;
                DataChanger.getInstance().postStatus(getApplicationContext(), entry);
            }
        }
    }

//    private void completeDownload(DownloadEntry entry) {
//        DownloadTask task = taskMap.remove(entry.id);
//        if (task != null) {
//            task.pause();
//        } else {
//            entry.status = DownloadEntry.DownloadStatus.completed;
//        }
//
//    }

    private void startDownload(DownloadEntry entry) {
        TraceUtil.d(String.format(Locale.CHINA, "entry id: %s: startDownload,status:%s", entry.id, entry.status));
        DownloadTask task = new DownloadTask(entry, mHandler, mExecutor);
        task.setTaskCallbak(this);
        taskMap.put(entry.id, task);
        task.start();
    }

    private void checkNext(DownloadEntry downloadEntry) {
        DownloadEntry newEntry = mWaitingQueue.poll();
        if (newEntry != null) {
            startDownload(newEntry);
        }
    }

    private void sendMsg(DownloadEntry entry) {
        Message msg = Message.obtain();
        msg.obj = entry;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onError(Throwable e, DownloadEntry entry) {
        taskMap.remove(entry.id);
        sendMsg(entry);
        TraceUtil.d(e.toString());
    }


    @Override
    public void onCompleted(DownloadTask task, DownloadEntry entry) {
        taskMap.remove(entry.id);
        sendMsg(entry);
    }


}
