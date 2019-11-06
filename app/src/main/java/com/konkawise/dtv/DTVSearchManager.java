package com.konkawise.dtv;

import android.support.annotation.IntDef;

import com.sw.dvblib.DTVSearch;

import java.util.ArrayList;

import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Struct_ProgNumStat;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Struct_Progress;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Struct_TP;

public class DTVSearchManager {
    public static final int SIGNAL_STRENGTH = 0;
    public static final int SIGNAL_QUALITY = 1;

    @IntDef(flag = true, value = {
            SIGNAL_STRENGTH, SIGNAL_QUALITY
    })
    private @interface SignalStatus {
    }

    private static class DTVSearchManagerHolder {
        private static final DTVSearchManager INSTANCE = new DTVSearchManager();
    }

    private DTVSearchManager() {
        DTVSearch.getInstance();
    }

    public static DTVSearchManager getInstance() {
        return DTVSearchManagerHolder.INSTANCE;
    }

    /**
     * 获取信号
     *
     * @param signalStatus signalStatus=0表示strength，signalStatus=1表示quality
     */
    public int getSignalStatus(@SignalStatus int signalStatus) {
        return DTVSearch.getInstance().getSignalStatus(signalStatus);
    }

    /**
     * 根据搜索获取的参数获取频道列表
     */
    public ArrayList<HProg_Struct_ProgBasicInfo> getTsSearchResInfo(int sat, int freq, int symbol, int qam, int plpid) {
        HSearch_Struct_TP tp = new HSearch_Struct_TP();
        tp.Sat = sat;
        tp.Freq = freq;
        tp.Rate = symbol;
        tp.Qam = qam;
        tp.plpId = plpid;
        return DTVSearch.getInstance().getTsSearchResInfo(tp);
    }

    public void searchByOneTS(int Sat, int Freq, int Symbol, int Qam, int scanMode, int nitOpen, int caFilter) {
        DTVSearch.getInstance().searchByOneTS(Sat, Freq, Symbol, Qam, scanMode, nitOpen, caFilter);
    }

    public void searchByNet(int Sat, int scanMode, int nitOpen, int caFilter) {
        DTVSearch.getInstance().searchByNET(Sat, scanMode, nitOpen, caFilter);
    }

    public void searchByNet(int Sat, ArrayList<HSearch_Struct_TP> psList, int scanMode, int nitOpen, int caFilter) {
        DTVSearch.getInstance().searchByNET(Sat, psList, scanMode, nitOpen, caFilter);
    }

    public void searchByNIT(int sat, int freq, int symbol, int qam, int scanMode, int nitOpen, int caFilter) {
        DTVSearch.getInstance().searchByNIT(sat, freq, symbol, qam, scanMode, nitOpen, caFilter);
    }

    /**
     * 停止搜索
     *
     * @param storeProgram storeProgram=0表示不保存，storeProgram=1表示保存
     */
    public int searchStop(boolean storeProgram) {
        return DTVSearch.getInstance().searchStop(storeProgram);
    }

    public int searchStop(Boolean storeProgram, int storeType) {
        return DTVSearch.getInstance().searchStop(storeProgram, storeType);
    }

    public HSearch_Struct_ProgNumStat getProgNumOfThisSarch(int sat, int freq) {
        return DTVSearch.getInstance().getProgNumOfThisSarch(sat, freq);
    }

    public void tunerLockFreq(int sat, int freq, int rate, int qam, int must, int centiSec) {
        HSearch_Struct_TP tp = new HSearch_Struct_TP();
        tp.Sat = sat;
        tp.Freq = freq;
        tp.Rate = rate;
        tp.Qam = qam;
        DTVSearch.getInstance().tunerLockFreq(tp, must, centiSec);
    }

    /**
     * 开始盲扫
     *
     * @param sat 卫星索引
     */
    public void blindScanStart(int sat) {
        DTVSearch.getInstance().blindScanStart(sat);
    }

    /**
     * 停止盲扫
     */
    public int blindScanStop() {
        DTVSearch.getInstance().blindScanStoped();
        return 0;
    }

    /**
     * Auto DiSEqC指定端口锁频
     *
     * @param portIndex 端口索引，portIndex=0~4代表DiSEqC DISEQC_A~D
     */
    public int tunerLockFreqDiSEqC(int sat, int freq, int rate, int qam, int portIndex) {
        HSearch_Struct_TP tp = new HSearch_Struct_TP();
        tp.Sat = sat;
        tp.Freq = freq;
        tp.Rate = rate;
        tp.Qam = qam;
        return DTVSearch.getInstance().tunerLockFreqDiSEqC(tp, portIndex);
    }

    /**
     * Auto DiSEqC是否锁住频，需要多次调用延时判断获取当前锁频状态
     */
    public boolean tunerIsLocked() {
        return DTVSearch.getInstance().tunerIsLocked() == 1;
    }

    /**
     * 盲扫回调进度，按周期循环调用获取进度
     */
    public HSearch_Struct_Progress blindScanProgress() {
        return DTVSearch.getInstance().blindScanProgress();
    }

    public int tunerMotorControl(int ctrlCode, int repeat, int[] data) {
        return DTVSearch.getInstance().tunerMotorControl(ctrlCode, repeat, data);
    }

}
