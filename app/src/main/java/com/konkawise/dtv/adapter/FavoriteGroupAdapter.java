package com.konkawise.dtv.adapter;

import android.content.Context;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

public class FavoriteGroupAdapter extends BaseListViewAdapter<String> {
    private int mSelectPosition;
    private boolean mFocus;

    public FavoriteGroupAdapter(Context context, List<String> datas) {
        super(context, datas, R.layout.favorite_group_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, String item) {
        if (mSelectPosition == position && mFocus) {
            holder.getView(R.id.ll_root_group).setBackgroundColor(mContext.getResources().getColor(R.color.channel_edit_gray));
        } else {
            holder.getView(R.id.ll_root_group).setBackgroundColor(0);
        }
        holder.setText(R.id.tv_fav_group_index, String.valueOf(position + 1))
                .setText(R.id.tv_fav_group_type, getItem(position));
    }

    public void setSelectPosition(int position) {
        mSelectPosition = position;
    }

    public void setFocus(boolean focus) {
        mFocus = focus;
    }
}

