package com.konkawise.dtv.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.util.SparseBooleanArray;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;
import com.konkawise.dtv.bean.RecordInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class RecordListAdapter extends BaseListViewAdapter<RecordInfo> {
    private int currSelectPosition;
    private boolean darked = false;
    private SparseBooleanArray mSelectMap = new SparseBooleanArray();

    public RecordListAdapter(Context context, List<RecordInfo> datas) {
        super(context, datas, R.layout.record_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, RecordInfo item) {
        holder.setText(R.id.tv_item_record_channel_num, String.valueOf(position + 1))
                .setText(R.id.tv_record_prog_name, item.getFile().getName())
                .setText(R.id.tv_record_channel_file_size, Formatter.formatFileSize(mContext, item.getFile().length()))
                .setText(R.id.tv_record_channel_date, getModifiedTime(item.getFile().lastModified()))
                .setVisibility(R.id.iv_item_record_select, !mSelectMap.get(position) ? View.INVISIBLE : View.VISIBLE)
                .setVisibility(R.id.iv_item_record_lock, 0 == 0 ? View.INVISIBLE : View.VISIBLE);
        if (currSelectPosition == position && darked) {
            holder.getView(R.id.ll_root_group2).setBackgroundColor(mContext.getResources().getColor(R.color.channel_edit_gray));
        } else {
            holder.getView(R.id.ll_root_group2).setBackgroundColor(0);
        }
    }

    public static String getModifiedTime(long time) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        cal.setTimeInMillis(time);
        return formatter.format(cal.getTime());
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
