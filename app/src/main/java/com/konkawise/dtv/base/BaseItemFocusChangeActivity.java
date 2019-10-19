package com.konkawise.dtv.base;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseItemFocusChangeActivity extends BaseActivity {
    private ItemFocusChangeDelegate mItemFocusChangeDelegate = new ItemFocusChangeDelegate();

    protected void itemChange(int currSelectItem, int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        mItemFocusChangeDelegate.itemChange(currSelectItem, selectItem, ivLeft, ivRight, textView);
    }

    protected void itemChange(int currSelectItem, int selectItem, ViewGroup itemGroup, ImageView ivLeft, ImageView ivRight, TextView textView) {
        mItemFocusChangeDelegate.itemChange(currSelectItem, selectItem, itemGroup, ivLeft, ivRight, textView);
    }

    protected int getMinusStep(int currStep, int maxSize) {
        return getMinusStep(currStep, maxSize, 0);
    }

    protected int getPlusStep(int currStep, int maxSize) {
        return getPlusStep(currStep, maxSize, maxSize);
    }

    protected int getMinusStep(int currStep, int maxSize, int limit) {
        return mItemFocusChangeDelegate.getMinusStep(currStep, maxSize, limit);
    }

    protected int getPlusStep(int currStep, int maxSize, int limit) {
        return mItemFocusChangeDelegate.getPlusStep(currStep, maxSize, limit);
    }
}
