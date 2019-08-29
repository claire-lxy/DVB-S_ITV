package com.konkawise.dtv.base;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class BaseItemFocusChangeDialogFragment extends BaseDialogFragment {
    private ItemFocusChangeDelegate mItemFocusChangeDelegate = new ItemFocusChangeDelegate();

    protected void itemChange(int currSelectItem, int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        mItemFocusChangeDelegate.itemChange(currSelectItem, selectItem, null, ivLeft, ivRight, textView);
    }

    protected void itemChange(int currSelectItem, int selectItem, ViewGroup itemGroup, ImageView ivLeft, ImageView ivRight, TextView textView) {
        mItemFocusChangeDelegate.itemChange(currSelectItem, selectItem, itemGroup, ivLeft, ivRight, textView);
    }
}
