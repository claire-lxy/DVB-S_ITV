package com.konkawise.dtv.adapter;

import android.content.Context;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;

public class TpListingAdapter extends BaseListViewAdapter<HProg_Struct_TP> {

    public TpListingAdapter(Context context, List<HProg_Struct_TP> datas) {
        super(context, datas, R.layout.tp_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, HProg_Struct_TP item) {
        holder.setText(R.id.tv_tp_list_desc, getTpName(position))
                .setText(R.id.tv_tp_list_id, String.valueOf(position + 1));
    }

    private String getTpName(int position) {
        HProg_Struct_TP channel = getItem(position);
        return channel.Freq + (channel.Qam == 0 ? " H " : " V ") + channel.Symbol;
    }
}
