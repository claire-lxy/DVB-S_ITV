package com.konkawise.dtv.adapter.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListViewAdapter<T> extends BaseAdapter {
    protected Context mContext;
    protected LayoutInflater mInflater;
    private int mLayoutResId;
    protected List<T> mDatas;

    public BaseListViewAdapter(Context context, List<T> datas, int layoutResId) {
        this.mContext = context;
        this.mDatas = datas;
        this.mLayoutResId = layoutResId;
        this.mInflater = LayoutInflater.from(context);
        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public T getItem(int position) {
        return getCount() > position ? mDatas.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseListViewHolder holder;
        if (convertView == null) {
            holder = onCreateViewHolder(parent, getItemViewType(position));
            convertView = holder.getConvertView();
            convertView.setTag(holder);
        } else {
            holder = (BaseListViewHolder) convertView.getTag();
        }

        T item = getItem(position);
        if (item != null) {
            convert(holder, position, item);
        }

        return convertView;
    }

    public List<T> getData() {
        return mDatas;
    }

    public void addData(int position, T item) {
        int count = getCount();
        mDatas.add(position > count ? count : position, item);
        notifyDataSetChanged();
    }

    public void removeData(int position) {
        if (position >= getCount()) return;

        mDatas.remove(position);
        notifyDataSetChanged();
    }

    public void addData(List<T> datas) {
        if (datas == null) return;
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void addData(int position, List<T> datas) {
        if (datas == null) return;
        int count = getCount();
        mDatas.addAll(position > count ? count : position, datas);
        notifyDataSetChanged();
    }

    public void updateData(List<T> datas) {
        if (datas == null) {
            datas = new ArrayList<>();
        }
        mDatas = datas;
        notifyDataSetChanged();
    }

    public void updateData(int position, T data) {
        if (data == null || position >= getCount()) return;
        mDatas.set(position, data);
        notifyDataSetChanged();
    }

    public void clearData() {
        mDatas.clear();
        notifyDataSetChanged();
    }

    public BaseListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BaseListViewHolder(mInflater.inflate(mLayoutResId, null));
    }

    protected abstract void convert(BaseListViewHolder holder, int position, T item);
}
