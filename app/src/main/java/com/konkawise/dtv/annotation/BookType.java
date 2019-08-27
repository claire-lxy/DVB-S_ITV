package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.BOOK_TYPE_ADD, Constants.BOOK_TYPE_EDIT
})
public @interface BookType {
}
