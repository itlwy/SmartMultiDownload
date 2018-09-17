package com.lwy.downloadlib.thread_task;

import com.lwy.downloadlib.Config;
import com.lwy.downloadlib.Constants;
import com.lwy.downloadlib.bean.DownloadEntry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lwy on 2018/8/9.
 */

public class DownloadThread implements Runnable {
    private final String mUrl;
    private final int mIndex;
    private final int mStartPos;
    private final int mEndPos;
    private boolean isSingleDownload;
    private DownloadThreadCallback mDownloadThreadCallback;
    private DownloadEntry.DownloadStatus status;
    private final String filePath;

    public void paused() {
        status = DownloadEntry.DownloadStatus.pause;
    }

    public void cancel() {
        status = DownloadEntry.DownloadStatus.cancel;
    }

    public void cancelByError() {
        status = DownloadEntry.DownloadStatus.pause;
        mDownloadThreadCallback.onError(mIndex, new RuntimeException(String.format("indexThread:%d,cancelByError", mIndex)));
    }

    public void setDownloadThreadCallback(DownloadThreadCallback downloadThreadCallback) {
        mDownloadThreadCallback = downloadThreadCallback;
    }

    public DownloadThread(String url, String tempFilePath, int index, int startPos, int endPos) {
        mUrl = url;
        mIndex = index;
        mStartPos = startPos;
        mEndPos = endPos;
        if (mStartPos == mEndPos)
            isSingleDownload = true;
        filePath = tempFilePath;
//        TraceUtil.d(String.format(Locale.CHINA, "index %d ,filePath:%s", index, tempFilePath));
    }

    @Override
    public void run() {
        status = DownloadEntry.DownloadStatus.downloading;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(mUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            if (!isSingleDownload)
                httpURLConnection.setRequestProperty("Range", "bytes=" + mStartPos + "-" + mEndPos);
            httpURLConnection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
            httpURLConnection.setReadTimeout(Constants.CONNECT_TIMEOUT);
            int code = httpURLConnection.getResponseCode();
            int contentLength = httpURLConnection.getContentLength();
            File file;
            RandomAccessFile raf;
            InputStream is;
            FileOutputStream fos;
            if (code == HttpURLConnection.HTTP_PARTIAL) {
                File dir = new File(Config.fileDir);
                if (!dir.exists())
                    dir.mkdirs();
                file = new File(filePath);
                raf = new RandomAccessFile(file, "rw");
                raf.seek(mStartPos);
                is = httpURLConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (status != DownloadEntry.DownloadStatus.downloading)
                        break;
                    raf.write(buffer, 0, len);
                    mDownloadThreadCallback.onProgress(mIndex, len);
                }
                raf.close();
                is.close();
                if (status == DownloadEntry.DownloadStatus.downloading)
                    mDownloadThreadCallback.onCompleted(mIndex);
            } else if (code == HttpURLConnection.HTTP_OK) {
                File dir = new File(Config.fileDir);
                if (!dir.exists())
                    dir.mkdirs();
                file = new File(filePath);
                fos = new FileOutputStream(file);
                is = httpURLConnection.getInputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = is.read(buffer)) != -1) {
                    if (status != DownloadEntry.DownloadStatus.downloading)
                        break;
                    fos.write(buffer, 0, len);
                    mDownloadThreadCallback.onProgress(mIndex, len);
                }
                fos.close();
                is.close();
                if (status == DownloadEntry.DownloadStatus.downloading)
                    mDownloadThreadCallback.onCompleted(mIndex);
            } else {
                mDownloadThreadCallback.onError(mIndex, new RuntimeException(String.format("httpCode:%d", code)));
            }
            if (status == DownloadEntry.DownloadStatus.pause) {
                mDownloadThreadCallback.onPaused(mIndex);
            }
            if (status == DownloadEntry.DownloadStatus.cancel) {
                mDownloadThreadCallback.onPaused(mIndex);
            }
        } catch (IOException e) {
            mDownloadThreadCallback.onError(mIndex, e);
        } finally {
            status = DownloadEntry.DownloadStatus.completed;
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    interface DownloadThreadCallback {

        void onProgress(int index, int progress);

        void onError(int index, Throwable e);

        void onPaused(int index);

        void onCompleted(int index);

        void onCancel(int index);
    }
}
