package com.konkawise.dtv.permission;

import java.util.List;

public interface OnRequestPermissionResultListener {
    void onRequestResult(List<String> grantedPermissions, List<String> deniedPermissions);
}
