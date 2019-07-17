package com.konkawise.dtv.event;

public class ProgramUpdateEvent {
    public int tvSize;
    public int radioSize;
    public boolean isProgramEdit;

    public ProgramUpdateEvent(int tvSize, int radioSize) {
        this.tvSize = tvSize;
        this.radioSize = radioSize;
    }

    public ProgramUpdateEvent(boolean isProgramEdit) {
        this.isProgramEdit = isProgramEdit;
    }
}
