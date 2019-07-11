package com.konkawise.dtv.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class TopmostLayout extends ViewGroup {
    private static final String TAG = "TopmostLayout";

    public TopmostLayout(Context context) {
        this(context, null);
    }

    public TopmostLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopmostLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setClickable(true);

        int widthOfContent = 0;//content的宽度
        int heightOfContent = 0;//content的高度
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.setClickable(true);
            if (childView.getVisibility() == GONE)
                continue;
            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
        }
        widthOfContent = getMeasuredWidth();
        heightOfContent = getMeasuredHeight();
        setMeasuredDimension(widthOfContent, heightOfContent);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i(TAG,"left:"+l);
        int childCount = getChildCount();
        int left = l;
        int right = r;
        for(int i=0; i<childCount; i++){
            View childView = getChildAt(i);
            if (childView.getVisibility() == GONE)
                continue;
            if(i == 0){
                childView.layout(left, getPaddingTop(), left + childView.getMeasuredWidth(), getPaddingTop() + childView.getMeasuredHeight());
            }else{
                Log.i(TAG,"child left:"+(left - childView.getMeasuredWidth()));
                childView.layout(left - childView.getMeasuredWidth(), getPaddingTop(), left, getPaddingTop() + childView.getMeasuredHeight());
                left = left - childView.getMeasuredWidth();
            }
        }
    }
}
