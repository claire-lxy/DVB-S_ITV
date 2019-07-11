package com.konkawise.dtv.adapter.base;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseRecyclerHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> mViews = new SparseArray<>();

    public BaseRecyclerHolder(View itemView) {
        super(itemView);
    }

    public TextView getTextView(int viewId) {
        return getView(viewId);
    }

    public BaseRecyclerHolder setText(int viewId, String text) {
        TextView textView = getTextView(viewId);
        textView.setText(text);
        return this;
    }

    public BaseRecyclerHolder setImageResource(int viewId, int drawableId) {
        ImageView imageView = getImageView(viewId);
        imageView.setImageResource(drawableId);
        return this;
    }

    public BaseRecyclerHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView imageView = getImageView(viewId);
        imageView.setImageBitmap(bitmap);
        return this;
    }

    public BaseRecyclerHolder setBackgroundResource(int viewId, int resId) {
        View view = getView(viewId);
        view.setBackgroundResource(resId);
        return this;
    }

    public BaseRecyclerHolder setVisibility(int viewId, int visibility) {
        View view = getView(viewId);
        view.setVisibility(visibility);
        return this;
    }

    public ImageView getImageView(int viewId) {
        return getView(viewId);
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T getView(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }
}
