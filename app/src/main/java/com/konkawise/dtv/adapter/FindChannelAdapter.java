package com.konkawise.dtv.adapter;

import android.content.Context;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class FindChannelAdapter extends BaseListViewAdapter<PDPMInfo_t> {
    private String mHighLightKeywords;

    public FindChannelAdapter(Context context, List<PDPMInfo_t> datas) {
        super(context, datas, R.layout.find_channel_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, PDPMInfo_t item) {
        holder.setText(R.id.tv_prog_num, String.valueOf(item.PShowNo))
                .setText(R.id.tv_prog_name, getHighLightName(item.Name));
    }

    public void updateHighLightKeywords(String highLightKeywords) {
        this.mHighLightKeywords = highLightKeywords;
    }

    public String getHighLightName(String name) {
        return name;
    }
}
