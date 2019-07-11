package com.konkawise.dtv.utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class ChannelLockProvider extends ContentProvider {
    private static final String TAG = "ChannelLockProvider";

    private static final String AUTHORITY = "dvbchannellock";
    private static final int SINGLE_ROW = 101;
    private static final int ALL_ROW = 102;

    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_VALUE = "value";

    private static final String SP_NAME = "DVB_INFO";
    public static final String CHANNEL_LOCK = "channel_lock";

    private static UriMatcher sUriMatcher;
    private SharedPreferences sp = null;
    private Context context;

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "dvb_info", ALL_ROW);
        sUriMatcher.addURI(AUTHORITY, "dvb_info/*", SINGLE_ROW);
    }

    @Override
    public boolean onCreate() {
        Log.i(TAG,"onCreate");
        this.context = getContext();
        sp = context.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        Log.i(TAG,"query");
        int code = sUriMatcher.match(uri);
        MatrixCursor vMatrixCursor = null;
        switch (code){
            case UriMatcher.NO_MATCH:
                return null;

            case ALL_ROW:
            case SINGLE_ROW:
                String locked = sp.getString(CHANNEL_LOCK,"0");
                vMatrixCursor = new MatrixCursor(new String[]{"locked"}, 1);
                vMatrixCursor.addRow(new Object[]{locked});
                return vMatrixCursor;
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        Log.i(TAG,"updatePfInformation");
        int code = sUriMatcher.match(uri);
        switch (code){
            case UriMatcher.NO_MATCH:
                return -1;

            case ALL_ROW:
                String name = contentValues.get(COLUMN_NAME) == null ? "" : contentValues.get(COLUMN_NAME).toString();
                String value = contentValues.get(COLUMN_VALUE) == null ? "" : contentValues.get(COLUMN_VALUE).toString();
                if(CHANNEL_LOCK.equals(name)){
                    Log.i(TAG,"updatePfInformation name:"+name+" value:"+value);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.clear();
                    editor.putString(CHANNEL_LOCK,value);
                    editor.commit();
                    return 1;
                }
                return -1;

            case SINGLE_ROW:
                String value2 = uri.getPathSegments().get(1);
                Log.i(TAG,"updatePfInformation value2:"+value2);
                SharedPreferences.Editor editor = sp.edit();
                editor.clear();
                editor.putString(CHANNEL_LOCK,value2);
                editor.commit();
                return 1;
        }
        return 0;
    }
}
