package com.konkawise.dtv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.konkawise.dtv.event.BookRegisterListenerEvent;
import com.konkawise.dtv.rx.RxBus;
import com.konkawise.dtv.ui.Topmost;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class KonkaApplication extends Application {
    private static KonkaApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        closeDetectedProblemApiDialog();
        registerActivityLifecycleCallbacks(new ActivityLifecycleListener());
    }

    private static class ActivityLifecycleListener implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            // launcher的播放画面进入就是Topmost，可以直接判断处理
            if (activity instanceof Topmost) {
                RxBus.getInstance().post(new BookRegisterListenerEvent(false));
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            if (activity instanceof Topmost) {
                RxBus.getInstance().post(new BookRegisterListenerEvent(true));
            }
        }
    }

    /**
     * Android 9开始限制开发者调用非官方API方法和接口(即用反射直接调用源码)
     * 弹框提示 Detected problems with API compatibility(visit g.co/dev/appcompat for more info)
     *
     * 隐藏警告弹框
     */
    private void closeDetectedProblemApiDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return;

        try {
            @SuppressLint("PrivateApi") Class clsPkgParser = Class.forName("android.content.pm.PackageParser$Package");
            Constructor constructor = clsPkgParser.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);

            @SuppressLint("PrivateApi") Class clsActivityThread = Class.forName("android.app.ActivityThread");
            Method method = clsActivityThread.getDeclaredMethod("currentActivityThread");
            method.setAccessible(true);
            Object activityThread = method.invoke(null);
            Field hiddenApiWarning = clsActivityThread.getDeclaredField("mHiddenApiWarningShown");
            hiddenApiWarning.setAccessible(true);
            hiddenApiWarning.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Context getContext() {
        return sApplication;
    }
}
