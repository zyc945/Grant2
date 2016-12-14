package com.zyc945.grant.demo;

import android.Manifest.permission;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.zyc945.grant.PermissionsManager;
import com.zyc945.grant.PermissionsResultAction;

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
        switch (view.getId()) {
            case R.id.getCameraPermission:
                PermissionsManager.requestPermissions(this, new String[]{permission.CAMERA},
                        new PermissionsResultAction() {
                            @Override
                            public void onGranted() {
                                showToast(permission.CAMERA + " granted");
                            }

                            @Override
                            public void onDenied(String permission) {
                                showToast(permission + " denied");
                            }
                        });
                break;
            case R.id.getReadContacts:
                PermissionsManager.requestPermissions(this, new String[]{permission.READ_CONTACTS},
                        new PermissionsResultAction() {
                            @Override
                            public void onGranted() {
                                showToast(permission.READ_CONTACTS + " granted");
                            }

                            @Override
                            public void onDenied(String permission) {
                                showToast(permission + " denied");
                            }
                        });
                break;
            case R.id.getReadPhoneState:
                PermissionsManager.requestPermissions(this, new String[]{permission.READ_PHONE_STATE},
                        new PermissionsResultAction() {
                            @Override
                            public void onGranted() {
                                showToast(permission.READ_PHONE_STATE + " granted");
                            }

                            @Override
                            public void onDenied(String permission) {
                                showToast(permission + " denied");
                            }
                        });
                break;
            case R.id.getUserFingerprint:
                PermissionsManager.requestPermissions(this, new String[]{permission.USE_FINGERPRINT},
                        new PermissionsResultAction() {
                            @Override
                            public void onGranted() {
                                showToast(permission.USE_FINGERPRINT + " granted");
                            }

                            @Override
                            public void onDenied(String permission) {
                                showToast(permission + " denied");
                            }
                        });
                break;
            default:
                break;
        }
    }

    private void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
