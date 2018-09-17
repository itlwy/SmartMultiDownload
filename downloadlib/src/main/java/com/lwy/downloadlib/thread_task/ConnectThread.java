package com.lwy.downloadlib.thread_task;

import com.lwy.downloadlib.Constants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lwy on 2018/8/11.
 */

public class ConnectThread implements Runnable {


    private final String mUrl;
    private ConnectThreadCallback mConnectThreadCallback;
    private boolean isRunning;

    public void setConnectThreadCallback(ConnectThreadCallback connectThreadCallback) {
        mConnectThreadCallback = connectThreadCallback;
    }

    public ConnectThread(String url) {
        mUrl = url;
    }

    @Override
    public void run() {
        isRunning = true;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(mUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
//            httpURLConnection.setRequestProperty("Range", "bytes=0-" + Integer.MAX_VALUE);
            httpURLConnection.setConnectTimeout(Constants.CONNECT_TIMEOUT);
            httpURLConnection.setReadTimeout(Constants.CONNECT_TIMEOUT);
            int code = httpURLConnection.getResponseCode();
            int contentLength = httpURLConnection.getContentLength();
            boolean isSupportRange = false;
//            if (code == HttpURLConnection.HTTP_PARTIAL) {
//                isSupportRange = true;
//            } else
            if (code == HttpURLConnection.HTTP_OK) {
                String eTag = "";
                if ("bytes".equals(httpURLConnection.getHeaderField("Accept-Ranges"))) {
                    eTag = httpURLConnection.getHeaderField("ETag");
                    isSupportRange = true;
                } else
                    isSupportRange = false;

                mConnectThreadCallback.onConnected(isSupportRange, contentLength, eTag);
            } else {
                mConnectThreadCallback.onConnectError(new RuntimeException(String.format("httpCode:%d", code)));
            }
        } catch (IOException e) {
            mConnectThreadCallback.onConnectError(e);
        } finally {
            isRunning = false;
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void cancel() {
        // TODO: 2018/8/16 暂停连接线程
//        Thread.currentThread().interrupt();
    }

    interface ConnectThreadCallback {
        void onConnectError(Throwable e);

        void onConnected(boolean isSupportRange, int contentLength, String eTag);
    }
}
