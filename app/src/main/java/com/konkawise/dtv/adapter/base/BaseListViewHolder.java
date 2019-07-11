package com.konkawise.dtv.adapter.base;

import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseListViewHolder {
    private final SparseArray<View> mViews = new SparseArray<>();
    private View mItemView;

    public BaseListViewHolder(View convertView) {
        this.mItemView = convertView;
    }

    public View getConvertView() {
        return mItemView;
    }

    public BaseListViewHolder setText(int viewId, String text) {
        TextView textView = getTextView(viewId);
        textView.setText(text);
        return this;
    }

    public BaseListViewHolder setTextSize(int viewId, float textSize) {
        TextView textView = getTextView(viewId);
        textView.setTextSize(textSize);
        return this;
    }

    public BaseListViewHolder setImageResource(int viewId, int drawableId) {
        ImageView imageView = getImageView(viewId);
        imageView.setImageResource(drawableId);
        return this;
    }

    public BaseListViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView imageView = getImageView(viewId);
        imageView.setImageBitmap(bitmap);
        return this;
    }

    public BaseListViewHolder setBackgroundResource(int viewId, int resId) {
        View view = getView(viewId);
        view.setBackgroundResource(resId);
        return this;
    }

    public BaseListViewHolder setVisibility(int viewId, int visibility) {
        View view = getView(viewId);
        view.setVisibility(visibility);
        return this;
    }

    public TextView getTextView(int viewId) {
        return getView(viewId);
    }

    public ImageView getImageView(int viewId) {
        return getView(viewId);
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = mItemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }
}
