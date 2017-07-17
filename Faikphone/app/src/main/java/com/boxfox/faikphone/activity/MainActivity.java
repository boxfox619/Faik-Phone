package com.boxfox.faikphone.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.boxfox.faikphone.R;
import com.boxfox.faikphone.data.PhoneStatus;
import com.boxfox.faikphone.fragment.FakeModeFragment;
import com.boxfox.faikphone.fragment.ModeFragment;
import com.boxfox.faikphone.fragment.RealModeFragment;
import com.boxfox.faikphone.network.EasyAquery;
import com.boxfox.faikphone.service.FakeStatusBarService;
import com.google.firebase.iid.FirebaseInstanceId;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_READ_PHONE_STATE = 21;
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 22;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 23;


    private PhoneStatus phoneStatus;
    private FloatingActionButton modeChangeFabBtn;
    private TextView modeTitleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Realm.init(getApplicationContext());
        Realm realm = Realm.getDefaultInstance();
        phoneStatus = realm.where(PhoneStatus.class).findFirst();
        if (phoneStatus == null) {
            realm.beginTransaction();
            phoneStatus = realm.createObject(PhoneStatus.class);
            phoneStatus.init();
            realm.commitTransaction();
        }
        modeChangeFabBtn = (FloatingActionButton) findViewById(R.id.fab_mode_change);
        modeChangeFabBtn.setOnClickListener(view -> changeMode());
        modeTitleTextView = (TextView) findViewById(R.id.tv_mode_title);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, phoneStatus.mode() ? new RealModeFragment() : new FakeModeFragment())
                .commit();
        modeTitleTextView.setText(phoneStatus.mode() ? "REAL" : "FAKE");

    }

    private Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
        } else {
            Toast.makeText(this, "뒤로가기버튼을 한번 더 누르면 앱이 종료됩니다.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }

    }

    private void onStartBtnClicked(View event) {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(startMain);
        Toast.makeText(getApplicationContext(), "설정을 하려면 앱을 다시 실행하시면 됩니다.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (getFragmentManager().findFragmentById(R.id.container) instanceof ModeFragment) {
            ((ModeFragment) getFragmentManager().findFragmentById(R.id.container)).requestPermissionResult(requestCode, grantResults);
        }
    }

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        }
    }

    private void changeMode() {
        new AlertDialog.Builder(MainActivity.this).setMessage("Mode를 변경하시면 연결된 데이터는 모두 초기화 됩니다. 변경하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 공기계 -> 본 핸드폰, 또는 그 반대 모드로 넘어갈 때 이전의 기기와의 연결을 끊기
                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        phoneStatus.setMode(!phoneStatus.mode());
                        realm.commitTransaction();
                        String token = FirebaseInstanceId.getInstance().getToken();
                        EasyAquery aq = new EasyAquery(MainActivity.this);
                        aq.setUrl(getString(R.string.real_mode_server_url) + "reset")
                                .addParam("token", token)
                                .addParam("type", phoneStatus.mode() ? "all" : "conn")
                                .post();
                        if (getFragmentManager().findFragmentById(R.id.container) instanceof ModeFragment) {
                            ((ModeFragment) getFragmentManager().findFragmentById(R.id.container)).release();
                        }
                        getFragmentManager()
                                .beginTransaction()
                                .setCustomAnimations(
                                        R.animator.card_flip_right_in,
                                        R.animator.card_flip_right_out,
                                        R.animator.card_flip_left_in,
                                        R.animator.card_flip_left_out)
                                .replace(R.id.container, phoneStatus.mode() ? new RealModeFragment(): new FakeModeFragment())
                                .addToBackStack(null)
                                .commit();
                        modeTitleTextView.setText(phoneStatus.mode() ? "REAL" : "FAKE");
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