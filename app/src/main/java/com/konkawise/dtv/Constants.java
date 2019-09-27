package com.konkawise.dtv;

import android.Manifest;

public class Constants {
    // support language
    public static final int LOCALE_TYPE_SYSTEM = 0x1001;
    public static final int LOCALE_TYPE_ITALIAN = 0x1002;
    public static final int LOCALE_TYPE_ENGLISH = 0x1003;
    public static final int LOCALE_TYPE_CHINESE = 0x1004;

    // usb receive type
    public static final int USB_TYPE_ATTACH = 0x2001;
    public static final int USB_TYPE_DETACH = 0x2002;

    // book type
    public static final int BOOK_TYPE_ADD = 0x3001; // 添加book
    public static final int BOOK_TYPE_EDIT = 0x3002; // 编辑book

    // book conflict type
    public static final int BOOK_CONFLICT_LIMIT = 0x4001; // book达到最大值32个，提示不能添加
    public static final int BOOK_CONFLICT_NONE = 0x4002; // book没冲突，正常添加
    public static final int BOOK_CONFLICT_ADD = 0x4003; // book冲突，需要先删除后添加
    public static final int BOOK_CONFLICT_REPLACE = 0x4004; // book冲突，需要替换

    // tp type
    public static final int TP_TYPE_ADD = 0x5001;
    public static final int TP_TYPE_EDIT = 0x5002;

    // msg callback id
    public static final int SCAN_CALLBACK_MSG_ID = 0x6001; // 频道扫描消息回调通道id
    public static final int BOOK_CALLBACK_MSG_ID = 0x6002; // book消息回调通道id
    public static final int LOCK_CALLBACK_MSG_ID = 0x6003; // 频道锁消息回调通道id
    public static final int TIME_CALLBACK_MSG_ID = 0x6004; // 系统时间消息回调通道id
    public static final int PVR_CALLBACK_MSG_ID = 0x6005; // PVR播放消息回调通道id
    public static final int EPG_CALLBACK_MSG_ID = 0x6006; // EPG消息回调通道id
    public static final int REFRESH_CHANNEL_CALLBACK_MSG_ID = 0x6007; // 提示更新搜台消息回调id

    // pvr type
    public static final int PVR_TYPE_TIMESHIFT = 0x7001;
    public static final int PVR_TYPE_RECORD = 0x7002;

    // dvb type
    public static final int DVB_SELECT_TYPE_INSTALLATION = 0x8001;
    public static final int DVB_SELECT_TYPE_SEARCH = 0x8002;

    // DiSEqC index
    public static final int DISEQC_A = 0;
    public static final int DISEQC_B = 1;
    public static final int DISEQC_C = 2;
    public static final int DISEQC_D = 3;

    public static final String SUBTITLE_NAME = "subtitleName";
    public static final String SUBTITLE_ORG_TYPE = "subtitleOrgType";
    public static final String SUBTITLE_TYPE = "subtitleType";

    // permission
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    public static final String STANDBY_PROPERTY = "persist.suspend.mode"; // 待机唤醒属性
    public static final String STANDBY_DEEP_RESTART = "deep_restart"; // 系统唤醒重启
    public static final String STANDBY_SMART_SUSPEND = "smart_suspend"; // 系统唤醒返回Launcher

    public static final String RECORD_FILE_TYPE = "ts";
    public static final String RECORD_CONFIG_FILE_TYPE = ".idx";

    public static final int T2_SATELLITE_INDEX = 0;

    public interface IntentKey {
        String INTENT_SATELLITE_INDEX = "satelliteIndex";
        String INTENT_SATELLITE_POSITION = "satellitePosition";
        String INTENT_SATELLITE_ACTIVITY = "satelliteActivity";
        String INTENT_EDIT_MANUAL_ACTIVITY = "editManualActivity";
        String INTENT_TPLIST_ACTIVITY = "tpListActivity";
        String INTENT_T2_MANUAL_SEARCH_ACTIVITY = "t2ManualSearchActivity";
        String INTENT_T2_AUTO_SEARCH = "installationT2Activity";
        String INTENT_TP_NAME = "tpName";
        String INTENT_LNB = "lnb";
        String ITENT_DISEQC = "DiSEqC";
        String INTENT_MOTOR_TYPE = "motorType";
        String INTENT_FREQ = "freq";
        String INTENT_SYMBOL = "symbol";
        String INTENT_QAM = "qam";
        String INTENT_LONGITUDE = "longitude";

        String INTENT_BOOK_TYPE = "bookType";
        String INTENT_BOOK_SECONDS = "bookSeconds";
        String INTENT_BOOK_SERVICEID = "serviceid";
        String INTENT_BOOK_TSID = "tsid";
        String INTENT_BOOK_SAT = "sat";

        String INTENT_BOOK_UPDATE = "bookUpdate";
        String INTENT_CURRENT_TP = "currentTp";

        String INTENT_TIMESHIFT_RECORD_FROM = "from";
        String INTENT_TIMESHIFT_TIME = "time";
        String INTENT_TIMESHIFT_PROGNUM = "progNum";
        String INTENT_RECORD_POSITION = "recordPosition";
    }

    public interface RequestCode {
        int REQUEST_CODE_EPG_BOOK = 1;

        int REQUEST_CODE_MOTOR = 2;
    }

    public interface PrefsKey {
        String LOCALE_TYPE = "localeType";

        String SAVE_CHANNEL = "channel";
    }
}
