package com.konkawise.dtv.utils;

import android.content.Context;
import android.widget.TextView;

import com.konkawise.dtv.R;

import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

/**
 * 创建者      lj DELL
 * 创建时间    2018/12/24 10:24
 * 描述        ${TODO}
 * <p>
 * 更新者      $Author$
 * <p>
 * 更新时间    $Date$
 * 更新描述    ${TODO}
 */

public class Utils {
    private static int[] lnbFreq0 = {5150, 5750, 9750, 10600, 11300};
    private static int[] lnbFreq1 = {5150, 5750, 9700, 10750, 9750, 10600};

    private static char[] direct = {'E', 'W', 'N', 'S'};

    /**
     * 判断是h 还是v
     */
    public static String getVorH(Context context, int qam) {
        return context.getResources().getString(qam == 0 ? R.string.h : R.string.v);
    }

    /**
     * 设置LNB 为off 或者ON
     */
    public static String getOnorOff(Context context, int lnbpower) {
        return context.getResources().getString(lnbpower == 0 ? R.string.off : R.string.on);
    }

    public static void satLNB(SatInfo_t satInfoT, int index, int lnbData) {
        int i = 0;
        if (index >= 6) {
            i = (index - 6) * 2;
            if (i == 0) {
                satInfoT.LnbType = 2;
            } else {

                satInfoT.LnbType = 0;
            }

            satInfoT.lnb_low = lnbFreq1[i];
            satInfoT.lnb_high = lnbFreq1[i + 1];
        } else if (index > 0) {
            satInfoT.LnbType = 1;
            satInfoT.lnb_low = lnbFreq0[index - 1];
            satInfoT.lnb_high = 0;
        } else {
            satInfoT.LnbType = 1;
            satInfoT.lnb_low = lnbData;
            satInfoT.lnb_high = 0;
        }
    }

    public static String getLNB(SatInfo_t satInfo_t) {
        String lnb;

        if (satInfo_t.LnbType == 0) {
            if (satInfo_t.lnb_low == 9750) {
                lnb = "Uni" + satInfo_t.lnb_low + "/" + satInfo_t.lnb_high;
            } else {
                lnb = satInfo_t.lnb_low + "/" + satInfo_t.lnb_high;
            }

        } else if (satInfo_t.LnbType == 1) {
            lnb = satInfo_t.lnb_low + "";
        } else {
            lnb = satInfo_t.lnb_low + "/" + satInfo_t.lnb_high;
        }
        return lnb;
    }


    public static void setDiesc(SatInfo_t satInfo_t, TextView textView, String[] stringArray) {
        // String[] stringArray = getResources().getStringArray(R.array.DISEQC);
        if ((satInfo_t.diseqc10_pos >= 1) && (satInfo_t.diseqc10_pos < 5)) {
            textView.setText(stringArray[satInfo_t.diseqc10_pos + 2]);
        } else if (satInfo_t.diseqc10_tone == 1) {

            textView.setText(stringArray[1]);
        } else if (satInfo_t.diseqc10_tone == 2) {
            textView.setText(stringArray[2]);
        } else if (satInfo_t.diseqc10_pos == 21) {
            textView.setText(stringArray[stringArray.length - 1]);
        } else {
            if (satInfo_t.skewonoff < stringArray.length - 1) {
                textView.setText(stringArray[satInfo_t.skewonoff]);
            }

        }
    }

    public static void setDescNum(SatInfo_t satInfoT, int currentIndex, String[] DescArrsy) {
        satInfoT.diseqc10_pos = 0;
        satInfoT.diseqc10_tone = 0;
        satInfoT.skewonoff = 0;
        satInfoT.diseqc12_pos = 0;
        satInfoT.diseqc12 = 0;
        //diseqc
        if (currentIndex >= 3 && currentIndex < 23) {
            satInfoT.diseqc10_pos = currentIndex - 2;
        } else if (currentIndex == 1) {
            satInfoT.diseqc10_tone = 1;
        } else if (currentIndex == 2) {
            satInfoT.diseqc10_tone = 2;
        } else if (currentIndex >= 23 && currentIndex < DescArrsy.length) {
            satInfoT.diseqc12_pos = currentIndex - 22;
            satInfoT.diseqc12 = 1;
        } else {
            satInfoT.skewonoff = currentIndex;
        }
    }

    public static String getDiseqc(SatInfo_t satInfoT, String[] DescArrsy) {
        int index = 0;
        if ((satInfoT.diseqc10_pos != 0)) {
            index = satInfoT.diseqc10_pos + 2;
        } else if (satInfoT.diseqc10_tone != 0) {
            index = satInfoT.diseqc10_tone;
        } else if (satInfoT.diseqc12 != 0) {
            index = satInfoT.diseqc12_pos + 22;
        } else {
            index = satInfoT.skewonoff;
        }
        return DescArrsy[index];
    }

    public static String getLongitude(SatInfo_t satInfoT) {
        String longitudeStr = "";
        int longitude = satInfoT.diseqc12_longitude;
        int longDirect = 0;
        int input0, input1;
        if (longitude > 1800)
            longitude = longitude - 3600;
        if (longitude > 0)
            longDirect = 0;
        else if (longitude == 0)
            longDirect = 1;
        else {
            longDirect = 1;
            longitude = -longitude;
        }
        input0 = longitude / 10;
        input1 = longitude % 10;
        longitudeStr = direct[longDirect] + "" + input0 + input1;
        return longitudeStr;
    }
}
