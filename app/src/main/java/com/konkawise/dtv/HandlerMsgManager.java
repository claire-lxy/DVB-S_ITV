package com.konkawise.dtv;

import android.os.Handler;
import android.os.Message;

import com.konkawise.dtv.bean.HandlerMsgModel;

public class HandlerMsgManager {

    private static class HandlerMsgManagerHolder {
        private static final HandlerMsgManager INSTANCE = new HandlerMsgManager();
    }

    public static HandlerMsgManager getInstance() {
        return HandlerMsgManagerHolder.INSTANCE;
    }

    public void sendMessage(Handler handler, HandlerMsgModel model) {
        Message msg = Message.obtain();
        msg.what = model.what;
        if (model.arg1 != -1) msg.arg1 = model.arg1;
        if (model.arg2 != -1) msg.arg2 = model.arg2;
        if (model.obj != null) msg.obj = model.obj;
        handler.sendMessageDelayed(msg, model.delay);
    }

    public void removeMessage(Handler handler, int what) {
        if (handler.hasMessages(what)) handler.removeMessages(what);
    }
}
