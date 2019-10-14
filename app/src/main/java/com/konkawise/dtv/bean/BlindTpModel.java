package com.konkawise.dtv.bean;

import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Struct_TP;

public class BlindTpModel {
    public static final int VIEW_TYPE_TP = 1;
    public static final int VIEW_TYPE_PRO = 2;

    public HSearch_Struct_TP pssParam_t;
    public HProg_Struct_ProgBasicInfo pdpInfo_t;
    public int type = VIEW_TYPE_TP;
}
