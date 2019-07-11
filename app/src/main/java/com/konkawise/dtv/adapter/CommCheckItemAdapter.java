package com.konkawise.dtv.adapter;

import android.content.Context;
import android.widget.RadioButton;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

public class CommCheckItemAdapter extends BaseListViewAdapter<String> {
    private int mSelectPosition;

    public CommCheckItemAdapter(Context context, List<String> datas) {
        super(context, datas, R.layout.listview_book_type_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, String item) {
        holder.setText(R.id.bookType, item);
        RadioButton radioButton = holder.getView(R.id.rb);
        radioButton.setChecked(mSelectPosition == position);
    }

    public void setSelectItem(int selectPosition) {
        mSelectPosition = selectPosition;
        notifyDataSetChanged();
    }
}
