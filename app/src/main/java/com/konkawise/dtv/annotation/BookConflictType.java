package com.konkawise.dtv.annotation;

import android.support.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.BOOK_CONFLICT_LIMIT, Constants.BOOK_CONFLICT_NONE,
        Constants.BOOK_CONFLICT_ADD, Constants.BOOK_CONFLICT_REPLACE
})
public @interface BookConflictType {
}
