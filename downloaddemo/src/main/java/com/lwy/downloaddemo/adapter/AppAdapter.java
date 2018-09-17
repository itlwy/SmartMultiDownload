package com.lwy.downloaddemo.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.lwy.downloaddemo.R;
import com.lwy.downloadlib.bean.DownloadEntry;

import java.util.List;
import java.util.Locale;

/**
 * Created by lwy on 2018/8/20.
 */

public class AppAdapter extends RecyclerView.Adapter {

    private List<DownloadEntry> mDatas;
    private OnItemClickedListener mOnItemClickedListener;

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        mOnItemClickedListener = onItemClickedListener;
    }

    public void setDatas(List<DownloadEntry> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    public AppAdapter(List<DownloadEntry> datas) {
        mDatas = datas;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_app, parent, false);
        AppAdapterHolder holder = new AppAdapterHolder(view);
        if (mOnItemClickedListener != null)
            holder.setOnItemClickedListener(mOnItemClickedListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final DownloadEntry item = mDatas.get(position);
        final AppAdapterHolder appHolder = (AppAdapterHolder) holder;
        appHolder.bindView(item);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }


    public interface OnItemClickedListener {
        void onItemClicked(View containView, DownloadEntry data, int position);

        void onBtnClicked(View button, DownloadEntry data, int position);
    }

    public static class AppAdapterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final Button itemBtn;
        private final TextView itemNameTV;
        private final ImageView itemIV;
        private final ProgressBar itemPB;
        private final View containView;
        private final TextView itemInfoTV;
        private OnItemClickedListener onItemClickedListener;
        private DownloadEntry data;

        public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
            this.onItemClickedListener = onItemClickedListener;
            containView.setOnClickListener(this);
            itemBtn.setOnClickListener(this);
        }

        public AppAdapterHolder(View itemView) {
            super(itemView);
            containView = itemView;
            itemBtn = itemView.findViewById(R.id.item_btn);
            itemNameTV = itemView.findViewById(R.id.item_name_tv);
            itemInfoTV = itemView.findViewById(R.id.item_info_tv);
            itemIV = itemView.findViewById(R.id.item_iv);
            itemPB = itemView.findViewById(R.id.item_pb);
        }


        public void bindView(DownloadEntry item) {
            data = item;
            itemIV.setImageResource(R.drawable.ic_launcher_background);
            itemNameTV.setText(item.name);
            itemInfoTV.setText(String.format(Locale.CHINA, "%d/%d,%d%%,%s", data.currentLength, data.totalLength,
                    data.percent == -1 ? 0 : data.percent, data.status));
            if (data.percent >= 0 && data.percent <= 100) {
//                itemPB.setMin(0);
                itemPB.setMax(100);
            } else {
//                itemPB.setMin(0);
                itemPB.setMax(item.totalLength);
            }
            itemPB.setProgress(item.percent);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_btn:
                    int position = getAdapterPosition();
                    this.onItemClickedListener.onBtnClicked(v, data, position);
                    break;
            }
        }
    }
}
