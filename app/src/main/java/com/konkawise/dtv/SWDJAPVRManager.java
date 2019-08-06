package com.konkawise.dtv;

import com.sw.dvblib.DJAPVR;

import java.util.ArrayList;

import vendor.konka.hardware.dtvmanager.V1_0.HPVR_RecFile_t;

public class SWDJAPVRManager {
    private boolean mRecording;

    private static class SWDJAPVRManagerHolder {
        private static SWDJAPVRManager INSTANCE = new SWDJAPVRManager();
    }

    private SWDJAPVRManager() {
        DJAPVR.CreateInstance();
    }

    public static SWDJAPVRManager getInstance() {
        return SWDJAPVRManagerHolder.INSTANCE;
    }

    public int startRecord(int delay) {
        return DJAPVR.CreateInstance().startRecord(delay);
    }

    public int stopRecord() {
        return DJAPVR.CreateInstance().stopRecord();
    }

    public int getRecordFileNum() {
        return DJAPVR.CreateInstance().getRecordFileNum();
    }

    public ArrayList<HPVR_RecFile_t> getRecordFileList(int begin, int limit) {
        return DJAPVR.CreateInstance().getRecordFileList(begin, limit);
    }

    public int startPlay(String path, String fname, int loop) {
        return DJAPVR.CreateInstance().startPlay(path, fname, loop);
    }

    public int stopPlay() {
        return DJAPVR.CreateInstance().stopPlay();
    }

    public void setRecording(boolean recording) {
        this.mRecording = recording;
    }

    public boolean isRecording() {
        return mRecording;
    }
}
