package com.konkawise.dtv.bean;

import com.konkawise.dtv.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMenuInfo {
    public static final Map<String, Integer> mMenuItemMap = new HashMap<>();

    static {
        mMenuItemMap.put("Installation", R.string.Installation);
        mMenuItemMap.put("EPG", R.string.epg);
        mMenuItemMap.put("Channel Manager", R.string.Channel_management);
        mMenuItemMap.put("Channel Edit", R.string.channel_edit);
        mMenuItemMap.put("Channel Favorite List", R.string.channel_list_fav);
        mMenuItemMap.put("Clear Channel", R.string.clear_channel);
        mMenuItemMap.put("Restore User Data", R.string.restore_channel);
        mMenuItemMap.put("Backup User Data", R.string.backup_channel);
        mMenuItemMap.put("Back", R.string.back);
        mMenuItemMap.put("DTV Setting", R.string.dtv_setting);
        mMenuItemMap.put("General Settings", R.string.general_setting);
        mMenuItemMap.put("T/T2 Settings", R.string.t2_setting);
        mMenuItemMap.put("Parental Control", R.string.parental_control);
        mMenuItemMap.put("PVR Settings", R.string.pvr_settings);
        mMenuItemMap.put("Book List", R.string.book_list);
        mMenuItemMap.put("Record List", R.string.record_list);
        mMenuItemMap.put("Data Reset", R.string.data_reset);
    }

    private List<MenuItemInfo> ltItems;

    public List<MenuItemInfo> getLtItems() {
        return ltItems;
    }

    public void setLtItems(List<MenuItemInfo> ltItems) {
        this.ltItems = ltItems;
    }
}
