package com.konkawise.dtv.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.konkawise.dtv.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortAdapter extends BaseAdapter {
    String[] sortString = null;
    private Context mContext;
    private SortDialogViewHolder sortDialogViewHolder;
    private int selected = -1;

    public SortAdapter(String[] sortArray, Context context, int selected) {
        sortString = sortArray;
        mContext = context;
        this.selected = selected;
    }

    @Override
    public int getCount() {
        return sortString.length;
    }

    @Override
    public String getItem(int position) {
        return sortString[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.dialog_check_group_list_item, null);
            sortDialogViewHolder = new SortDialogViewHolder(convertView);
            convertView.setTag(sortDialogViewHolder);
        } else {
            sortDialogViewHolder = (SortDialogViewHolder) convertView.getTag();
        }

        sortDialogViewHolder.tv_fav_group.setText(sortString[position]);
        if (selected == position) {
            if (sortDialogViewHolder.fav_dialog_checkbox.isChecked()) {
                sortDialogViewHolder.fav_dialog_checkbox.setChecked(false);
                selected = -1;
            } else {
                sortDialogViewHolder.fav_dialog_checkbox.setChecked(true);
            }
        } else {
            sortDialogViewHolder.fav_dialog_checkbox.setChecked(false);
        }

        return convertView;
    }

    public void setSelected(int selected) {
        this.selected = selected;
        notifyDataSetChanged();
    }

    public int getSelected() {
        return selected;
    }


    public class SortDialogViewHolder {
        @BindView(R.id.tv_check_group_name)
        public TextView tv_fav_group;

        @BindView(R.id.cb_check_group)
        public CheckBox fav_dialog_checkbox;

        SortDialogViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }


}
