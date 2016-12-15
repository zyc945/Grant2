package com.zyc945.grant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * Author: zyc945
 * Date  : 2016-12-14.
 */

public class InvisiblePermissionRequestActivity extends Activity {
    public static final int REQUEST_PERMISSION = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] permissions = PermissionsManager.getPendingAuthorizedPermissions();
        if (permissions.length > 0) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
        } else {
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (REQUEST_PERMISSION == requestCode) {
            PermissionsManager.notifyPermissionsChange(permissions, grantResults);
        }
        finish();
    }
}
