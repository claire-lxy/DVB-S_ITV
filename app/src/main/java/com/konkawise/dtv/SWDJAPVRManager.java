package com.konkawise.dtv;

import com.sw.dvblib.DJAPVR;

import java.util.ArrayList;

import vendor.konka.hardware.dtvmanager.V1_0.HPVR_Progress_t;
import vendor.konka.hardware.dtvmanager.V1_0.HPVR_RecFile_t;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_AudDB_t;

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

    /**
     * 开始录制
     *
     * @param delay 底层启动录制需要时间，首次delay=0，如果失败，delay=1直到成功
     * @return 0：启动成功  -4：正在启动需要继续等待  other:启动失败
     */
    public int startRecord(int delay) {
        return DJAPVR.CreateInstance().startRecord(delay);
    }

    /**
     * 开始录制
     *
     * @param delay      底层启动录制需要时间，首次delay=0，如果失败，delay=1直到成功
     * @param recordPath 流录制的盘符路径，如/storage/sda1
     * @return 0：启动成功  -4：正在启动需要继续等待  other:启动失败
     */
    public int startRecord(int delay, String recordPath) {
        return DJAPVR.CreateInstance().startRecordEx(delay, recordPath);
    }

    /**
     * 停止录制
     */
    public int stopRecord() {
        return DJAPVR.CreateInstance().stopRecord();
    }

    public int getRecordFileNum() {
        return DJAPVR.CreateInstance().getRecordFileNum(null);
    }

    public ArrayList<HPVR_RecFile_t> getRecordFileList(int begin, int limit) {
        return DJAPVR.CreateInstance().getRecordFileList(null, begin, limit);
    }

    public int startPlay(String path, String fname, int loop) {
        return DJAPVR.CreateInstance().startPlay(path, fname, loop);
    }

    public int stopPlay() {
        return DJAPVR.CreateInstance().stopPlay();
    }

    public int beginTimeshift() {
        return DJAPVR.CreateInstance().beginTimeshift();
    }

    public int stopTimeshift() {
        return DJAPVR.CreateInstance().stopTimeshift();
    }

    public HPVR_Progress_t getPlayProgress() {
        return DJAPVR.CreateInstance().getPlayProgress();
    }

    public int playSeek(int time_ms) {
        return DJAPVR.CreateInstance().playSeek(time_ms);
    }

    public int setPlaySpeed(int speed) {
        return DJAPVR.CreateInstance().setPlaySpeed(speed);
    }

    public int playPause() {
        return DJAPVR.CreateInstance().playPause();
    }

    public int playResume() {
        return DJAPVR.CreateInstance().playResume();
    }

    public int lockRecordFile(String path, String fname, int lock) {
        return DJAPVR.CreateInstance().lockRecordFile(path, fname, lock);
    }

    public HProg_AudDB_t getAudioList() {
        return DJAPVR.CreateInstance().getAudioList();
    }

    public int getCurrAudioIndex() {
        return DJAPVR.CreateInstance().getCurrAudioIndex();
    }

    public int setAudioPid(int audPID, int audioType) {
        return DJAPVR.CreateInstance().setAudioPid(audPID, audioType);
    }

    public int injectSubTTXAudio(String path, String fname) {
        return DJAPVR.CreateInstance().injectSubTTXAudio(path, fname);
    }

    public void setRecording(boolean recording) {
        this.mRecording = recording;
    }

    public boolean isRecording() {
        return mRecording;
    }
}
