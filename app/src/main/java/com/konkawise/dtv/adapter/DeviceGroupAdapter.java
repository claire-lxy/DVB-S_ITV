package com.konkawise.dtv.adapter;

import android.content.Context;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;
import com.konkawise.dtv.bean.UsbInfo;

import java.util.List;

public class DeviceGroupAdapter extends BaseListViewAdapter<String> {
    private int mSelectPosition;
    private boolean darked = false;

    public DeviceGroupAdapter(Context context, List<String> ltDeviceNames) {
        super(context, ltDeviceNames, R.layout.adapter_deivce_group);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, String item) {
        holder.setText(R.id.tv_device_group_index, String.valueOf(position + 1))
                .setText(R.id.tv_device_group_type, item);
        if (mSelectPosition == position && darked) {
            holder.getView(R.id.ll_root_group).setBackgroundColor(mContext.getResources().getColor(R.color.channel_edit_gray));
        } else {
            holder.getView(R.id.ll_root_group).setBackgroundColor(0);
        }
    }

    public void setSelectPosition(int position) {
        this.mSelectPosition = position;
    }

    public void setDarked(boolean darked) {
        this.darked = darked;
    }
}
