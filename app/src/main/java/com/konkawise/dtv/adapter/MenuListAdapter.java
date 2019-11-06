package com.konkawise.dtv.adapter;

import android.content.Context;
import android.view.View;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;
import com.konkawise.dtv.bean.MainMenuInfo;
import com.konkawise.dtv.bean.MenuItemInfo;

import java.util.List;

public class MenuListAdapter extends BaseListViewAdapter<MenuItemInfo> {
    public MenuListAdapter(Context context, List<MenuItemInfo> datas) {
        super(context, datas, R.layout.menu_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, MenuItemInfo item) {
        holder.setText(R.id.tv_channel_manage, mContext.getResources().getString(MainMenuInfo.mMenuItemMap.get(item.getText())))
                .setVisibility(R.id.iv_channel_manage_back, item.getCallback().equals(Constants.TopmostMenuEvent.BACK) ? View.VISIBLE : View.GONE);
    }
}
