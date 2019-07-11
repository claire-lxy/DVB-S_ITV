package com.konkawise.dtv.adapter;

import android.content.Context;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseRecyclerAdapter;
import com.konkawise.dtv.adapter.base.BaseRecyclerHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;

public class TvAndRadioRecycleViewAdapter extends BaseRecyclerAdapter<PDPInfo_t> {

    public TvAndRadioRecycleViewAdapter(Context context, List<PDPInfo_t> datas) {
        super(context, datas, R.layout.adapter_tv_and_radio_item);
    }

    @Override
    protected void convert(BaseRecyclerHolder holder, int position, PDPInfo_t item) {
        holder.setText(R.id.tv_tv_and_radio_id, String.valueOf(position + 1))
                .setText(R.id.tv_tv_and_radio_desc, getItem(position).Name);
    }

    @Override
    public void addData(List<PDPInfo_t> datas) {
        if (datas == null) return;
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }
}
