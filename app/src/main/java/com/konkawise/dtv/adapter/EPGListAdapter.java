package com.konkawise.dtv.adapter;

import android.content.Context;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class EPGListAdapter extends BaseListViewAdapter<PDPMInfo_t> {
    private int mSelectPosition;
    private boolean mFocus;

    public EPGListAdapter(Context context, List<PDPMInfo_t> datas) {
        super(context, datas, R.layout.epg_listview_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, PDPMInfo_t item) {
        holder.setText(R.id.epg_list_item, item.Name);
        if (mSelectPosition == position) {
            holder.setVisibility(R.id.epg_imageview, View.VISIBLE)
                    .setVisibility(R.id.epg_tv_id, View.GONE);
            holder.getTextView(R.id.epg_list_item).setTextColor(mContext.getResources().getColor(R.color.epg_text_select));
        } else {
            holder.setText(R.id.epg_tv_id, String.valueOf(item.PShowNo))
                    .setVisibility(R.id.epg_tv_id, View.VISIBLE)
                    .setVisibility(R.id.epg_imageview, View.GONE);
            holder.getTextView(R.id.epg_list_item).setTextColor(mContext.getResources().getColor(R.color.epg_text_normal));
            holder.getTextView(R.id.epg_tv_id).setTextColor(mContext.getResources().getColor(R.color.epg_text_normal));
        }

        if (mSelectPosition == position && !mFocus) {
            holder.getView(R.id.ll_epg_item).setBackgroundColor(mContext.getResources().getColor(R.color.channel_edit_gray));
        } else {
            holder.getView(R.id.ll_epg_item).setBackground(mContext.getResources().getDrawable(R.drawable.dvb_btn_selector));
        }

        holder.setVisibility(R.id.iv_epg_pay, item.CasFlag == 1 ? View.VISIBLE : View.GONE);
    }

    public void setSelectPosition(int position) {
        mSelectPosition = position;
        notifyDataSetChanged();
    }

    public void setFocus(boolean focus) {
        mFocus = focus;
        notifyDataSetChanged();
    }
}
