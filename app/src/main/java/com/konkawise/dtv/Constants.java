package com.konkawise.dtv;

public class Constants {
    // installation scan type, s2 or t2
    public static final int INSTALLATION_S2_SCAN = 0x1001;
    public static final int INSTALLATION_T2_AUTO_SEARCH = 0x1002;
    public static final int INSTALLATION_T2_MANUAL_SEARCH = 0x1003;

    // support language
    public static final int LOCALE_TYPE_SYSTEM = 0x2001;
    public static final int LOCALE_TYPE_ITALIAN = 0x2002;
    public static final int LOCALE_TYPE_ENGLISH = 0x2003;
    public static final int LOCALE_TYPE_CHINESE = 0x2004;

    // usb receive type
    public static final int USB_TYPE_ATTACH = 0x3001;
    public static final int USB_TYPE_DETACH = 0x3002;

    // booking receive type
    public static final int BOOKING_TYPE_PLAY_STANDBY = 0x4001;
    public static final int BOOKING_TYPE_RECORD_STANDBY = 0x4002;

    // book type
    public static final int BOOK_TYPE_ADD = 0x5001; // 添加book
    public static final int BOOK_TYPE_EDIT = 0x5002; // 编辑book

    // book conflict type
    public static final int BOOK_CONFLICT_LIMIT = 0x6001; // book达到最大值32个，提示不能添加
    public static final int BOOK_CONFLICT_NONE = 0x6002; // book没冲突，正常添加
    public static final int BOOK_CONFLICT_ADD = 0x6003; // book冲突，需要先删除后添加
    public static final int BOOK_CONFLICT_REPLACE = 0x6004; // book冲突，需要替换

    // tp type
    public static final int TP_TYPE_ADD = 0x7001;
    public static final int TP_TYPE_EDIT = 0x7002;

    // DiSEqC index
    public static final int DISEQC_A = 0;
    public static final int DISEQC_B = 1;
    public static final int DISEQC_C = 2;
    public static final int DISEQC_D = 3;

    public interface IntentKey {
        String INTENT_SATELLITE_INDEX = "satelliteIndex";
        String INTENT_SATELLITE_ACTIVITY = "satelliteActivity";
        String INTENT_TP_NAME = "tpName";
        String INTENT_LNB = "lnb";
        String INTENT_DISEQC = "diseqc";
        String INTENT_FREQ = "freq";
        String INTENT_TP_INDEX = "tpIndex";
        String INTENT_SYMBOL = "symbol";
        String INTENT_QAM = "qam";
        String INTENT_TPLIST_ACTIVITY = "tpListActivity";
        String INTENT_EDIT_MANUAL_ACTIVITY = "editManualActivity";

        String INTENT_T2_SETTING = "t2Setting";

        String INTENT_BOOK_TYPE = "bookType";
        String INTENT_BOOK_PROG_NUM = "bookProgNum";
        String INTENT_BOOK_PROG_TYPE = "bookProgType";
        String INTENT_BOOK_SECONDS = "bookSeconds";
    }

    public interface PrefsKey {
        // language
        String LOCALE_TYPE = "localeType";
    }
}
