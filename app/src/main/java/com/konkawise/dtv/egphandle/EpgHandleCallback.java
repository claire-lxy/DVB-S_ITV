package com.konkawise.dtv.egphandle;

/**
 * 职责链Epg节目处理回调接口
 */
public interface EpgHandleCallback {

    /**
     * @param egpHandleResult 回调是否特殊处理Epg和处理类型封装类
     */
    void handleResult(EpgHandleResult egpHandleResult);
}
