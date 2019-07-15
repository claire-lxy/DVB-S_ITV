package com.konkawise.dtv.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class RecordListAdapter extends BaseListViewAdapter<PDPMInfo_t> {
    private int currSelectPosition;
    private boolean darked = false;
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();

    public RecordListAdapter(Context context, List<PDPMInfo_t> datas) {
        super(context, datas, R.layout.record_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, PDPMInfo_t item) {
        holder.setText(R.id.tv_item_record_channel_num, String.valueOf(item.PShowNo))
                .setText(R.id.tv_record_prog_name, item.Name)
                .setText(R.id.tv_record_channel_file_size, "")
                .setText(R.id.tv_record_channel_date, "")
                .setVisibility(R.id.iv_item_record_select, !mSelectMap.get(position) ? View.INVISIBLE : View.VISIBLE)
                .setVisibility(R.id.iv_item_record_lock, item.LockFlag == 0 ? View.INVISIBLE : View.VISIBLE);
        if (currSelectPosition == position && darked) {
            holder.getView(R.id.ll_root_group2).setBackgroundColor(mContext.getResources().getColor(R.color.channel_edit_gray));
        } else {
            holder.getView(R.id.ll_root_group2).setBackgroundColor(0);
        }
    }

    public void setSelect(int position) {
        mSelectMap.put(position, !mSelectMap.get(position));
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectMap() {
        return mSelectMap;
    }

    public void clearSelect() {
        mSelectMap.clear();
    }

    public void setSelectPosition(int position) {
        this.currSelectPosition = position;
    }

    public void setDarked(boolean darked) {
        this.darked = darked;
    }
}
