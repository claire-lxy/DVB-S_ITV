package com.konkawise.dtv.utils;

import android.content.Context;

import com.konkawise.dtv.R;

import java.text.DecimalFormat;

import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

public class Utils {
    private static int[] lnbFreq0 = {5150, 5750, 9750, 10600, 11300};
    private static int[] lnbFreq1 = {5150, 5750, 9700, 10750, 9750, 10600};

    private static final int MAX_LONGITUDE = 1800;
    private static final int LONGITUDE_REVERSE_VALUE = 3600;
    private static final int MAX_LATITUDE = 900;
    private static final int LATITUDE_REVERSE_VALUE = 1800;

    // 取小数点后一位
    private static final DecimalFormat sLatLngFormat = new DecimalFormat("##0.0");

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

    public static String getLnb(SatInfo_t satInfo) {
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

    public static int getDiSEqC10Pos(int currDiSEqcIndex) {
        if (currDiSEqcIndex >= 3 && currDiSEqcIndex < 23) {
            return currDiSEqcIndex - 2;
        }
        return 0;
    }

    public static int getDiSEqC10Tone(int currDiSEqCIndex) {
        if (currDiSEqCIndex == 1) return 1;
        if (currDiSEqCIndex == 2) return 2;
        return 0;
    }

    public static int getDiSEqC12Pos(int currDiSEqCIndex) {
        if (currDiSEqCIndex == 23) {
            return currDiSEqCIndex - 22;
        }
        return 0;
    }

    public static int getDiSEqC12(int currDiSEqCIndex) {
        if (currDiSEqCIndex == 23) {
            return 1;
        }
        return 0;
    }

    public static int getSkewOnOff(int currDiSEqCIndex) {
        if (currDiSEqCIndex > 23) return currDiSEqCIndex;
        return 0;
    }

    public static String getDiSEqC(SatInfo_t satInfo, String[] DiSEqcArray) {
        if (satInfo.diseqc10_pos != 0) {
            return DiSEqcArray[satInfo.diseqc10_pos + 2];
        } else if (satInfo.diseqc10_tone != 0) {
            return DiSEqcArray[satInfo.diseqc10_tone];
        } else if (satInfo.diseqc12 != 0) {
            return DiSEqcArray[satInfo.diseqc12_pos + 22];
        } else {
            return DiSEqcArray[satInfo.skewonoff];
        }
    }

    public static String getMotorType(Context context, SatInfo_t satInfo) {
        if (satInfo == null) return "";

        if (satInfo.diseqc12 == 0) {
            return context.getString(R.string.motor_type_off);
        } else if (satInfo.diseqc12 == 1) {
            return context.getString(R.string.motor_type_diseqc);
        } else if (satInfo.diseqc12 == 2) {
            return context.getString(R.string.motor_type_usals);
        }
        return "";
    }
}
