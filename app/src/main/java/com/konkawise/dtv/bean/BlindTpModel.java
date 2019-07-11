package com.konkawise.dtv.bean;

import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.PSSParam_t;

public class BlindTpModel {
    public static final int VIEW_TYPE_TP = 1;
    public static final int VIEW_TYPE_PRO = 2;

    public PSSParam_t pssParam_t;
    public PDPInfo_t pdpInfo_t;
    public int type = VIEW_TYPE_TP;
}
