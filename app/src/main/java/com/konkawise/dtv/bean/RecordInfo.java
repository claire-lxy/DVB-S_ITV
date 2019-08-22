package com.konkawise.dtv.bean;

import java.io.File;
import java.io.Serializable;

import vendor.konka.hardware.dtvmanager.V1_0.HPVR_RecFile_t;

public class RecordInfo implements Serializable{
    private File recordFile;
    private HPVR_RecFile_t mHpvrRecFileT;

    public RecordInfo() {

    }

    public RecordInfo(File recordFile, HPVR_RecFile_t hpvrRecFileT) {
        this.recordFile = recordFile;
        mHpvrRecFileT = hpvrRecFileT;
    }

    public File getFile() {
        return recordFile;
    }

    public HPVR_RecFile_t getHpvrRecFileT() {
        return mHpvrRecFileT;
    }

    public void setRecordFile(File recordFile) {
        this.recordFile = recordFile;
    }

    public void setHpvrRecFileT(HPVR_RecFile_t hpvrRecFileT) {
        mHpvrRecFileT = hpvrRecFileT;
    }
}
