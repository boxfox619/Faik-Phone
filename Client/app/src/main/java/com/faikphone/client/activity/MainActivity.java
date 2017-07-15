package com.faikphone.client.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.faikphone.client.R;
import com.faikphone.client.application.FaikPhoneApplication;
import com.faikphone.client.fragment.FakeModeFragment;
import com.faikphone.client.fragment.RealModeFragment;
import com.faikphone.client.network.EasyAquery;
import com.faikphone.client.service.FakeStatusBarService;
import com.faikphone.client.utils.AppPreferences;
import com.google.firebase.iid.FirebaseInstanceId;

import at.markushi.ui.CircleButton;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_MANAGE_OVERLAY_PERMISSION = 11;
    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 21;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 22;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 23;

    private AppPreferences appPreferences;
    private CircleButton changeModeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appPreferences = FaikPhoneApplication.getAppPreferences();

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, appPreferences.getPhoneMode() ? new RealModeFragment() : new FakeModeFragment())
                    .commit();
        }

        /*startBtn = (Button) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(event -> onStartBtnClicked(event));*/

        if (checkDrawOverlayPermission()) {
            startService(new Intent(this, FakeStatusBarService.class));
        }
        checkPermission(Manifest.permission.READ_PHONE_STATE, PERMISSIONS_REQUEST_READ_PHONE_STATE);
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (appPreferences.getPhoneMode()) {  // fakePhone
            getSupportActionBar().setTitle("FaikPhone(Fake Mode)");
        } else {  // realPhone
            getSupportActionBar().setTitle("FaikPhone(Real Mode)");
            if (appPreferences.getKeyDevicePhoneNumber() == null) {
                TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);
                appPreferences.setKeyDevicePhoneNumber(telephonyManager.getLine1Number());
            }
        }
    }

    private void onChangeModeBtnClicked(View view) {

    }

    private void onStartBtnClicked(View event) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(startMain);
        Toast.makeText(getApplicationContext(), "설정을 하려면 앱을 다시 실행하시면 됩니다.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MANAGE_OVERLAY_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        startService(new Intent(this, FakeStatusBarService.class));
                        finish();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "Fake Call 기능을 사용하기 위해서는 전화 권한이 필요합니다.\n" +
                            "설정에서 권한을 허용해주세요.", Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
            default:
                break;
        }
    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), REQUEST_MANAGE_OVERLAY_PERMISSION);
                return false;
            }
        }

        return true;
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void onClickSettings(MenuItem menuItem) {
        flipCard();
    }


    private void flipCard() {
        new AlertDialog.Builder(MainActivity.this).setMessage("Mode를 변경하시면 연결된 데이터는 모두 초기화 됩니다. 변경하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 공기계 -> 본 핸드폰, 또는 그 반대 모드로 넘어갈 때 이전의 기기와의 연결을 끊기
                        appPreferences.setPhoneMode(!appPreferences.getPhoneMode());
                        String token = FirebaseInstanceId.getInstance().getToken();
                        EasyAquery aq = new EasyAquery(MainActivity.this);
                        aq.setUrl(getString(R.string.real_mode_server_url) + "reset")
                                .addParam("token", token)
                                .addParam("type", appPreferences.getPhoneMode() ? "all" : "conn")
                                .post();
                        getFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(
                                        R.animator.card_flip_right_in,
                                        R.animator.card_flip_right_out,
                                        R.animator.card_flip_left_in,
                                        R.animator.card_flip_left_out)
                                .replace(R.id.container, appPreferences.getPhoneMode() ? new RealModeFragment() : new FakeModeFragment())
                                .addToBackStack(null)
                                .commit();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}