package com.konkawise.dtv.adapter;

import android.content.Context;
import android.view.View;
import android.widget.RadioButton;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.HashMap;
import java.util.List;

public class SubtitleAdapter extends BaseListViewAdapter<HashMap<String, Object>> {
    private int mSelectPosition;

    public SubtitleAdapter(Context context, List<HashMap<String, Object>> datas) {
        super(context, datas, R.layout.subtitle_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, HashMap<String, Object> item) {
		if(position > 0) {
			holder.setText(R.id.subtitle_item_language, (String) item.get(Constants.SUBTITLE_NAME));
			holder.setImageResource(R.id.subtitle_item_type, ((Boolean) item.get(Constants.SUBTITLE_ORG_TYPE)) ? R.drawable.icon_dvb : R.drawable.icon_txt);
			holder.setVisibility(R.id.subtitle_item_hard, ((Boolean) item.get(Constants.SUBTITLE_TYPE)) ? View.VISIBLE : View.GONE);
		} else {
			holder.setText(R.id.subtitle_item_language, (String) item.get(Constants.SUBTITLE_NAME));
			holder.setVisibility(R.id.subtitle_item_type, View.GONE);
		}
		RadioButton radioButton = holder.getView(R.id.subtitle_item_check);
        radioButton.setChecked(mSelectPosition == position);
    }

    public void setSelectItem(int selectPosition) {
        mSelectPosition = selectPosition;
        notifyDataSetChanged();
    }
}
