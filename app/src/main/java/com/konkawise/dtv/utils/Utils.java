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
        if (satInfo.unicConfig.UnicEnable != 0) {
            return getUnicable(context, satInfo);
        } else {
            if (satInfo.diseqc10_pos == Constants.SatInfoValue.OFF_OR_TONEBURST) {
                return getOFFOrToneBurst(context, satInfo);
            } else if (isDISEQC10(satInfo.diseqc10_pos)) {
                return getDiSEqC10(context, satInfo);
            } else if (isDiSEqc11(satInfo.diseqc10_pos)) {
               return getDiSEqC11(context, satInfo);
            }
        }
        return context.getString(R.string.off);
    }

    public static String getUnicable(Context context, HProg_Struct_SatInfo satInfo) {
        // SatPosition=0, SCRType=0, 1Sat4SCR
        // SatPosition=0, SCRType=1, 1Sat8SCR
        // SatPosition>=1, SCRType=0, 2Sat4SCR
        // SatPosition>=1, SCRType=1, 2Sat8SCR
        String[] unicableArray = context.getResources().getStringArray(R.array.unicable);
        if (satInfo.unicConfig.SatPosition == Constants.SatInfoValue.SINGLE_SAT_POSITION && satInfo.unicConfig.SCRType == Constants.SatInfoValue.SCR_4) {
            return unicableArray[0];
        } else if (satInfo.unicConfig.SatPosition == Constants.SatInfoValue.SINGLE_SAT_POSITION && satInfo.unicConfig.SCRType == Constants.SatInfoValue.SCR_8) {
            return unicableArray[1];
        } else if (satInfo.unicConfig.SatPosition >= Constants.SatInfoValue.MULTI_SAT_POSITION_A && satInfo.unicConfig.SCRType == Constants.SatInfoValue.SCR_4) {
            return unicableArray[2];
        } else if (satInfo.unicConfig.SatPosition >= Constants.SatInfoValue.MULTI_SAT_POSITION_A && satInfo.unicConfig.SCRType == Constants.SatInfoValue.SCR_8) {
            return unicableArray[3];
        } else {
            return unicableArray[4];
        }
    }

    public static String getOFFOrToneBurst(Context context, HProg_Struct_SatInfo satInfo) {
        // diseqc10_pos=0, OFF or ToneBurst, diseqc10_tone value is valid
        // diseqc10_tone=0, OFF
        // diseqc10_tone=1, ToneBurst DISEQC_A
        // diseqc10_tone=2, ToneBurst DISEQC_B
        String[] toneBurstArray = context.getResources().getStringArray(R.array.tone_burst);
        if (satInfo.diseqc10_tone == Constants.SatInfoValue.OFF_OR_TONEBURST) {
            return context.getString(R.string.off);
        } else {
            return toneBurstArray[satInfo.diseqc10_tone - 1];
        }
    }

    public static String getDiSEqC10(Context context, HProg_Struct_SatInfo satInfo) {
        String[] diSEqC10Array = context.getResources().getStringArray(R.array.DiSEqc10);
        // diseqc10_pos=1~4, DiSEqC DISEQC_A~D
        return diSEqC10Array[satInfo.diseqc10_pos - 1];
    }

    public static String getDiSEqC11(Context context, HProg_Struct_SatInfo satInfo) {
        String[] diSEqC11Array = context.getResources().getStringArray(R.array.DiSEqC11);
        // diseqc10_pos=5~16, LNB 1~16
        return diSEqC11Array[satInfo.diseqc10_pos - 5];
    }

    public static boolean isDISEQC10(int diseqcPos) {
        return diseqcPos >= Constants.SatInfoValue.DISEQC_MIN_RANGE && diseqcPos <= Constants.SatInfoValue.DISEQC_MAX_RANGE;
    }

    public static boolean isDiSEqc11(int diseqcPos) {
        return diseqcPos >= Constants.SatInfoValue.LNB_MIN_RANGE && diseqcPos <= Constants.SatInfoValue.LNB_MAX_RANGE;
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
