package com.konkawise.dtv.adapter;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.base.BaseListViewAdapter;
import com.konkawise.dtv.adapter.base.BaseListViewHolder;

import java.util.ArrayList;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;

public class FavoriteChannelAdapter extends BaseListViewAdapter<HProg_Struct_ProgInfo> {
    private static final String TAG = "FavoriteChannelAdapter";

    private int mSelectPosition;
    private boolean mFocus;

    private SparseBooleanArray mSelectMap = new SparseBooleanArray();

    public FavoriteChannelAdapter(Context context, List<HProg_Struct_ProgInfo> datas) {
        super(context, datas, R.layout.favorite_channel_list_item);
    }

    @Override
    protected void convert(BaseListViewHolder holder, int position, HProg_Struct_ProgInfo item) {
        if (mSelectPosition == position && !mFocus) {
            holder.getView(R.id.ll_fav_adapter_root).setBackgroundColor(mContext.getResources().getColor(R.color.channel_edit_gray));
        } else {
            holder.getView(R.id.ll_fav_adapter_root).setBackgroundColor(0);
        }

        Log.i(TAG, "ProgIndex:"+item.ProgIndex+"tv_fav_channel_name:"+item.Name);
        holder.setText(R.id.tv_fav_channel_num, String.valueOf(position + 1))
                .setText(R.id.tv_fav_channel_name, item.Name);
        holder.setVisibility(R.id.iv_channel_dui_flag, mSelectMap.get(position) ? View.VISIBLE : View.INVISIBLE);
    }

    public void setSelectPosition(int position) {
        mSelectPosition = position;
    }

    public void setFocus(boolean focus) {
        mFocus = focus;
    }

    public void setSelect(int position) {
        setSelect(position, !mSelectMap.get(position));
    }

    public void setSelect(int position, boolean isSelect) {
        mSelectMap.put(position, isSelect);
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSelectMap() {
        return mSelectMap;
    }

    public List<HProg_Struct_ProgInfo> getSelectData(){
        List<HProg_Struct_ProgInfo> filterData = new ArrayList<>();
        for(int i=0; i<getData().size(); i++){
            if(mSelectMap.get(i)){
                filterData.add(getData().get(i));
            }
        }
        return filterData;
    }

    public void clearSelect() {
        mSelectMap.clear();
        notifyDataSetChanged();
    }
}
