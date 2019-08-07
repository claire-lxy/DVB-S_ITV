package com.konkawise.dtv.permission;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PermissionHelper {
    private static final String FRAGMENT_TAG = "permission_fragment_tag";

    private FragmentActivity mFragmentActivity;
    private List<String> mPermissions = new ArrayList<>();

    public PermissionHelper(@NonNull FragmentActivity mFragmentActivity) {
        this.mFragmentActivity = mFragmentActivity;
    }

    public PermissionHelper permission(String permission) {
        addPermissions(Collections.singletonList(permission));
        return this;
    }

    public PermissionHelper permissions(String[] permissions) {
        addPermissions(Arrays.asList(permissions));
        return this;
    }

    public PermissionHelper permissions(List<String> permissions) {
        addPermissions(permissions);
        return this;
    }

    private void addPermissions(List<String> permissions) {
        if (permissions != null && !permissions.isEmpty()) {
            for (String permission : permissions) {
                if (!mPermissions.contains(permission)) {
                    mPermissions.add(permission);
                }
            }
        }
    }

    public PermissionHelper request() {
        Fragment fragment = getPermissionFragment();
        if (fragment == null) {
            fragment = new PermissionFragment();
            mFragmentActivity.getSupportFragmentManager().beginTransaction().add(fragment, FRAGMENT_TAG).commitNow();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> deniedPermission = new ArrayList<>();
            for (String permission : mPermissions) {
                if (!((PermissionFragment) fragment).isGranted(permission)) {
                    deniedPermission.add(permission);
                }
            }

            if (!deniedPermission.isEmpty()) {
                ((PermissionFragment) fragment).requestPermissions(deniedPermission.toArray(new String[deniedPermission.size()]));
            }
        }
        return this;
    }

    public void result(OnRequestPermissionResultListener listener) {
        Fragment fragment = getPermissionFragment();
        if (fragment == null) {
            request();
            fragment = getPermissionFragment();
        }

        ((PermissionFragment) fragment).setPermissionResultListener(listener);
        mPermissions.clear();
    }

    private Fragment getPermissionFragment() {
        FragmentManager fm = mFragmentActivity.getSupportFragmentManager();
        return fm.findFragmentByTag(FRAGMENT_TAG);
    }
}
