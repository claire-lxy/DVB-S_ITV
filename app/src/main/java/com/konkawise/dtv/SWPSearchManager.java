package com.konkawise.dtv;

import android.support.annotation.IntDef;

import com.sw.dvblib.SWPSearch;

import java.util.ArrayList;

import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.PSRNum_t;
import vendor.konka.hardware.dtvmanager.V1_0.PSSParam_t;

public class SWPSearchManager {
    public static final int SIGNAL_STRENGTH = 0;
    public static final int SIGNAL_QUALITY = 1;

    @IntDef(flag = true, value = {
            SIGNAL_STRENGTH, SIGNAL_QUALITY
    })
    private @interface SignalStatus {
    }

    private static class SWPSearchManagerHolder {
        private static final SWPSearchManager INSTANCE = new SWPSearchManager();
    }

    private SWPSearchManager() {
        SWPSearch.CreateInstance();
    }

    public static SWPSearchManager getInstance() {
        return SWPSearchManagerHolder.INSTANCE;
    }

    /**
     * 获取信号
     *
     * @param signalStatus signalStatus=0表示strength，signalStatus=1表示quality
     */
    public int getSignalStatus(@SignalStatus int signalStatus) {
        return SWPSearch.CreateInstance().getSignalStatus(signalStatus);
    }

    /**
     * 根据搜索获取的参数获取频道列表
     */
    public ArrayList<PDPInfo_t> getTsSearchResInfo(int sat, int freq, int symbol, int qam, int plpid) {
        return SWPSearch.CreateInstance().getTsSearchResInfo(sat, freq, symbol, qam, plpid);
    }

    /**
     * 配置搜索所需参数
     */
    public void config(int smode, int cafilter, int nit) {
        SWPSearch.CreateInstance().config(smode, cafilter, nit);
    }

    public void searchByOneTS(int Sat, int Freq, int Symbol, int Qam) {
        SWPSearch.CreateInstance().searchByOneTS(Sat, Freq, Symbol, Qam);
    }

    public void searchByNet(int Sat) {
        SWPSearch.CreateInstance().searchByNET(Sat);
    }

    public void searchByNet(int Sat, ArrayList<PSSParam_t> psList) {
        SWPSearch.CreateInstance().searchByNET(Sat, psList);
    }

    /**
     * 停止搜索
     *
     * @param storeProgram storeProgram=0表示不保存，storeProgram=1表示保存
     */
    public int seatchStop(boolean storeProgram) {
        return SWPSearch.CreateInstance().seatchStop(storeProgram);
    }

    public int seatchStop(Boolean storeProgram, int storeType) {
        return SWPSearch.CreateInstance().seatchStop(storeProgram, storeType);
    }

    public PSRNum_t getProgNumOfThisSarch(int sat, int freq) {
        return SWPSearch.CreateInstance().getProgNumOfThisSarch(sat, freq);
    }

    public void searchByNIT(int sat, int freq, int symbol, int qam) {
        SWPSearch.CreateInstance().searchByNIT(sat, freq, symbol, qam);
    }
}
