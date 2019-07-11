package com.konkawise.dtv.utils;

import android.util.Log;

/**
 * 日志打印工具类
 *
 *
 */
public class LogUtils {
    private static final boolean LOGOPEN = true;

    public static void  v(String tag,String message){
        if (LOGOPEN){
            Log.v(tag,message);
        }
    }


    public static void e(String tag, String msg){
        if (LOGOPEN){
            Log.e(tag,msg);
        }
    }


    public static void d(String tag, String msg){
        if (LOGOPEN){
            Log.d(tag,msg);
        }
    }


    public static void  w(String tag, String msg){
        if (LOGOPEN){
            Log.w(tag,msg);
        }
    }

    public static void i(String tag,String msg){
        if (LOGOPEN){
            Log.i(tag,msg);
        }
    }

    public static void e(String tag,String msg,Exception e){
        if (LOGOPEN){
            Log.e(tag, msg, e);
        }
    }


}
