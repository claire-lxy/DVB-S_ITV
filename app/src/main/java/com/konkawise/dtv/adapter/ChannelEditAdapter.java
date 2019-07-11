package com.konkawise.dtv.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.ArrayList;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class ChannelEditAdapter extends BaseListViewAdapter<PDPMInfo_t> {
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();
    private SparseBooleanArray mDeleteMap = new SparseBooleanArray();

    public ChannelEditAdapter(Context context, List<PDPMInfo_t> datas) {
        super(context, datas, R.layout.channel_edit_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, PDPMInfo_t item) {
        holder.setText(R.id.tv_channel_num, String.valueOf(item.PShowNo))
                .setText(R.id.tv_program_nanme, item.Name);

        holder.setVisibility(R.id.channel_dollars, item.CasFlag == 0 ? View.INVISIBLE : View.VISIBLE)
                .setVisibility(R.id.iv_channel_edit_gou, mSelectMap.get(position) ? View.VISIBLE : View.INVISIBLE)
                .setVisibility(R.id.iv_channel_edit_delete, mDeleteMap.get(position) ? View.VISIBLE : View.INVISIBLE)
                .setVisibility(R.id.iv_channel_edit_fav, item.FavFlag == 0 ? View.INVISIBLE : View.VISIBLE)
                .setVisibility(R.id.iv_channel_edit_lock, item.LockFlag == 0 ? View.INVISIBLE : View.VISIBLE)
                .setVisibility(R.id.iv_channel_edit_skip, item.HideFlag == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    public void setSelect(int position) {
        setSelect(position, !mSelectMap.get(position));
    }

    public void setSelect(int position, boolean isSelect) {
        mSelectMap.put(position, isSelect);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectMap() {
        return mSelectMap;
    }

    public void setDelete(int position) {
        setDelete(position, !mDeleteMap.get(position));
    }

    public void setDelete(int position, boolean isDelete) {
        mDeleteMap.put(position, isDelete);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getDeleteMap() {
        return mDeleteMap;
    }

    public List<PDPMInfo_t> moveChannels() {
        List<PDPMInfo_t> moveChannels = new ArrayList<>();
        for (int i = 0; i < mDatas.size(); i++) {
            if (mSelectMap.get(i)) {
                moveChannels.add(getItem(i));
            }
        }
        mDatas.removeAll(moveChannels);
        return moveChannels;
    }

    public void clearSelect() {
        mSelectMap.clear();
        notifyDataSetChanged();
    }

    public void clearDelete() {
        mDeleteMap.clear();
        notifyDataSetChanged();
    }
}
