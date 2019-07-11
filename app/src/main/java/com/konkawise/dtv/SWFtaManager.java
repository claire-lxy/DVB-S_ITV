package com.konkawise.dtv;

import com.sw.dvblib.SWFta;

import vendor.konka.hardware.dtvmanager.V1_0.HPDPPlayInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.ScanProgress_t;

public class SWFtaManager {

    private static class SWFtaManagerHolder {
        private static final SWFtaManager INSTANCE = new SWFtaManager();
    }

    private SWFtaManager() {
        SWFta.CreateInstance();
    }

    public static SWFtaManager getInstance() {
        return SWFtaManagerHolder.INSTANCE;
    }

    public void tunerLockFreq(int sat, int freq, int rate, int qam, int must, int centiSec) {
        SWFta.CreateInstance().tunerLockFreq(sat, freq, rate, qam, must, centiSec);
    }

    /**
     * 是否打开Parental Lock
     */
    public boolean isOpenParentLock() {
        return getCommE2PInfo(SWFta.E_E2PP.E2P_cParentLock.ordinal()) == 1;
    }

    /**
     * 是否打开Menu Lock
     */
    public boolean isOpenMenuLock() {
        return getCommE2PInfo(SWFta.E_E2PP.E2P_cMenuLock.ordinal()) == 1;
    }

    /**
     * 保存Setting参数
     */
    public void setCommE2PInfo(int param, int value) {
        SWFta.CreateInstance().setCommE2PInfo(param, value);
    }

    /**
     * 获取Setting参数
     */
    public int getCommE2PInfo(int param) {
        return SWFta.CreateInstance().getCommE2PInfo(param);
    }

    /**
     * 获取Parental Lock或Menu Lock的密码
     */
    public String getCommPWDInfo(int param) {
        return SWFta.CreateInstance().getCommPWDInfo(param);
    }

    /**
     * 设置Parental Lock或Menu Lock的密码
     */
    public void setCommPWDInfo(int param, String password) {
        SWFta.CreateInstance().setCommPWDInfo(param, password);
    }

    /**
     * 开始盲扫
     *
     * @param sat 卫星索引
     */
    public void blindScanStart(int sat) {
        SWFta.CreateInstance().blindScanStart(sat);
    }

    /**
     * 停止盲扫
     */
    public void blindScanStop() {
        SWFta.CreateInstance().blindScanStoped();
    }

    public int getCurrScanMode() {
        return getCommE2PInfo(SWFta.E_E2PP.E2P_ScanMode.ordinal());
    }

    public int getCurrNetwork() {
        return getCommE2PInfo(SWFta.E_E2PP.E2P_Network.ordinal());
    }

    public int getCurrCAS() {
        return getCommE2PInfo(SWFta.E_E2PP.E2P_CAS.ordinal());
    }

    /**
     * 清空频道
     */
    public void clearChannel() {
        SWFta.CreateInstance().DBaseReset(0);
    }

    /**
     * 获取Subtitle数量
     */
    public int getSubtitleNum(int serviceid) {
        return SWFta.CreateInstance().getSubtitleNum(serviceid);
    }

    /**
     * 获取Teletext数量
     */
    public int getTeletxtNum(int serviceid) {
        return SWFta.CreateInstance().getTeletxtNum(serviceid);
    }

    /**
     * 获取Booking即将播放的频道
     */
    public HPDPPlayInfo_t getCurrPlayInfo(int param) {
        return SWFta.CreateInstance().getCurrPlayInfo(param);
    }

    public long dismissTimeout() {
        return getCommE2PInfo(SWFta.E_E2PP.E2P_PD_dispalytime.ordinal()) * 1000;
    }

    /**
     * Auto DiSEqC指定端口锁频
     *
     * @param portIndex 端口索引，portIndex=0~4代表DiSEqC A~D
     */
    public int tunerLockFreqDiSEqC(int sat, int freq, int rate, int qam, int portIndex) {
        return SWFta.CreateInstance().tunerLockFreqDiSEqC(sat, freq, rate, qam, portIndex);
    }

    /**
     * Auto DiSEqC是否锁住频，需要多次调用延时判断获取当前锁频状态
     */
    public boolean tunerIsLocked() {
        return SWFta.CreateInstance().tunerIsLocked() == 1;
    }

    /**
     * 盲扫回调进度，按周期循环调用获取进度
     */
    public ScanProgress_t blindScanProgress() {
        return SWFta.CreateInstance().blindScanProgress();
    }
}
