package com.konkawise.dtv.bean;

import vendor.konka.hardware.dtvmanager.V1_0.EpgEvent_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class EpgBookParameterModel {
    public int type;
    public int schtype;
    public int schway;
    public SysTime_t startTimeInfo;
    public SysTime_t endTimeInfo;
    public PDPMInfo_t progInfo;
    public EpgEvent_t eventInfo;
}
