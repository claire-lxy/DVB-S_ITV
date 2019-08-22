package com.konkawise.dtv.bean;

public class HandlerMsgModel {
    public int what;
    public int arg1 = -1;
    public int arg2 = -1;
    public Object obj;
    public long delay;

    public HandlerMsgModel(int what) {
        this.what = what;
    }

    public HandlerMsgModel(int what, long delay) {
        this.what = what;
        this.delay = delay;
    }

    public HandlerMsgModel(int what, int arg1) {
        this.what = what;
        this.arg1 = arg1;
    }

    public HandlerMsgModel(int what, Object obj) {
        this.what = what;
        this.obj = obj;
    }

    public HandlerMsgModel(int what, int arg1, long delay) {
        this.what = what;
        this.arg1 = arg1;
        this.delay = delay;
    }
}

