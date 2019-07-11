package com.konkawise.dtv.bean;

import android.net.Uri;

public class UsbInfo {
    // uuid
    public String uuid;
    // uri
    public Uri uri;
    // u盘类型
    public String fsType;
    // u盘名称
    public String fsLabel;
    // u盘路径
    public String path;
    // u盘路径
    public String internalPath;
    // 已格式化显示的可用空间大小
    public String availableSize;
    // 已格式化显示的总空间大小
    public String totalSize;

    @Override
    public String toString() {
        return "UsbInfo{" +
                "uuid='" + uuid + '\'' +
                ", uri=" + uri +
                ", fsType='" + fsType + '\'' +
                ", fsLabel='" + fsLabel + '\'' +
                ", path='" + path + '\'' +
                ", internalPath='" + internalPath + '\'' +
                ", availableSize='" + availableSize + '\'' +
                ", totalSize='" + totalSize + '\'' +
                '}';
    }
}
