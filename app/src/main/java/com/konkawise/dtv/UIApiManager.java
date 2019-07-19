package com.konkawise.dtv;

import android.view.Surface;
import android.view.SurfaceHolder;

import com.sw.dvblib.SWFta;
import com.sw.dvblib.UIAPI;

import java.util.ArrayList;

import vendor.konka.hardware.dtvmanager.V1_0.EpgEvent_t;

public class UIApiManager {

    private static class UIApiManagerHolder {
        private static final UIApiManager INSTANCE = new UIApiManager();
    }

    private UIApiManager() {
        UIAPI.CreateInstance();
    }

    public static UIApiManager getInstance() {
        return UIApiManagerHolder.INSTANCE;
    }

    public void palyTv(int condition, SurfaceHolder surfaceHolder) {
        UIAPI.CreateInstance().setSurface(surfaceHolder.getSurface());
        UIAPI.CreateInstance().startPlayCurrProg(condition);
    }

    /**
     * 设置Surface
     */
    public void setSurface(Surface surface) {
        UIAPI.CreateInstance().setSurface(surface);
    }

    /**
     * 设置Surface Window大小
     */
    public void setWindowSize(int x, int y, int w, int h) {
        UIAPI.CreateInstance().setWindowSize(x, y, w, h);
    }

    /**
     * 暂停播放
     */
    public void stopPlay(int black) {
        UIAPI.CreateInstance().stopPlay(black);
    }

    /**
     * 根据节目号播放节目
     *
     * @param progNo 节目号
     */
    public void startPlayProgNo(int progNo, int condition) {
//        UIAPI.CreateInstance().startPlayProgNo(progNo, condition);
        SWPDBaseManager.getInstance().setCurrProgNo(progNo);
        SWFta.CreateInstance().nowPlayCurrProg(condition);

    }

    /**
     * 获取音频轨大小 0~2
     */
    public int getCurrProgTrack() {
        return UIAPI.CreateInstance().getCurrProgTrack();
    }

    /**
     * 设置音频轨大小
     */
    public void setCurrProgTrack(int value) {
        UIAPI.CreateInstance().setCurrProgTrack(value);
    }

    /**
     * 根据日期索引获取当天正在播放或未播放的EPG频道
     *
     * @param dayIndex 0~6
     */
    public ArrayList<EpgEvent_t> getCurrProgSchInfo(int dayIndex) {
        return UIAPI.CreateInstance().getCurrProgSchInfo(dayIndex);
    }

    /**
     * PfBar播放当前节目信息
     *
     * @param index 0代表information0 1代表information1
     */
    public EpgEvent_t getCurrProgPFInfo(int index) {
        return UIAPI.CreateInstance().getCurrProgPFInfo(index);
    }
}
