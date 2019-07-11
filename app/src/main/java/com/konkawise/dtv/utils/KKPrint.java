package com.konkawise.dtv.utils;

import android.util.Log;

/**
 * print the log information.
 * call the function of *_path to print the file name, line number, class name and method name
 * @author zhouhouqiu
 */
public class KKPrint {
	private static final String LOG_TAG = "KonkaDVB";
//	private static KKPrint kkPrint = null;
	
	private static final int DEBUG_LEVEL_VERBOSE = 0;
	private static final int DEBUG_LEVEL_DEBUG = 1;
	private static final int DEBUG_LEVEL_INFO = 2;
	private static final int DEBUG_LEVEL_WARN = 3;
	private static final int DEBUG_LEVEL_ERROR = 4;
//	private static final int DEBUG_LEVEL_ASSERT = 5;
	
	public static final int DEBUG_LEVEL = DEBUG_LEVEL_VERBOSE;
	
	public static String getLogTag(){
		return LOG_TAG;
	}
	
	
	private KKPrint() {
	}

	/*static public synchronized KKPrint getInstance(){
		// TODO Auto-generated constructor stub
		if(null == kkPrint){
			kkPrint = new KKPrint();
		}
		return kkPrint;
	}*/


	/**
	 * @param deep
	 * @return "[file_name, line_number, class_name.method_name]"
	 */
	private static String getCurrentPath(int deep) {  
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        
		/*d("_______________________________");
		for(int i = 0; i< stacktrace.length; i++){
			d("--------" + i + "--------");
			d("FileName-->"+stacktrace[i].getFileName());
			d("ClassName-->"+stacktrace[i].getClassName());
			d("MethodName-->"+stacktrace[i].getMethodName());
			d("LineNumber-->"+stacktrace[i].getLineNumber());
		}
		d("_______________________________");*/
        
        StackTraceElement e = stacktrace[deep];  
        
        return "["+e.getFileName()+",line:"+e.getLineNumber()+","+e.getClassName()+"."+e.getMethodName()+"] "; 
        //return "["+e.getClassName()+"."+e.getMethodName()+"] "; 
    }
	
	/**
	 * @return "[file_name, line_number, class_name.method_name]"
	 */
	public static String getCurrentPath() {  
		return getCurrentPath(4);  
    }
	
	public static void d(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_DEBUG)
			Log.d(getLogTag(), msg);
	} 

	public static void i(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_INFO)
			Log.i(getLogTag(), msg);
	} 

	public static void v(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_VERBOSE)
			Log.v(getLogTag(), msg);
	} 

	public static void w(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_WARN)
			Log.w(getLogTag(), msg);
	} 

	public static void e(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_ERROR)
			Log.e(getLogTag(), msg);
	}  
	
	public static void d(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_DEBUG)
			Log.d(tag,msg);
	} 

	public static void i(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_INFO)
			Log.i(tag,msg);
	} 

	public static void v(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_VERBOSE)
			Log.v(tag,msg);
	} 

	public static void w(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_WARN)
			Log.w(tag,msg);
	} 

	public static void e(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_ERROR)
			Log.e(tag,msg);
	} 
	
	public static void d_path(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_DEBUG)
			Log.d(getLogTag(), getCurrentPath(4) + msg);
	} 

	public static void i_path(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_INFO)
			Log.i(getLogTag(), getCurrentPath(4) + msg);
	} 

	public static void v_path(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_VERBOSE)
			Log.v(getLogTag(), getCurrentPath(4) + msg);
	} 

	public static void w_path(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_WARN)
			Log.w(getLogTag(), getCurrentPath(4) + msg);
	} 

	public static void e_path(String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_ERROR)
			Log.e(getLogTag(), getCurrentPath(4) + msg);
	}  
	
	public static void d_path(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_DEBUG)
			Log.d(tag,getCurrentPath(4) + ": "+msg);
	} 

	public static void i_path(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_INFO)
			Log.i(tag,getCurrentPath(4) + ": "+msg);
	} 

	public static void v_path(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_VERBOSE)
			Log.v(tag,getCurrentPath(4) + ": "+msg);
	} 

	public static void w_path(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_WARN)
			Log.w(tag,getCurrentPath(4) + ": "+msg);
	} 

	public static void e_path(String tag,String msg) { 
		if(DEBUG_LEVEL <= DEBUG_LEVEL_ERROR)
			Log.e(tag,getCurrentPath(4) + ": "+msg);
	}
}