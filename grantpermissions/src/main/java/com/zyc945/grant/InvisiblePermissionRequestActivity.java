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

        ActivityCompat.requestPermissions(this,
                PermissionsManager.getPendingAuthorizedPermissions(), REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionsManager.notifyPermissionsChange(permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
