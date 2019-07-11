package com.konkawise.dtv;

import com.konkawise.dtv.weaktool.WeakTool;
import com.konkawise.dtv.weaktool.WeakToolInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeakToolManager {
    private Map<WeakToolInterface, List<WeakTool>> mWeakToolMap = new HashMap<>();

    private static class WeakToolManagerHolder {
        private static final WeakToolManager INSTANCE = new WeakToolManager();
    }

    public static WeakToolManager getInstance() {
        return WeakToolManagerHolder.INSTANCE;
    }

    public synchronized void addWeakTool(WeakToolInterface weakToolInterface, WeakTool weakTool) {
        List<WeakTool> weakTools = mWeakToolMap.get(weakToolInterface);
        if (weakTools == null) {
            weakTools = new ArrayList<>();
        }
        weakTools.add(weakTool);
        mWeakToolMap.put(weakToolInterface, weakTools);
    }

    public synchronized void removeWeakTool(WeakToolInterface weakToolInterface) {
        List<WeakTool> weakTools = mWeakToolMap.get(weakToolInterface);
        if (weakTools == null) return;
        for (WeakTool weakTool : weakTools) {
            weakTool.release();
        }
        mWeakToolMap.remove(weakToolInterface);
    }
}
