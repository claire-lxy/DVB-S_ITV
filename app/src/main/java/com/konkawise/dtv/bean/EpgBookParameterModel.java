package com.konkawise.dtv.bean;

import com.sw.dvblib.DTVCommon;

import vendor.konka.hardware.dtvmanager.V1_0.HEPG_Struct_Event;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;

public class EpgBookParameterModel {
    public int type;
    public int schtype;
    public int schway;
    public DTVCommon.TimeModel startTimeInfo;
    public DTVCommon.TimeModel endTimeInfo;
    public HProg_Struct_ProgInfo progInfo;
    public HEPG_Struct_Event eventInfo;
}
