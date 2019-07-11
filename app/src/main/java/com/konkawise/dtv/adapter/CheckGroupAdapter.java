package com.konkawise.dtv.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.widget.CheckBox;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

public class CheckGroupAdapter extends BaseListViewAdapter<String> {
    private SparseBooleanArray mCheckMap = new SparseBooleanArray();

    public CheckGroupAdapter(Context context, List<String> datas) {
        super(context, datas, R.layout.dialog_check_group_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, String item) {
        holder.setText(R.id.tv_check_group_name, item);
        CheckBox checkBox = holder.getView(R.id.cb_check_group);
        checkBox.setChecked(mCheckMap.get(position));
    }

    public void setCheck(int position) {
        mCheckMap.put(position, !mCheckMap.get(position));
        notifyDataSetChanged();
    }

    public void checkAll() {
        for (int i = 0; i < getCount(); i++) {
            mCheckMap.put(i, true);
        }
        notifyDataSetChanged();
    }

    public SparseBooleanArray getCheckMap() {
        return mCheckMap;
    }
}
