package com.konkawise.dtv.utils;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.konkawise.dtv.KonkaApplication;

import java.lang.reflect.Field;

/**
 * fixed has already been added to the window manager crash toast
 */
public class ToastUtils {
    private static Field sFieldTN;
    private static Field sFieldTNHandler;

    static {
        try {
            sFieldTN = Toast.class.getDeclaredField("mTN");
            sFieldTN.setAccessible(true);
            sFieldTNHandler = sFieldTN.getType().getDeclaredField("mHandler");
            sFieldTNHandler.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void hook(Toast toast) {
        try {
            Object tn = sFieldTN.get(toast);
            Handler preHandler = (Handler) sFieldTNHandler.get(tn);
            sFieldTNHandler.set(tn, new SafelyHandlerWrapper(preHandler));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class SafelyHandlerWrapper extends Handler {
        private Handler impl;

        SafelyHandlerWrapper(Handler impl) {
            this.impl = impl;
        }

        @Override
        public void dispatchMessage(Message msg) {
            try {
                super.dispatchMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void handleMessage(Message msg) {
            impl.handleMessage(msg);
        }
    }

    public static void showToast(@StringRes int id) {
        showToast(KonkaApplication.getContext().getResources().getString(id), Toast.LENGTH_SHORT);
    }

    public static void showToast(@StringRes int id, int duration) {
        showToast(KonkaApplication.getContext().getResources().getString(id), duration);
    }

    public static void showToast(@NonNull String msg) {
        showToast(msg, Toast.LENGTH_SHORT);
    }

    public static void showToast(@NonNull String msg, int duration) {
        Toast toast = Toast.makeText(KonkaApplication.getContext(), msg, duration);
        // 在调用Toast.show()之前处理:
        hook(toast);
        toast.show();
    }
}
