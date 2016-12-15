package com.zyc945.grant.demo;

import android.Manifest.permission;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.zyc945.grant.PermissionsManager;
import com.zyc945.grant.PermissionsResultAction;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.getCameraPermission).setOnClickListener(this);
        findViewById(R.id.getReadContacts).setOnClickListener(this);
        findViewById(R.id.getReadPhoneState).setOnClickListener(this);
        findViewById(R.id.getUserFingerprint).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        String[] proceedPermissions = null;
        switch (view.getId()) {
            case R.id.getCameraPermission:
                proceedPermissions = new String[]{permission.CAMERA};
                break;
            case R.id.getReadContacts:
                proceedPermissions = new String[]{permission.READ_CONTACTS};
                break;
            case R.id.getReadPhoneState:
                proceedPermissions = new String[]{permission.READ_PHONE_STATE};
                break;
            case R.id.getUserFingerprint:
                proceedPermissions = new String[]{permission.USE_FINGERPRINT};
                break;
            default:
                break;
        }

        if (null != proceedPermissions) {
            PermissionsManager.requestPermissions(this, proceedPermissions,
                    new CommonPermissionsResultAction().proceedPermission(proceedPermissions));
        }

    }

    class CommonPermissionsResultAction extends PermissionsResultAction {
        private String[] proceedPermissions;

        public PermissionsResultAction proceedPermission(String[] permissions) {
            proceedPermissions = permissions;
            return this;
        }

        @Override
        public void onGranted() {
            showToast(Arrays.toString(proceedPermissions) + " granted");
        }

        @Override
        public void onDenied(String permission) {
            showToast(permission + " denied");
        }
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
