package com.lwy.downloadlib;

import android.os.Environment;

/**
 * Created by lwy on 2018/8/12.
 */

public class Config {
    public static boolean isDebug = false;
    public static String fileDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/downloadLib/";

    boolean isAutoRecover ;
    private Config(Builder builder) {
        isDebug = builder.isDebug;
        fileDir = builder.fileDir;
    }

    public static class Builder {

        private boolean isDebug;
        private String fileDir;

        public Builder() {

        }

        public Builder isDebug(boolean flag) {
            isDebug = flag;
            return this;
        }

        public Builder setFileDir(String fileDir) {
            this.fileDir = fileDir;
            return this;
        }

        public Config build() {
            return new Config(this);
        }
    }
}
