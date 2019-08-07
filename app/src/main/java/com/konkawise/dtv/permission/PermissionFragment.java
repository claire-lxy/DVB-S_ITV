package com.konkawise.dtv.permission;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;
import java.util.List;

public class PermissionFragment extends Fragment {
    private static final int PERMISSION_REQUEST_CODE = 1;

    private OnRequestPermissionResultListener mPermissionResultListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    void requestPermissions(@NonNull String[] permissions) {
        requestPermissions(permissions, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> grantPermissions = new ArrayList<>();
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantPermissions.add(permissions[i]);
            } else {
                deniedPermissions.add(permissions[i]);
            }
        }

        if (mPermissionResultListener != null) {
            mPermissionResultListener.onRequestResult(grantPermissions, deniedPermissions);
        }
    }

    void setPermissionResultListener(OnRequestPermissionResultListener listener) {
        this.mPermissionResultListener = listener;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    boolean isGranted(String permission) {
        final FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity == null) {
            throw new IllegalStateException("This fragment must be attached to an activity.");
        }
        return fragmentActivity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }
}
