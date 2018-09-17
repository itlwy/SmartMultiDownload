package com.lwy.downloadlib.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.lwy.downloadlib.Config;
import com.lwy.downloadlib.utils.CryptoUtil;
import com.lwy.downloadlib.utils.FileUtils;
import com.lwy.downloadlib.utils.TraceUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by lwy on 2018/8/9.
 */

public class DownloadEntry implements Parcelable {
    public String id;
    public String name;
    public String url;
    public String destDir;
    public String tempFilePath;
    public DownloadStatus status;

    public int totalLength;
    public int currentLength;
    public int percent = -1;
    public boolean isSupportRange;
    public String eTag;  // http's ETag

    public HashMap<Integer, Integer> ranges;

    @Override
    public String toString() {
        return status.toString();
    }

    public DownloadEntry(String url, String name, String destDir) {
        if (TextUtils.isEmpty(url))
            throw new RuntimeException("url can't be empty");
        this.url = url;
        this.id = CryptoUtil.md5(url);
        this.name = name;
        this.destDir = destDir;
        tempFilePath = Config.fileDir + CryptoUtil.md5(url);
        TraceUtil.d(String.format(Locale.CHINA, "new DownloadEntry:%s", tempFilePath));
    }

    public void reset() {
        totalLength = 0;
        currentLength = 0;
        percent = -1;
        isSupportRange = false;
        eTag = null;
        if (ranges != null)
            ranges.clear();
        status = DownloadStatus.idle;
        if (!TextUtils.isEmpty(tempFilePath) && FileUtils.isFileExist(tempFilePath)) {
            FileUtils.deleteFile(tempFilePath);
        } else if (!TextUtils.isEmpty(destDir) && !TextUtils.isEmpty(name)) {
            File file = new File(destDir, name);
            if (FileUtils.isFileExist(file)) {
                FileUtils.deleteFile(file);
            }
        }
    }

    public enum DownloadStatus {idle, waiting, downloading, pause, resume, cancel, connecting, error, completed}

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DownloadEntry)) {
            return false;
        }
        DownloadEntry other = (DownloadEntry) obj;
        return id.hashCode() == other.id.hashCode();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.url);
        dest.writeString(this.destDir);
        dest.writeString(this.tempFilePath);
        dest.writeInt(this.status == null ? -1 : this.status.ordinal());
        dest.writeInt(this.totalLength);
        dest.writeInt(this.currentLength);
        dest.writeInt(this.percent);
        dest.writeByte(this.isSupportRange ? (byte) 1 : (byte) 0);
        dest.writeSerializable(this.ranges);
    }

    protected DownloadEntry(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.url = in.readString();
        this.destDir = in.readString();
        this.tempFilePath = in.readString();
        int tmpStatus = in.readInt();
        this.status = tmpStatus == -1 ? null : DownloadStatus.values()[tmpStatus];
        this.totalLength = in.readInt();
        this.currentLength = in.readInt();
        this.percent = in.readInt();
        this.isSupportRange = in.readByte() != 0;
        this.ranges = (HashMap<Integer, Integer>) in.readSerializable();
    }

    public static final Parcelable.Creator<DownloadEntry> CREATOR = new Parcelable.Creator<DownloadEntry>() {
        @Override
        public DownloadEntry createFromParcel(Parcel source) {
            return new DownloadEntry(source);
        }

        @Override
        public DownloadEntry[] newArray(int size) {
            return new DownloadEntry[size];
        }
    };
}
