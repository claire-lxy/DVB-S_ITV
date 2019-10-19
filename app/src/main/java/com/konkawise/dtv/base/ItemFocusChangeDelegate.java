package com.konkawise.dtv.base;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.annotation.StepType;

public class ItemFocusChangeDelegate {

    public void itemChange(int currSelectItem, int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        itemChange(currSelectItem, selectItem, null, ivLeft, ivRight, textView);
    }

    public void itemChange(int currSelectItem, int selectItem, ViewGroup itemGroup, ImageView ivLeft, ImageView ivRight, TextView textView) {
        if (itemGroup != null) {
            itemGroup.setBackgroundResource(currSelectItem == selectItem ? R.drawable.btn_translate_bg_select_shape : 0);
        }
        if (ivLeft != null) {
            ivLeft.setVisibility(currSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        }
        if (ivRight != null) {
            ivRight.setVisibility(currSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        }
        if (textView != null) {
            textView.setBackgroundResource(currSelectItem == selectItem ? R.drawable.btn_red_bg_shape : 0);
        }
    }

    public int getMinusStep(int currStep, int maxSize) {
        return getMinusStep(currStep, maxSize, 0);
    }

    public int getPlusStep(int currStep, int maxSize) {
        return getPlusStep(currStep, maxSize, maxSize);
    }

    public int getMinusStep(int currStep, int maxSize, int limit) {
        return getSelectStep(Constants.STEP_TYPE_MINUS_STEP, currStep, maxSize, limit);
    }

    public int getPlusStep(int currStep, int maxSize, int limit) {
        return getSelectStep(Constants.STEP_TYPE_PLUS_STEP, currStep, maxSize, limit);
    }

    private int getSelectStep(@StepType int stepType, int currStep, int maxSize, int limit) {
        if (stepType == Constants.STEP_TYPE_PLUS_STEP) {
            currStep = plusStep(currStep);
            if (currStep > limit) currStep = 0;
            return currStep;
        } else if (stepType == Constants.STEP_TYPE_MINUS_STEP) {
            currStep = minusStep(currStep);
            if (currStep < limit) currStep = maxSize;
            return currStep;
        }
        return currStep;
    }

    public int minusStep(int currStep) {
        return minusStep(currStep, 1);
    }

    public int minusStep(int currStep, int step) {
        return currStep - step;
    }

    public int plusStep(int currStep) {
        return plusStep(currStep, 1);
    }

    public int plusStep(int currStep, int step) {
        return currStep + step;
    }
}
