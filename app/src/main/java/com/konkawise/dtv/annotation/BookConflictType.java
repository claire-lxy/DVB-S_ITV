package com.konkawise.dtv.annotation;

import android.support.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.BookConflictType.LIMIT, Constants.BookConflictType.NONE,
        Constants.BookConflictType.ADD, Constants.BookConflictType.REPLACE
})
public @interface BookConflictType {
}
