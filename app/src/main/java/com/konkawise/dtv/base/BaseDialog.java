package com.konkawise.dtv.base;

import android.app.Dialog;
import android.content.Context;

import com.konkawise.dtv.R;

import butterknife.ButterKnife;

public abstract class BaseDialog extends Dialog {
    public BaseDialog(Context context) {
        super(context, R.style.konka_dvb_dialog);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
    }

    protected abstract int getLayoutId();
}
