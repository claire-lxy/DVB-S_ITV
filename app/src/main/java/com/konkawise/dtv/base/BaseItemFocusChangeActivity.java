package com.konkawise.dtv.base;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.annotation.StepType;

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
        return getSelectStep(Constants.STEP_TYPE_MINUS_STEP, currStep, maxSize, limit);
    }

    protected int getPlusStep(int currStep, int maxSize, int limit) {
        return getSelectStep(Constants.STEP_TYPE_PLUS_STEP, currStep, maxSize, maxSize);
    }

    private int getSelectStep(@StepType int stepType, int currStep, int maxSize, int limit) {
        if (stepType == Constants.STEP_TYPE_PLUS_STEP) {
            currStep = mItemFocusChangeDelegate.plusStep(currStep);
            if (currStep > limit) currStep = 0;
            return currStep;
        } else if (stepType == Constants.STEP_TYPE_MINUS_STEP) {
            currStep = mItemFocusChangeDelegate.minusStep(currStep);
            if (currStep < limit) currStep = maxSize;
            return currStep;
        }
        return currStep;
    }
}
