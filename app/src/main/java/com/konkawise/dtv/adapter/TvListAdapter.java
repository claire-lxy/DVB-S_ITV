package com.konkawise.dtv.adapter;

import android.content.Context;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class TvListAdapter extends BaseListViewAdapter<PDPMInfo_t> {
    private int mSelectPosition;

    public TvListAdapter(Context context, List<PDPMInfo_t> datas) {
        super(context, datas, R.layout.prog_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, PDPMInfo_t item) {
        holder.setText(R.id.tv_prog_num, String.valueOf(item.PShowNo))
                .setText(R.id.tv_prog_name, item.Name)
                .setVisibility(R.id.iv_prog_play, mSelectPosition == position ? View.VISIBLE : View.GONE)
                .setVisibility(R.id.iv_prog_fav, item.FavFlag >= 1 ? View.VISIBLE : View.INVISIBLE)
                .setVisibility(R.id.iv_prog_lock, item.LockFlag == 1 ? View.VISIBLE : View.INVISIBLE)
                .setVisibility(R.id.iv_prog_pay, item.CasFlag == 1 ? View.VISIBLE : View.INVISIBLE);
    }

    public void setSelectPosition(int position) {
        mSelectPosition = position;
        notifyDataSetChanged();
    }
}
