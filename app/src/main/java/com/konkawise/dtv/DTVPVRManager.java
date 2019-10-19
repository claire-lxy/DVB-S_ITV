package com.konkawise.dtv;

import com.sw.dvblib.DTVPVR;

import java.util.ArrayList;

import vendor.konka.hardware.dtvmanager.V1_0.HPVR_Struct_Progress;
import vendor.konka.hardware.dtvmanager.V1_0.HPVR_Struct_RecFile;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_AudDB_t;

public class DTVPVRManager {
    private boolean mRecording;

    private static class DTVPVRManagerHolder {
        private static DTVPVRManager INSTANCE = new DTVPVRManager();
    }

    private DTVPVRManager() {
        DTVPVR.getInstance();
    }

    public static DTVPVRManager getInstance() {
        return DTVPVRManagerHolder.INSTANCE;
    }

    /**
     * 开始录制
     *
     * @param delay 底层启动录制需要时间，首次delay=0，如果失败，delay=1直到成功
     * @return 0：启动成功  -4：正在启动需要继续等待  other:启动失败
     */
    public int startRecord(int delay) {
        return DTVPVR.getInstance().startRecord(delay);
    }

    /**
     * 开始录制
     *
     * @param delay      底层启动录制需要时间，首次delay=0，如果失败，delay=1直到成功
     * @param recordPath 流录制的盘符路径，如/storage/sda1
     * @return 0：启动成功  -4：正在启动需要继续等待  other:启动失败
     */
    public int startRecord(int delay, String recordPath) {
        return DTVPVR.getInstance().startRecordEx(delay, recordPath);
    }

    /**
     * 停止录制
     */
    public int stopRecord() {
        return DTVPVR.getInstance().stopRecord();
    }

    public int getRecordFileNum() {
        return DTVPVR.getInstance().getRecordFileNum(null);
    }

    public ArrayList<HPVR_Struct_RecFile> getRecordFileList(int begin, int limit) {
        return DTVPVR.getInstance().getRecordFileList(null, begin, limit);
    }

    public String getRecordDirName() {
        return DTVPVR.getInstance().getRecordDirName();
    }

    public int startPlay(String path, String fname, int loop) {
        return DTVPVR.getInstance().startPlay(path, fname, loop);
    }

    public int stopPlay() {
        return DTVPVR.getInstance().stopPlay();
    }

    public int beginTimeshift() {
        return DTVPVR.getInstance().beginTimeshift();
    }

    public int stopTimeshift() {
        return DTVPVR.getInstance().stopTimeshift();
    }

    public HPVR_Struct_Progress getPlayProgress() {
        return DTVPVR.getInstance().getPlayProgress();
    }

    public int playSeek(int time_ms) {
        return DTVPVR.getInstance().playSeek(time_ms);
    }

    public int setPlaySpeed(int speed) {
        return DTVPVR.getInstance().setPlaySpeed(speed);
    }

    public int playPause() {
        return DTVPVR.getInstance().playPause();
    }

    public int playResume() {
        return DTVPVR.getInstance().playResume();
    }

    public int lockRecordFile(String path, String fname, int lock) {
        return DTVPVR.getInstance().lockRecordFile(path, fname, lock);
    }

    public HProg_Struct_AudDB_t getAudioList() {
        return DTVPVR.getInstance().getAudioList();
    }

    public int getCurrAudioIndex() {
        return DTVPVR.getInstance().getCurrAudioIndex();
    }

    public int setAudioPid(int audPID, int audioType) {
        return DTVPVR.getInstance().setAudioPid(audPID, audioType);
    }

    public int injectSubTTXAudio(String path, String fname) {
        return DTVPVR.getInstance().injectSubTTXAudio(path, fname);
    }

    public void setRecording(boolean recording) {
        this.mRecording = recording;
    }

    public boolean isRecording() {
        return mRecording;
    }
}
