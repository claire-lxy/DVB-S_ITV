package com.konkawise.dtv.bean;

import com.konkawise.dtv.annotation.BookConflictType;

import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Struct_Timer;

public class BookParameterModel {
    @BookConflictType
    public int bookConflict;

    public BookingModel bookingModel;
    public HBooking_Struct_Timer conflictBookProg;

    @Override
    public String toString() {
        return "BookParameterModel{" +
                "bookConflict=" + bookConflict +
                ", bookingModel=" + bookingModel +
                ", conflictBookProg=" + conflictBookProg +
                '}';
    }
}
