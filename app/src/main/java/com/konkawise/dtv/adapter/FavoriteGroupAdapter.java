package com.konkawise.dtv.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.konkawise.dtv.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FavoriteGroupAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mFavoriteGroupList;
    private int mSelectPosition;
    private boolean mFocus;
    private SparseArray<FavoriteViewHolder> mViewHolderMap = new SparseArray<>();

    public FavoriteGroupAdapter(Context context, List<String> favoriteGroupList) {
        mContext = context;
        mFavoriteGroupList = favoriteGroupList;
    }

    @Override
    public int getCount() {
        return mFavoriteGroupList.size();
    }

    @Override
    public String getItem(int position) {
        return mFavoriteGroupList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FavoriteViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.favorite_group_list_item, null);
            viewHolder = new FavoriteViewHolder(convertView);
            mViewHolderMap.put(position, viewHolder);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (FavoriteViewHolder) convertView.getTag();
        }

        if (mSelectPosition == position && mFocus) {
            viewHolder.ll_root_group.setBackgroundColor(mContext.getResources().getColor(R.color.channel_edit_gray));
        } else {
            viewHolder.ll_root_group.setBackgroundColor(0);
        }
        viewHolder.tv_fav_group_num.setText(String.valueOf(position + 1));
        viewHolder.tv_fav_group_type.setText(getItem(position));

        return convertView;
    }

    public void updateFavoriteGroupName(int position, String newGroupName) {
        FavoriteViewHolder viewHolder = mViewHolderMap.get(position);
        if (viewHolder != null) {
            viewHolder.tv_fav_group_type.setText(newGroupName);
        }
    }

    public void setSelectPosition(int position) {
        mSelectPosition = position;
    }

    public void setFocus(boolean focus) {
        mFocus = focus;
    }

    public class FavoriteViewHolder {
        @BindView(R.id.tv_fav_group_index)
        TextView tv_fav_group_num;

        @BindView(R.id.tv_fav_group_type)
        TextView tv_fav_group_type;

        @BindView(R.id.ll_root_group)
        LinearLayout ll_root_group;

        FavoriteViewHolder(View convertView) {
            ButterKnife.bind(this, convertView);
        }
    }
}

