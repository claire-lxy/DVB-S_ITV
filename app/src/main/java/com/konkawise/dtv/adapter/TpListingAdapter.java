package com.konkawise.dtv.adapter;

import android.content.Context;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;

public class TpListingAdapter extends BaseListViewAdapter<ChannelNew_t> {

    public TpListingAdapter(Context context, List<ChannelNew_t> datas) {
        super(context, datas, R.layout.tp_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, ChannelNew_t item) {
        holder.setText(R.id.tv_tp_list_desc, getTpName(position))
                .setText(R.id.tv_tp_list_id, String.valueOf(position + 1));
    }

    private String getTpName(int position) {
        ChannelNew_t channel = getItem(position);
        return channel.Freq + (channel.Qam == 0 ? " H " : " V ") + channel.Symbol;
    }
}
