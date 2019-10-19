package com.konkawise.dtv;

import android.view.Surface;

import com.sw.dvblib.DTVCommon;
import com.sw.dvblib.DTVPlayer;

import vendor.konka.hardware.dtvmanager.V1_0.HPlayer_Struct_ProgInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HPlayer_Struct_Subtitle;
import vendor.konka.hardware.dtvmanager.V1_0.HPlayer_Struct_Teletext;

public class DTVPlayerManager {

    private static class DTVPlayerManagerHolder {
        private static final DTVPlayerManager INSTANCE = new DTVPlayerManager();
    }

    private DTVPlayerManager() {
        DTVPlayer.getInstance();
    }

    public static DTVPlayerManager getInstance() {
        return DTVPlayerManagerHolder.INSTANCE;
    }

    /**
     * 清空频道
     */
    public void programReset() {
        DTVCommon.getInstance().programReset(0);
    }

    /**
     * 获取Subtitle数量
     */
    public int getSubtitleNum(int serviceid) {
        return DTVPlayer.getInstance().getSubtitleNum(serviceid);
    }

    /**
     * 获取Subtitle信息
     */
    public HPlayer_Struct_Subtitle getSubtitleInfo(int serviceid, int index) {
        return DTVPlayer.getInstance().getSubtitleInfo(serviceid, index);
    }

    /**
     * 获取当前Subtitle信息
     */
    public HPlayer_Struct_Subtitle getCurSubtitleInfo(int serviceid) {
        return DTVPlayer.getInstance().getCurSubtitleInfo(serviceid);
    }

    /**
     * 开启Subtitle
     */
    public void openSubtitle(int pid) {
        DTVPlayer.getInstance().openSubtitle(pid);
    }

    /**
     * 获取Teletext数量
     */
    public int getTeletextNum(int serviceid) {
        return DTVPlayer.getInstance().getTeletxtNum(serviceid);
    }

    /**
     * 获取Teletext信息
     */
    public HPlayer_Struct_Teletext getTeletextInfo(int serviceid, int index) {
        return DTVPlayer.getInstance().getTeletextInfo(serviceid, index);
    }

    /**
     * 开启Teletext
     */
    public void openTeletext(int pid) {
        DTVPlayer.getInstance().openTeletext(pid);
    }

    /**
     * 根据type获取当前频道音轨参数
     *
     * @param type SWFta.OSDFTA_TRACK or SWFta.OSDFTA_AUDIO
     */
    public int getCurrProgParam(int type) {
        return DTVPlayer.getInstance().getCurrProgParam(type);
    }

    /**
     * 设置频道音轨参数
     */
    public void setCurrProgParam(int type, int value) {
        DTVPlayer.getInstance().setCurrProgParam(type, value);
    }

    /**
     * 获取Booking即将播放的频道
     */
    public HPlayer_Struct_ProgInfo getCurrPlayInfo(int param) {
        return DTVPlayer.getInstance().getCurrPlayInfo(param);
    }

    /**
     * 根据serviceid、tsid和sat播放book
     */
    public void forcePlayProgByServiceId(int serviceId, int tsid, int sat) {
        DTVPlayer.getInstance().forcePlayProgByServiceID(serviceId, tsid, sat);
    }

    /**
     * 设置Surface
     */
    public void setSurface(Surface surface) {
        DTVPlayer.getInstance().setVideoSurface(surface);
    }

    /**
     * 设置Surface Window大小
     */
    public void setWindowSize(int x, int y, int w, int h) {
        DTVPlayer.getInstance().setWindowSize(x, y, w, h);
    }

    /**
     * 暂停播放
     */
    public void stopPlay(int black) {
        DTVPlayer.getInstance().stopNewChannel(black);
    }

    /**
     * 根据节目号播放节目
     *
     * @param progNo 节目号
     */
    public void startPlayProgNo(int progNo, int condition) {
        DTVProgramManager.getInstance().setCurrProgNo(progNo);
        DTVPlayer.getInstance().nowPlayCurrProg(condition);
    }
}
