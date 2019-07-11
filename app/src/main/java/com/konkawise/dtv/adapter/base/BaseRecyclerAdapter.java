package com.konkawise.dtv.adapter.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseRecyclerHolder> {
    protected Context mContext;
    protected LayoutInflater mInflater;
    protected List<T> mDatas;
    private int mLayoutResId;

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private OnItemSelectedListener mOnItemSelectedListener;

    public BaseRecyclerAdapter(Context context, List<T> datas, int layoutResId) {
        this.mContext = context;
        this.mDatas = datas;
        this.mLayoutResId = layoutResId;
        this.mInflater = LayoutInflater.from(context);

        if (mDatas == null) {
            mDatas = new ArrayList<>();
        }
    }

    @NonNull
    @Override
    public BaseRecyclerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BaseRecyclerHolder(mInflater.inflate(mLayoutResId, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final BaseRecyclerHolder holder, final int position) {
        T item = getItem(position);
        if (item != null) {
            convert(holder, position, item);
        }

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.onItemClick(holder, holder.getAdapterPosition());
                }
            });
        }

        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mOnItemLongClickListener.onItemLongClick(holder, holder.getAdapterPosition());
                    return true;
                }
            });
        }

        if (mOnItemSelectedListener != null) {
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    mOnItemSelectedListener.onItemSelected(holder, hasFocus, holder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public T getItem(int position) {
        return getItemCount() > position ? mDatas.get(position) : null;
    }

    public List<T> getData() {
        return mDatas;
    }

    public void onItemChange(T obj) {
        int index = mDatas.indexOf(obj);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    public void onItemChange(int index) {
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    public void addData(int position, T item) {
        int count = getItemCount();
        mDatas.add(position > count ? count : position, item);
        notifyItemInserted(position);
    }

    public void removeData(int position) {
        if (position >= getItemCount()) return;

        mDatas.remove(position);
        notifyItemRemoved(position);
    }

    public void addData(List<T> datas) {
        int start = mDatas.size();
        mDatas.addAll(datas);
        notifyItemRangeInserted(start, datas.size());
    }

    public void updateData(List<T> datas) {
        if (datas == null) {
            datas = new ArrayList<>();
        }
        mDatas = datas;
        notifyDataSetChanged();
    }

    public void claerData() {
        mDatas.clear();
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mOnItemSelectedListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(BaseRecyclerHolder holder, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(BaseRecyclerHolder holder, int position);
    }

    public interface OnItemSelectedListener {
        void onItemSelected(BaseRecyclerHolder holder, boolean hasFocus, int position);
    }

    protected abstract void convert(BaseRecyclerHolder holder, int position, T item);
}
