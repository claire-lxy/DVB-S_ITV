package com.konkawise.dtv;

import android.Manifest;

public class Constants {

    public interface LocaleType {
        int SYSTEM = 0;
        int ITALIAN = 1;
        int ENGLISH = 2;
        int CHINESE = 3;
    }

    public interface UsbType {
        int ATTACH = 0;
        int DETACH = 1;
    }

    public interface BookType {
        int ADD = 0; // 添加book
        int EDIT = 1; // 编辑book
    }

    public interface BookConflictType {
        int LIMIT = 0; // book达到最大值32个，提示不能添加
        int NONE = 1; // book没冲突，正常添加
        int ADD = 2; // book冲突，需要先删除后添加
        int REPLACE = 3; // book冲突，需要替换
    }

    public interface TpType {
        int ADD = 0;
        int EDIT = 1;
    }

    public interface MsgCallbackId {
        int SCAN = 0; // 频道扫描消息回调通道id
        int BOOK = 1; // book消息回调通道id
        int LOCK = 2; // 频道锁消息回调通道id
        int TIME = 3; // 系统时间消息回调通道id
        int PVR = 4; // PVR播放消息回调通道id
        int EPG = 5; // EPG消息回调通道id
        int REFRESH_CHANNEL = 6; // 提示更新搜台消息回调id
    }

    public interface StepType {
        int NONE = 0;
        int PLUS = 1;
        int MINUS = 2;
    }

    // DiSEqC常量值是和底层约定的，不可修改
    public interface DiSEqCPortIndex {
        int DISEQC_A = 0;
        int DISEQC_B = 1;
        int DISEQC_C = 2;
        int DISEQC_D = 3;
    }

    public interface Permissions {
        String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
        String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    }

    public static final String STANDBY_PROPERTY = "persist.suspend.mode"; // 待机唤醒属性

    public interface StandbyProperty {
        String DEEP_RESTART = "deep_restart"; // 系统唤醒重启
        String SMART_SUSPEND = "smart_suspend"; // 系统唤醒返回Launcher
    }

    public interface MotorType {
        int OFF = 0;
        int DISEQC = 1;
        int USALS = 2;
    }

    public interface SatIndex {
        int S_START_INDEX = 0; // 获取S卫星列表时的开始循环索引
        int ALL_START_INDEX = -2; // 获取所有卫星列表时的开始循环索引
        int EXCLUDE_SAT_NUM = -2; // 排除S后的其他卫星数量，目前只有T和C
        int T2 = -2;
        int CABLE = -1;
        int ALL_SAT_INDEX = -1000;
    }

    public static final String SUBTITLE_NAME = "subtitleName";
    public static final String SUBTITLE_ORG_TYPE = "subtitleOrgType";
    public static final String SUBTITLE_TYPE = "subtitleType";

    public static final String RECORD_FILE_TYPE = "ts";
    public static final String RECORD_CONFIG_FILE_TYPE = ".idx";

    public interface IntentKey {
        String INTENT_SATELLITE_INDEX = "satelliteIndex";
        String INTENT_SATELLITE_POSITION = "satellitePosition";
        String INTENT_TP_NAME = "tpName";
        String INTENT_LNB = "lnb";
        String ITENT_DISEQC = "DiSEqC";
        String INTENT_MOTOR_TYPE = "motorType";
        String INTENT_FREQ = "freq";
        String INTENT_SYMBOL = "symbol";
        String INTENT_QAM = "qam";
        String INTENT_LONGITUDE = "longitude";
        String INTENT_SEARCH_TYPE = "searchType";

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

    public interface IntentValue {
        int SEARCH_TYPE_SATELLITE = 1;
        int SEARCH_TYPE_TPLISTING = 2;
        int SEARCH_TYPE_EDITMANUAL = 3;
        int SEARCH_TYPE_T2MANUAL = 4;
        int SEARCH_TYPE_T2AUTO = 5;
    }

    public interface RequestCode {
        int REQUEST_CODE_EPG_BOOK = 1;
        int REQUEST_CODE_MOTOR = 2;
    }

    public interface PrefsKey {
        String LOCALE_TYPE = "localeType";
        String SAVE_CHANNEL = "channel";
        String LOAD_SAT_TYPE = "loadSatType";
    }

    public interface SatInfoValue {
        int UNICABLE_DISABLE = 0;
        int UNICABLE_SCR_ENABLE = 1;
        int UNICABLE_DCSS_ENABLE = 2;
        int SINGLE_SAT_POSITION = 0;
        int MULTI_SAT_POSITION_A = 1;
        int MULTI_SAT_POSITION_B = 2;
        int SCR_4 = 0;
        int SCR_8 = 1;

        // diseqc10_pos=0，OFF或ToneBurst
        int OFF_OR_TONEBURST = 0;
        // diseqc10_pos=1~4，DiSEqC DISEQC_A~D
        int DISEQC_MIN_RANGE = 1;
        int DISEQC_MAX_RANGE = 4;
        // diseqc10_pos=5~20，LNB 1~16
        int LNB_MIN_RANGE = 5;
        int LNB_MAX_RANGE = 20;

        int LNB_USER = 0;

        int HZ22K_OFF = 0;
        int HZ22K_ON = 1;
        int HZ22K_AUTO = 2;

        int LNB_POWER_OFF = 0;
        int LNB_POWER_ON = 1;
    }

    public interface TopmostMenuEvent {
        String MENU_CONFIG_NAME = "menuconfig.json";

        String INSTALLATION = "Installation";
        String BACK = "Back";
        String CLEARCHANNEL = "ClearChannel";
        String RESTOREUSERDATA = "RestoreUserData";
        String BACKUP = "Backup";
        String PARENTALCONTAOL = "ParentalControl";
        String DATARESET = "DataReset";
    }
}
