package com.konkawise.dtv.dialog;

import android.util.SparseBooleanArray;

public interface OnCheckGroupCallback {
    void callback(SparseBooleanArray checkMap);

    void cancel();
}
