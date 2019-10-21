package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.BookType.ADD, Constants.BookType.EDIT
})
public @interface BookType {
}
