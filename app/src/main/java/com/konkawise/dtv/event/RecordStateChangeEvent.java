package com.konkawise.dtv.event;

public class RecordStateChangeEvent {
    public boolean isRecording;

    public RecordStateChangeEvent(boolean isRecording) {
        this.isRecording = isRecording;
    }
}
