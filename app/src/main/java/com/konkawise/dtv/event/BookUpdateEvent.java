package com.konkawise.dtv.event;

import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;

public class BookUpdateEvent {
    public HSubforProg_t bookInfo;

    public BookUpdateEvent(HSubforProg_t bookInfo) {
        this.bookInfo = bookInfo;
    }
}
