package com.konkawise.dtv.ui;


import com.konkawise.dtv.R;

import android.app.Activity;
import android.support.annotation.IntDef;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;

public class SmallHintBox {
    public static final int CHECK_TYPE_DATABASE = 0;
    public static final int CHECK_TYPE_SIGNAL = 1;

    @IntDef(flag = true, value = {
            CHECK_TYPE_DATABASE, CHECK_TYPE_SIGNAL
    })
    private @interface CheckType {

    }

    private Activity activity;
    private RelativeLayout reLayout;
    private TextView TVbox;

    public SmallHintBox(Activity act) {
        activity = act;
        reLayout = (RelativeLayout) activity.findViewById(R.id.SmallBox);

        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.FILL_PARENT);
        TVbox = new TextView(activity);
        reLayout.addView(TVbox, param);
        TVbox.setGravity(Gravity.CENTER);
        TVbox.setTextSize(22);
        TVbox.setTextColor(Color.WHITE);
        TVbox.setVisibility(View.GONE);
    }

    private void SmallBoxShow(String str) {
        if (TVbox.getVisibility() != View.VISIBLE) {
            TVbox.setVisibility(View.VISIBLE);
            TVbox.setText(str);
        }
    }

    private void SmallBoxHide() {
        if (TVbox.getVisibility() != View.GONE) {
            TVbox.setVisibility(View.GONE);
            TVbox.setText("");
        }
    }

    public void hintBox(@CheckType int checkType, boolean enable) {
        if (!enable) {
            SmallBoxShow(activity.getString(checkType == CHECK_TYPE_DATABASE ? R.string.topmost_program_empty : R.string.topmost_no_signal));
        } else {
            SmallBoxHide();
        }
    }
}
