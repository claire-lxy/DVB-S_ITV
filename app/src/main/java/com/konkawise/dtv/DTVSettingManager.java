package com.konkawise.dtv;

import com.sw.dvblib.DTVSetting;

import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;

public class DTVSettingManager {
    private static class DTVSettingManagerHolder {
        private static final DTVSettingManager INSTANCE = new DTVSettingManager();
    }

    private DTVSettingManager() {
        DTVSetting.getInstance();
    }

    public static DTVSettingManager getInstance() {
        return DTVSettingManagerHolder.INSTANCE;
    }

    /**
     * 是否打开Menu Lock
     */
    public boolean isOpenMenuLock() {
        return getDTVProperty(HSetting_Enum_Property.cMenuLock) == 1;
    }

    public int getCurrScanMode() {
        return getDTVProperty(HSetting_Enum_Property.ScanMode);
    }

    public int getCurrNetwork() {
        return getDTVProperty(HSetting_Enum_Property.Network);
    }

    public int getCurrCAS() {
        return getDTVProperty(HSetting_Enum_Property.CAS);
    }

    public long dismissTimeout() {
        return getDTVProperty(HSetting_Enum_Property.PD_dispalytime) * 1000;
    }

    /**
     * 是否为第一次开启启动没有密码
     * <p>
     * SWFta.E_E2PP.E2P_FirstOpen.ordinal()==1表示第一次启动，SWFta.E_E2PP.E2P_FirstOpen.ordinal()==0表示不是第一次启动
     */
    public boolean isPasswordEmpty() {
        return getDTVProperty(HSetting_Enum_Property.FirstOpen) == 1;
    }

    /**
     * 保存Setting参数
     */
    public void setDTVProperty(int param, int value) {
        DTVSetting.getInstance().setDTVProperty(param, value);
    }

    /**
     * 获取Setting参数
     */
    public int getDTVProperty(int param) {
        return DTVSetting.getInstance().getDTVProperty(param);
    }

    /**
     * 获取Parental Lock或Menu Lock的密码
     */
    public String getPasswd(int param) {
        return DTVSetting.getInstance().getPasswd(param);
    }

    /**
     * 设置Parental Lock或Menu Lock的密码
     */
    public void setPasswd(int param, String password) {
        DTVSetting.getInstance().setPasswd(param, password);
    }

    /**
     * 获取录制的盘符uuid
     */
    public String getDiskUUID() {
        return DTVSetting.getInstance().getDiskUUID();
    }

    /**
     * 设置录制的盘符uuid
     */
    public void setDiskUUID(String uuid) {
        DTVSetting.getInstance().setDiskUUID(uuid);
    }
}
