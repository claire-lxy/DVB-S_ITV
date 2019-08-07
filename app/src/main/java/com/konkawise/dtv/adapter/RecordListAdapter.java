package com.konkawise.dtv.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;
import com.konkawise.dtv.bean.RecordInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

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
                .setText(R.id.tv_record_channel_file_size, getPrintSize(item.getFile().length()))
                .setText(R.id.tv_record_channel_date, getModifiedTime(item.getFile().lastModified()))
                .setVisibility(R.id.iv_item_record_select, !mSelectMap.get(position) ? View.INVISIBLE : View.VISIBLE)
                .setVisibility(R.id.iv_item_record_lock, 0 == 0 ? View.INVISIBLE : View.VISIBLE);
        if (currSelectPosition == position && darked) {
            holder.getView(R.id.ll_root_group2).setBackgroundColor(mContext.getResources().getColor(R.color.channel_edit_gray));
        } else {
            holder.getView(R.id.ll_root_group2).setBackgroundColor(0);
        }
    }

    public static String getPrintSize(long size) {
        // 如果字节数少于1024，则直接以B为单位，否则先除于1024，后3位因太少无意义
        if (size < 1024) {
            return String.valueOf(size) + "B";
        } else {
            size = size / 1024;
        }
        // 如果原字节数除于1024之后，少于1024，则可以直接以KB作为单位
        // 因为还没有到达要使用另一个单位的时候
        // 接下去以此类推
        if (size < 1024) {
            return String.valueOf(size) + "K";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            // 因为如果以MB为单位的话，要保留最后1位小数，
            // 因此，把此数乘以100之后再取余
            size = size * 100;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "M";
        } else {
            // 否则如果要以GB为单位的，先除于1024再作同样的处理
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "." + String.valueOf((size % 100)) + "G";
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
