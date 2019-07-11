package com.konkawise.dtv.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * 创建者      lj DELL
 * 创建时间    2019/1/4 17:19
 * 描述      处理焦点返回问题
 * <p>
 * 更新者      $Author$
 * <p>
 * 更新时间    $Date$
 * 更新描述    ${TODO}
 */

public class TVListView extends ListView {

    private int currentSelect = 0;

    public TVListView(Context context) {
        super(context);
    }

    public TVListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TVListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {

        //处理第一次返回的焦点控制
       int lastSelectItem = getSelectedItemPosition();
       currentSelect = lastSelectItem;
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            setSelection(lastSelectItem);
        }
    }

    public int getCurrentSelect(){
        return currentSelect;
    }


}
