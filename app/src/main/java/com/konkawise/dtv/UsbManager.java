package com.konkawise.dtv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;

import com.konkawise.dtv.annotation.UsbObserveType;
import com.konkawise.dtv.bean.UsbInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class UsbManager {
    private static final String TAG = "UsbManager";
    private Set<UsbInfo> mUsbInfos = new LinkedHashSet<>();
    private List<OnUsbReceiveListener> mOnUsbReceiveListeners = new LinkedList<>();

    private static class UsbManagerHolder {
        private static final UsbManager INSTANCE = new UsbManager();
    }

    public static UsbManager getInstance() {
        return UsbManagerHolder.INSTANCE;
    }

    public Set<UsbInfo> getUsbInfos() {
        return mUsbInfos;
    }

    public void registerUsbReceiveListener(OnUsbReceiveListener listener) {
        mOnUsbReceiveListeners.add(listener);
    }

    public void unregisterUsbReceiveListener(OnUsbReceiveListener listener) {
        mOnUsbReceiveListeners.remove(listener);
    }

    public void usbObserveReceive(Context context, Intent intent, @UsbObserveType int usbObserveType) {
        Set<UsbInfo> tUsbInfos = queryUsbInfos(context);
        UsbInfo attachUsbInfo = new UsbInfo();
        if(usbObserveType == Constants.USB_TYPE_ATTACH){
            for(UsbInfo usbInfo: tUsbInfos){
                if(intent.getData().getPath().equals(usbInfo.path)){
                    usbInfo.uri = intent.getData();
                    attachUsbInfo = usbInfo;
                    mUsbInfos.add(attachUsbInfo);
                    break;
                }
            }
        }else{
            for(UsbInfo usbInfo: mUsbInfos){
                if(usbInfo.uri.equals(intent.getData())){
                    attachUsbInfo = usbInfo;
                    mUsbInfos.remove(attachUsbInfo);
                    break;
                }
            }
        }
        Log.i(TAG, "intent = " + intent + ", usbInfos = " + mUsbInfos);
        for (OnUsbReceiveListener listener : mOnUsbReceiveListeners) {
            if (listener != null) {
                listener.onUsbReceive(usbObserveType, mUsbInfos, attachUsbInfo);
            }
        }
       /* try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Class clazz = Class.forName("android.os.storage.StorageManager");
            Method methodGetVolumes = clazz.getMethod("getVolumes");
            List<?> volumeInfos = (List<?>) methodGetVolumes.invoke(sm);
            @SuppressLint("PrivateApi") Class volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            Method methodGetFsUUID = volumeInfoClazz.getMethod("getFsUuid");

            Field fieldFsType = volumeInfoClazz.getDeclaredField("fsType") ;
            Field fieldFsLabelField = volumeInfoClazz.getDeclaredField("fsLabel");
            Field fieldPath = volumeInfoClazz.getDeclaredField("path");
            Field fieldInternalPath = volumeInfoClazz.getDeclaredField("internalPath");
            Log.i(TAG,"volumeInfos size:"+volumeInfos.size());
            if (volumeInfos != null) {
                for (Object volumeInfo : volumeInfos) {
                    String uuid = (String) methodGetFsUUID.invoke(volumeInfo);
                    if (uuid != null) {
                        Log.i(TAG,"add usbinfo---");
                        UsbInfo usbInfo = new UsbInfo();
                        usbInfo.uuid = uuid;
                        usbInfo.uri = intent.getData();
                        usbInfo.fsType = (String) fieldFsType.get(volumeInfo);
                        usbInfo.fsLabel = (String) fieldFsLabelField.get(volumeInfo);
                        usbInfo.path = (String) fieldPath.get(volumeInfo);
                        usbInfo.internalPath = (String) fieldInternalPath.get(volumeInfo);
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                StatFs statFs = new StatFs(usbInfo.path) ;
                                usbInfo.availableSize = Formatter.formatFileSize(context, statFs.getAvailableBytes());
                                usbInfo.totalSize = Formatter.formatFileSize(context, statFs.getTotalBytes());
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }

                        if(getSameUUIDUSbInfo(usbInfo) == null)
                            mUsbInfos.add(usbInfo);
                    } else {
                        if (usbObserveType == Constants.USB_TYPE_DETACH) {
                            Log.i(TAG,"remove usbinfo---");
                            Set<UsbInfo> sameUUIDUsbInfos = new LinkedHashSet<>();
                            Iterator<UsbInfo> iterator = mUsbInfos.iterator();
                            while (iterator.hasNext()) {
                                UsbInfo attachUsbInfo = iterator.next();
                                if (attachUsbInfo.uri.equals(intent.getData())) {
                                    // 移除可能存在相同uuid的设备
                                    UsbInfo sameUUIDUsbInfo = getSameUUIDUSbInfo(attachUsbInfo);
                                    if (sameUUIDUsbInfo != null) {
                                        sameUUIDUsbInfos.add(sameUUIDUsbInfo);
                                    }
                                    iterator.remove();
                                }
                            }

                            mUsbInfos.removeAll(sameUUIDUsbInfos);
                        }
                    }
                }
            }

            Log.i(TAG, "intent = " + intent + ", usbInfos = " + mUsbInfos);
            for (OnUsbReceiveListener listener : mOnUsbReceiveListeners) {
                if (listener != null) {
                    listener.onUsbReceive(usbObserveType, mUsbInfos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    private Set<UsbInfo> queryUsbInfos(Context context){
        Set<UsbInfo> usbInfos = new LinkedHashSet<>();
        try{
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);

            Class clazz = Class.forName("android.os.storage.StorageManager");
            Method methodGetVolumes = clazz.getMethod("getVolumes");
            List<?> volumeInfos = (List<?>) methodGetVolumes.invoke(sm);
            @SuppressLint("PrivateApi") Class volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo");
            Method methodGetFsUUID = volumeInfoClazz.getMethod("getFsUuid");

            Field fieldFsType = volumeInfoClazz.getDeclaredField("fsType") ;
            Field fieldFsLabelField = volumeInfoClazz.getDeclaredField("fsLabel");
            Field fieldPath = volumeInfoClazz.getDeclaredField("path");
            Field fieldInternalPath = volumeInfoClazz.getDeclaredField("internalPath");

            if(volumeInfos != null){
                for (Object volumeInfo : volumeInfos) {
                    String uuid = (String) methodGetFsUUID.invoke(volumeInfo);
                    if(uuid != null){
                        UsbInfo usbInfo = new UsbInfo();
                        usbInfo.uuid = uuid;
                        usbInfo.fsType = (String) fieldFsType.get(volumeInfo);
                        usbInfo.fsLabel = (String) fieldFsLabelField.get(volumeInfo);
                        usbInfo.path = (String) fieldPath.get(volumeInfo);
                        usbInfo.internalPath = (String) fieldInternalPath.get(volumeInfo);
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                StatFs statFs = new StatFs(usbInfo.path) ;
                                usbInfo.availableSize = Formatter.formatFileSize(context, statFs.getAvailableBytes());
                                usbInfo.totalSize = Formatter.formatFileSize(context, statFs.getTotalBytes());
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                        usbInfos.add(usbInfo);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return usbInfos;
    }

    private UsbInfo getSameUUIDUSbInfo(UsbInfo attachUsbInfo) {
        for (UsbInfo usbInfo : mUsbInfos) {
            if (usbInfo.uuid.equals(attachUsbInfo.uuid)) {
                return usbInfo;
            }
        }
        return null;
    }

    public interface OnUsbReceiveListener {
        void onUsbReceive(@UsbObserveType int usbObserveType, Set<UsbInfo> usbInfos, UsbInfo currUsbInfo);
    }
}
