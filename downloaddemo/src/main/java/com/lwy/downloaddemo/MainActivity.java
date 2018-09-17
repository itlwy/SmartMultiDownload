package com.lwy.downloaddemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.lwy.downloaddemo.adapter.AppAdapter;
import com.lwy.downloadlib.Config;
import com.lwy.downloadlib.DataWatcher;
import com.lwy.downloadlib.DownloadManager;
import com.lwy.downloadlib.bean.DownloadEntry;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AppAdapter.OnItemClickedListener {
    private static String sPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/";
    private RecyclerView mRcv;
    private DataWatcher mDataWatcher = new DataWatcher() {
        @Override
        protected void notifyUpdate(DownloadEntry data) {
            for (int i = 0; i < mDatas.size(); i++) {
                DownloadEntry item = mDatas.get(i);
                if (item.id.equals(data.id)) {
                    mDatas.set(i, data);
                    break;
                }
            }
            mAdapter.notifyDataSetChanged();
//            if (mDownloadEntry != null && mDownloadEntry.id.equals(data.id)) {
//                if (data.status == DownloadEntry.DownloadStatus.cancel) {
//                    mDownloadEntry = new DownloadEntry(url, "传奇.apk",
//                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/");
//                } else
//                    mDownloadEntry = data;
//                String info = String.format(Locale.CHINA, "下载任务ID:%s,下载进度:%d,下载状态:%s,总大小:%d",
//                        mDownloadEntry.id, mDownloadEntry.currentLength, mDownloadEntry.status, mDownloadEntry.totalLength);
//                mInfoTv.setText(info);
//            } else if (mDownloadEntry2 != null && mDownloadEntry2.id.equals(data.id)) {
//                if (data.status == DownloadEntry.DownloadStatus.cancel) {
//                    mDownloadEntry2 = new DownloadEntry(url, "西瓜视频.apk",
//                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/");
//                } else
//                    mDownloadEntry2 = data;
//                String info = String.format(Locale.CHINA, "下载任务ID:%s,下载进度:%d,下载状态:%s,总大小:%d",
//                        mDownloadEntry2.id, mDownloadEntry2.currentLength, mDownloadEntry2.status, mDownloadEntry2.totalLength);
//                mInfoTv2.setText(info);
//            }

        }
    };
    private ArrayList<DownloadEntry> mDatas;
    private AppAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRcv = (RecyclerView) findViewById(R.id.rcv);
        DownloadManager.getInstance().init(new Config.Builder().isDebug(true).
                setFileDir(Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/").build());
        initView();

    }

    private void initView() {
        initDatas();
        mAdapter = new AppAdapter(mDatas);
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRcv.setLayoutManager(manager);
        mRcv.setAdapter(mAdapter);
        mAdapter.setOnItemClickedListener(this);
    }

    private void initDatas() {
        mDatas = new ArrayList<>();
        mDatas.add(new DownloadEntry("http://yapkwww.cdn.anzhi.com/data4/apk/201807/13/40e4011e4302717b1a0afcfb08581f2f.apk",
                "传奇.apk", sPath));
        mDatas.add(new DownloadEntry("http://yapkwww.cdn.anzhi.com/data4/apk/201808/07/6e95a178757f05e8ca56608d923e5a80_32638800.apk",
                "西瓜视频.apk", sPath));
        mDatas.add(new DownloadEntry("http://yapkwww.cdn.anzhi.com/data1/apk/201806/01/064bf9839646bb75a049e11c28aaed9c_19917300.apk",
                "熊出没大冒险.apk", sPath));
        mDatas.add(new DownloadEntry("http://yapkwww.cdn.anzhi.com/data4/apk/201808/15/95d32775b8f85baf945305f20d57e0d5_22370900.apk",
                "今日头条.apk", sPath));

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDataWatcher != null)
            DownloadManager.getInstance().addObserver(mDataWatcher);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDataWatcher != null)
            DownloadManager.getInstance().removeObserver(mDataWatcher);
    }


    @Override
    public void onItemClicked(View containView, DownloadEntry data, int position) {

    }

    @Override
    public void onBtnClicked(View button, DownloadEntry data, int position) {

//        if (mDownloadEntry == null) {
//            mDownloadEntry = new DownloadEntry(url, "传奇.apk",
//                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/");
//        } else
//            mDownloadEntry.reset();
        if (data.status == DownloadEntry.DownloadStatus.error)
            data.reset();
        if (data.status == null || data.status == DownloadEntry.DownloadStatus.idle) {
            ((Button) button).setText("pause");
            DownloadManager.getInstance().add(this, data);
        } else if (data.status == DownloadEntry.DownloadStatus.pause) {
            ((Button) button).setText("pause");
            DownloadManager.getInstance().resume(this, data);
        } else if (data.status == DownloadEntry.DownloadStatus.downloading) {
            ((Button) button).setText("resume");
            DownloadManager.getInstance().pause(this, data);
        }

    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.pause_btn:
//                if (mDownloadEntry != null)
//                    DownloadManager.getInstance().pause(this, mDownloadEntry);
//                break;
//            case R.id.resume_btn:
////                if (mDownloadEntry != null)
//                if (mDownloadEntry == null) {
//                    mDownloadEntry = new DownloadEntry(url, "传奇.apk",
//                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/");
//                }
//                DownloadManager.getInstance().resume(this, mDownloadEntry);
//                break;
//            case R.id.download2_btn:
//                if (mDownloadEntry2 == null) {
//                    mDownloadEntry2 = new DownloadEntry(url2, "西瓜视频.apk",
//                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/");
//                } else
//                    mDownloadEntry2.reset();
//                DownloadManager.getInstance().add(this, mDownloadEntry2);
//                break;
//            case R.id.download_btn:
//                if (mDownloadEntry == null) {
//                    mDownloadEntry = new DownloadEntry(url, "传奇.apk",
//                            Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/");
//                } else
//                    mDownloadEntry.reset();
//                DownloadManager.getInstance().add(this, mDownloadEntry);
//                break;
//            case R.id.cancel_btn:
//                if (mDownloadEntry != null)
//                    DownloadManager.getInstance().cancel(this, mDownloadEntry);
//                break;
//        }
//    }
}
