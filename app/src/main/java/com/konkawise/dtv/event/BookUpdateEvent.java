package com.konkawise.dtv.event;

import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Struct_Timer;

public class BookUpdateEvent {
    public HBooking_Struct_Timer bookInfo;

    public BookUpdateEvent(HBooking_Struct_Timer bookInfo) {
        this.bookInfo = bookInfo;
    }
}
