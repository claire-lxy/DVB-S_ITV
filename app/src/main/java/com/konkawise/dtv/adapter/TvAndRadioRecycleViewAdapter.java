package com.konkawise.dtv.adapter;

import android.content.Context;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseRecyclerAdapter;
import com.konkawise.dtv.adapter.base.BaseRecyclerHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;

public class TvAndRadioRecycleViewAdapter extends BaseRecyclerAdapter<HProg_Struct_ProgBasicInfo> {

    public TvAndRadioRecycleViewAdapter(Context context, List<HProg_Struct_ProgBasicInfo> datas) {
        super(context, datas, R.layout.tv_and_radio_list_item);
    }

    @Override
    protected void convert(BaseRecyclerHolder holder, int position, HProg_Struct_ProgBasicInfo item) {
        holder.setText(R.id.tv_tv_and_radio_id, String.valueOf(position + 1))
                .setText(R.id.tv_tv_and_radio_desc, getItem(position).Name);
    }

    @Override
    public void addData(List<HProg_Struct_ProgBasicInfo> datas) {
        if (datas == null) return;
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }
}
