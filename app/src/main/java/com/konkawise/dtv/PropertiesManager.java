package com.konkawise.dtv;

import android.annotation.SuppressLint;

import java.lang.reflect.Method;

public class PropertiesManager {

    private static class PropertiesManagerHolder {
        private static PropertiesManager INSTANCE = new PropertiesManager();
    }

    public static PropertiesManager getInstance() {
        return PropertiesManagerHolder.INSTANCE;
    }

    public String getProperty(String key) {
        return getProperty(key, "");
    }

    public String getProperty(String key, String defValue) {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            return (String) (get.invoke(c, key, defValue));
        } catch (Exception e) {
            e.printStackTrace();
            return defValue;
        }
    }

    public void setProperty(String key, String value) {
        try {
            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
