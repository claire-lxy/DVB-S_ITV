package com.konkawise.dtv.bean;

import java.io.File;
import java.io.Serializable;

import vendor.konka.hardware.dtvmanager.V1_0.HPVR_Struct_RecFile;

public class RecordInfo implements Serializable{
    private File recordFile;
    private HPVR_Struct_RecFile mHpvrRecFileT;

    public RecordInfo() {

    }

    public RecordInfo(File recordFile, HPVR_Struct_RecFile hpvrRecFileT) {
        this.recordFile = recordFile;
        mHpvrRecFileT = hpvrRecFileT;
    }

    public File getFile() {
        return recordFile;
    }

    public HPVR_Struct_RecFile getHpvrRecFileT() {
        return mHpvrRecFileT;
    }

    public void setRecordFile(File recordFile) {
        this.recordFile = recordFile;
    }

    public void setHpvrRecFileT(HPVR_Struct_RecFile hpvrRecFileT) {
        mHpvrRecFileT = hpvrRecFileT;
    }
}
