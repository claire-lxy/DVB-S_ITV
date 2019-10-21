package com.konkawise.dtv.utils;

import android.content.Context;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;

import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;

/**
 * DiSEqC相关字段文档说明
 * https://kcms.konkawise.com/blog/a/5da90d8ac05c7d3284ba0d8d
 */
public class Utils {
    private static int[] lnbFreq0 = {5150, 5750, 9750, 10600, 11300};
    private static int[] lnbFreq1 = {5150, 5750, 9700, 10750, 9750, 10600};

    public static String getVorH(Context context, int qam) {
        return context.getResources().getString(qam == 0 ? R.string.h : R.string.v);
    }

    public static int getLnbType(int currLnbIndex) {
        if (currLnbIndex >= 6) {
            if (((currLnbIndex - 6) * 2) == 0) {
                return 2;
            } else {
                return 0;
            }
        } else {
            return 1;
        }
    }

    public static int getLnbLow(int currLnbIndex, int currLnb) {
        if (currLnbIndex >= 6) {
            currLnbIndex = (currLnbIndex - 6) * 2;
            return lnbFreq1[currLnbIndex];
        } else if (currLnbIndex > 0) {
            return lnbFreq0[currLnbIndex - 1];
        } else {
            return currLnb;
        }
    }

    public static int getLnbHeight(int currLnbIndex) {
        if (currLnbIndex >= 6) {
            currLnbIndex = (currLnbIndex - 6) * 2;
            return lnbFreq1[currLnbIndex + 1];
        } else {
            return 0;
        }
    }

    public static String getLnb(HProg_Struct_SatInfo satInfo) {
        if (satInfo.LnbType == 0) {
            if (satInfo.lnb_low == 9750) {
                return "Uni" + satInfo.lnb_low + "/" + satInfo.lnb_high;
            } else {
                return satInfo.lnb_low + "/" + satInfo.lnb_high;
            }

        } else if (satInfo.LnbType == 1) {
            return satInfo.lnb_low + "";
        } else {
            return satInfo.lnb_low + "/" + satInfo.lnb_high;
        }
    }

    public static String getDiSEqC(Context context, HProg_Struct_SatInfo satInfo) {
        String[] diSEqCArray = context.getResources().getStringArray(R.array.DISEQC);
        if (satInfo.diseqc10_pos == Constants.DiSEqCPosValueRange.OFF_OR_TONEBURST) {
            // diseqc10_pos=0, OFF or ToneBurst, diseqc10_tone value is valid
            // diseqc10_tone=0, OFF
            // diseqc10_tone=1, ToneBurst DISEQC_A
            // diseqc10_tone=2, ToneBurst DISEQC_B
            return diSEqCArray[satInfo.diseqc10_tone];
        } else if (isDISEQC10(satInfo.diseqc10_pos)) {
            // diseqc10_pos=1~4, DiSEqC DISEQC_A~D
            return diSEqCArray[satInfo.diseqc10_pos + 2];
        } else if (isDiSEqc11(satInfo.diseqc10_pos)) {
            // diseqc10_pos=5~16, LNB 1~16
            return diSEqCArray[satInfo.diseqc10_pos + 2];
        }
        return diSEqCArray[0];
    }

    public static boolean isDISEQC10(int diseqcPos) {
        return diseqcPos >= Constants.DiSEqCPosValueRange.DISEQC_MIN_RANGE && diseqcPos <= Constants.DiSEqCPosValueRange.DISEQC_MAX_RANGE;
    }

    public static boolean isDiSEqc11(int diseqcPos) {
        return diseqcPos >= Constants.DiSEqCPosValueRange.LNB_MIN_RANGE && diseqcPos <= Constants.DiSEqCPosValueRange.LNB_MAX_RANGE;
    }

    public static String getMotorType(Context context, HProg_Struct_SatInfo satInfo) {
        if (satInfo == null) return "";

        if (satInfo.diseqc12 == Constants.MotorType.OFF) {
            return context.getString(R.string.motor_type_off);
        } else if (satInfo.diseqc12 == Constants.MotorType.DISEQC) {
            return context.getString(R.string.motor_type_diseqc);
        } else if (satInfo.diseqc12 == Constants.MotorType.USALS) {
            return context.getString(R.string.motor_type_usals);
        }
        return "";
    }
}
