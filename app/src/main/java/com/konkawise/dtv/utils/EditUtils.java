package com.konkawise.dtv.utils;

import android.widget.EditText;

public class EditUtils {

    public static String getEditSubstring(EditText editText) {
        String s = editText.getText().toString();
        if (s.length() > 0) {
            return s.substring(0, s.length() - 1);
        }
        return "";
    }
}
