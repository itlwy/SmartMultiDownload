package com.lwy.downloadlib.thread_task;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.lwy.downloadlib.Config;
import com.lwy.downloadlib.Constants;
import com.lwy.downloadlib.bean.DownloadEntry;
import com.lwy.downloadlib.utils.TraceUtil;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * Created by lwy on 2018/8/9.
 */

public class DownloadTask implements DownloadThread.DownloadThreadCallback, ConnectThread.ConnectThreadCallback {
    private final DownloadEntry mDownloadEntry;
    private final ExecutorService mExecutor;
    private Handler mHandler;
    private boolean isPause;
    private boolean isCancel;
    private TaskCallbak mTaskCallbak;
    private DownloadThread[] mDownloadThreads;
    private ConnectThread connectThread;
    private DownloadEntry.DownloadStatus threadStatus[];
    private long lastUpdateTime;

    public void setTaskCallbak(TaskCallbak taskCallbak) {
        mTaskCallbak = taskCallbak;
    }


    public DownloadTask(@NonNull DownloadEntry downloadEntry, Handler handler, ExecutorService executor) {
        mHandler = handler;
        mExecutor = executor;
        mDownloadEntry = downloadEntry;
    }

    public void pause() {
        isPause = true;
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (mDownloadThreads != null) {
            for (DownloadThread downloadThread : mDownloadThreads) {
                downloadThread.paused();
            }
        }
    }

    public void resume() {
        isPause = false;
    }

    public void cancel() {
        isCancel = true;
        if (connectThread != null && connectThread.isRunning()) {
            connectThread.cancel();
        }
        if (mDownloadThreads != null) {
            for (DownloadThread downloadThread : mDownloadThreads) {
                downloadThread.cancel();
            }
        }
    }

    public void start() {
        if (mDownloadEntry.totalLength == 0) {
            connectThread = new ConnectThread(mDownloadEntry.url);
            connectThread.setConnectThreadCallback(this);
            mExecutor.execute(connectThread);
            mDownloadEntry.status = DownloadEntry.DownloadStatus.connecting;
        } else {
            mDownloadEntry.status = DownloadEntry.DownloadStatus.downloading;
            if (mDownloadEntry.isSupportRange) {
                startMultiDownload();
            } else {
                startSingleDownload();
            }
        }
        notifyUpdate(mDownloadEntry);
    }

    private void notifyUpdate(DownloadEntry downloadEntry) {
        Message msg = Message.obtain();
        msg.obj = downloadEntry;
        mHandler.sendMessage(msg);
    }

    @Override
    public synchronized void onProgress(int index, int progress) {
        if (mDownloadEntry.ranges.get(index) == null)
            mDownloadEntry.ranges.put(index, 0);
        mDownloadEntry.ranges.put(index, mDownloadEntry.ranges.get(index) + progress);
        mDownloadEntry.currentLength += progress;
        if (mDownloadEntry.totalLength > -1) {
            int percent = (int) (mDownloadEntry.currentLength * 100.0 / mDownloadEntry.totalLength);
            mDownloadEntry.percent = percent;
        }

        long time = System.currentTimeMillis();
//        TraceUtil.d(String.format("System.currentTimeMillis() %d,time - lastUpdateTime = %d", time, time - lastUpdateTime));
        if (time - lastUpdateTime > Constants.UPDATEINFO_INTERVAL) {
            lastUpdateTime = time;
            notifyUpdate(mDownloadEntry);
            TraceUtil.d(String.format("indexThread:%d ,progress:%d", index, progress));
        }

    }


    @Override
    public synchronized void onError(int index, Throwable e) {
        TraceUtil.d("onError index:" + index);
        threadStatus[index] = DownloadEntry.DownloadStatus.error;
        boolean isAllError = true;
        for (int i = 0; i < threadStatus.length; i++) {
            if (threadStatus[i] != DownloadEntry.DownloadStatus.error) {
                isAllError = false;
                if (mDownloadThreads[i] != null)
                    mDownloadThreads[i].cancelByError();
            }
        }
        if (isAllError) {
            mTaskCallbak.onError(e, mDownloadEntry);
            mDownloadEntry.status = DownloadEntry.DownloadStatus.error;
            TraceUtil.d(String.format("indexThread:%d ,onError:%s", index, e.getMessage()));
        }

    }

    @Override
    public synchronized void onPaused(int index) {
        TraceUtil.d("onPaused index:" + index);
        threadStatus[index] = DownloadEntry.DownloadStatus.pause;
        for (int i = 0; i < threadStatus.length; i++) {
            if (threadStatus[i] != DownloadEntry.DownloadStatus.pause) {
                return;
            }
        }
        mDownloadEntry.status = DownloadEntry.DownloadStatus.pause;
        notifyUpdate(mDownloadEntry);
        TraceUtil.d(String.format("indexThread:%d ,onPaused", index));
    }

    @Override
    public synchronized void onCompleted(int index) {
        TraceUtil.d("onCompleted index:" + index);
        threadStatus[index] = DownloadEntry.DownloadStatus.completed;
        for (int i = 0; i < threadStatus.length; i++) {
            if (threadStatus[i] != DownloadEntry.DownloadStatus.completed) {
                return;
            }
        }
        mDownloadEntry.status = DownloadEntry.DownloadStatus.completed;
        if (!TextUtils.isEmpty(mDownloadEntry.destDir) && !TextUtils.isEmpty(mDownloadEntry.name)) {
            File file = new File(Config.fileDir + mDownloadEntry.id);
            File newFile = new File(mDownloadEntry.destDir, mDownloadEntry.name);
            file.renameTo(newFile);
        }
        mTaskCallbak.onCompleted(this, mDownloadEntry);
        TraceUtil.d(String.format("indexThread:%d ,onCompleted", index));
    }

    @Override
    public void onCancel(int index) {
        TraceUtil.d("onCancel index:" + index);
        threadStatus[index] = DownloadEntry.DownloadStatus.cancel;
        for (int i = 0; i < threadStatus.length; i++) {
            if (threadStatus[i] != DownloadEntry.DownloadStatus.cancel) {
                return;
            }
        }
        mDownloadEntry.status = DownloadEntry.DownloadStatus.cancel;
        notifyUpdate(mDownloadEntry);
        TraceUtil.d(String.format("indexThread:%d ,onCancel", index));
    }

    @Override
    public void onConnectError(Throwable e) {
        mDownloadEntry.status = DownloadEntry.DownloadStatus.error;
        mTaskCallbak.onError(e, mDownloadEntry);
        TraceUtil.d(e.toString());
    }

    @Override
    public void onConnected(boolean isSupportRange, int contentLength, String eTag) {
        TraceUtil.d(String.format("连接成功,isSupportRange:" + isSupportRange + " ,contentLength:" + contentLength));
        if (!TextUtils.isEmpty(mDownloadEntry.eTag) && !mDownloadEntry.eTag.equals(eTag)) {
            // 防止断点下载时，服务器资源已发生变化
            mDownloadEntry.reset();
        }
        mDownloadEntry.isSupportRange = isSupportRange;
        mDownloadEntry.totalLength = contentLength;
        mDownloadEntry.eTag = eTag;
        if (isSupportRange && contentLength > -1) {
            startMultiDownload();
        } else {
            startSingleDownload();
        }
    }

    private void startSingleDownload() {
        mDownloadThreads = new DownloadThread[1];
        threadStatus = new DownloadEntry.DownloadStatus[1];
        // single thread to download
        mDownloadThreads[0] = new DownloadThread(mDownloadEntry.url, mDownloadEntry.tempFilePath, 0, 0, 0);
        threadStatus[0] = DownloadEntry.DownloadStatus.downloading;
        mDownloadThreads[0].setDownloadThreadCallback(this);
        mExecutor.execute(mDownloadThreads[0]);
    }

    private void startMultiDownload() {
        int block = mDownloadEntry.totalLength / Constants.MASK_DOWNLOAD_TASKS;
        int startPos = 0;
        int endPos = 0;
        if (mDownloadEntry.ranges == null) {
            mDownloadEntry.ranges = new HashMap<>();
        }
        mDownloadThreads = new DownloadThread[Constants.MASK_DOWNLOAD_TASKS];
        threadStatus = new DownloadEntry.DownloadStatus[Constants.MASK_DOWNLOAD_TASKS];
        for (int i = 0; i < Constants.MASK_DOWNLOAD_TASKS; i++) {
            Integer oldStart = mDownloadEntry.ranges.get(i);
            if (oldStart == null)
                oldStart = 0;
            startPos = i * block + oldStart;
            if (i == Constants.MASK_DOWNLOAD_TASKS - 1) {
                endPos = mDownloadEntry.totalLength;
            } else {
                endPos = (i + 1) * block - 1;
            }
            if (startPos == endPos)
                continue;
            mDownloadThreads[i] = new DownloadThread(mDownloadEntry.url, mDownloadEntry.tempFilePath, i, startPos, endPos);
            threadStatus[i] = DownloadEntry.DownloadStatus.downloading;
            mDownloadThreads[i].setDownloadThreadCallback(this);
            mExecutor.execute(mDownloadThreads[i]);
        }
        mDownloadEntry.status = DownloadEntry.DownloadStatus.downloading;

    }

    public interface TaskCallbak {

        void onError(Throwable e, DownloadEntry entry);

        void onCompleted(DownloadTask task, DownloadEntry entry);

    }


}
