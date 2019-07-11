package com.konkawise.dtv.adapter;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.widget.CheckBox;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

public class SatelliteListAdapter extends BaseListViewAdapter<SatInfo_t> {
    private SparseBooleanArray mCheckSatelliteMap = new SparseBooleanArray();

    public SatelliteListAdapter(Context context, List<SatInfo_t> datas) {
        super(context, datas, R.layout.satellite_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, SatInfo_t item) {
        holder.setText(R.id.tv_sat_position, String.valueOf(position + 1))
                .setText(R.id.tv_sat_name, item.sat_name);
        CheckBox checkBox = holder.getView(R.id.checkbox);
        checkBox.setChecked(isSatelliteCheck(position));
    }

    public void setSatelliteCheck(int position) {
        mCheckSatelliteMap.put(position, !isSatelliteCheck(position));
        notifyDataSetChanged();
    }

    public boolean isSatelliteCheck(int position) {
        return mCheckSatelliteMap.get(position);
    }
}
