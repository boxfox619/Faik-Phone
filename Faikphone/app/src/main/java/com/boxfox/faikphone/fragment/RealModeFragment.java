package com.boxfox.faikphone.fragment;

import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.boxfox.faikphone.R;
import com.boxfox.faikphone.data.PhoneStatus;
import com.boxfox.faikphone.listener.PhoneStateRead;
import com.boxfox.faikphone.network.EasyAquery;
import com.boxfox.faikphone.receiver.CallReceiver;
import com.boxfox.faikphone.receiver.SMSReceiver;
import com.boxfox.faikphone.service.PhoneStatusMonitoringService;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.Switch;
import com.gc.materialdesign.widgets.SnackBar;

import io.realm.Realm;

/**
 * Created by boxfox on 2017-07-16.
 */

public class RealModeFragment extends ModeFragment {
    private static final int SMS_PERMISSION_REQUEST_CODE = 332;
    private static final int CALL_PERMISSION_REQUEST_CODE = 333;

    private PhoneStatus phoneStatus;

    private TextView codeTv;
    private ImageButton refreshBtn;
    private ButtonFlat disconnectBtn;

    private Switch smsControlSwitch;
    private Switch callControlSwitch;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.real_mode_layout, container, false);
        phoneStatus = Realm.getDefaultInstance().where(PhoneStatus.class).findFirst();
        codeTv = (TextView) root.findViewById(R.id.tv_real_key_code);
        refreshBtn = (ImageButton) root.findViewById(R.id.btn_refresh_code);
        refreshBtn.setOnClickListener(view -> refreshCode());
        smsControlSwitch = (Switch) root.findViewById(R.id.switch_sms_recive);
        callControlSwitch = (Switch) root.findViewById(R.id.switch_call_recive);
        smsControlSwitch.setOncheckListener((view, check) -> smsRecive(check));
        callControlSwitch.setOncheckListener((view, check) -> callRecive(check));
        smsControlSwitch.setChecked(phoneStatus.useSmsRecive());
        callControlSwitch.setChecked(phoneStatus.useCallRecive());
        disconnectBtn = (ButtonFlat) root.findViewById(R.id.btn_disconnect);
        disconnectBtn.setOnClickListener(view -> disconnect());
        disconnectBtn.setEnabled(false);
        updateCode();
        return root;
    }

    @Override
    public void release() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        phoneStatus.useCallRecive(false);
        phoneStatus.useSmsRecive(false);
        phoneStatus.setKeyCode(null);
        realm.commitTransaction();
        restartService();
    }

    private void smsRecive(boolean check) {
        Realm realm = Realm.getDefaultInstance();
        if (check)
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS}, SMS_PERMISSION_REQUEST_CODE);
            } else {
                realm.beginTransaction();
                phoneStatus.useSmsRecive(true);
                realm.commitTransaction();
                restartService();
            }
        else {
            realm.beginTransaction();
            phoneStatus.useSmsRecive(false);
            realm.commitTransaction();
            restartService();
        }
    }

    private void callRecive(boolean check) {
        Realm realm = Realm.getDefaultInstance();
        if (check) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_PHONE_STATE}, CALL_PERMISSION_REQUEST_CODE);
            } else {
                realm.beginTransaction();
                phoneStatus.useCallRecive(true);
                realm.commitTransaction();
                restartService();
            }
        } else {
            realm.beginTransaction();
            phoneStatus.useCallRecive(false);
            realm.commitTransaction();
            restartService();
        }
    }

    private void restartService() {
        getActivity().stopService(new Intent(getActivity(), PhoneStatusMonitoringService.class));
        if (phoneStatus.useSmsRecive() || phoneStatus.useCallRecive()) {
            getActivity().startService(new Intent(getActivity(), PhoneStatusMonitoringService.class));
        }
    }

    @Override
    public void requestPermissionResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    SnackBar snackbar = new SnackBar(getActivity(), "권한을 허용하지 않아 SMS를 활용할 수 없습니다.", "확인", view -> {
                    });
                    snackbar.show();
                    return;
                }
            }
            smsRecive(true);
        } else if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    SnackBar snackbar = new SnackBar(getActivity(), "권한을 허용하지 않아 전화수신기능을 활용할 수 없습니다.", "확인", view -> {
                    });
                    snackbar.show();
                    return;
                }
            }
            callRecive(true);
        }
    }

    private void checkConnection() {
        disconnectBtn.setEnabled(true);
    }

    private void refreshCode() {
        if ((phoneStatus.getKeyCode()) == null) {
            updateCode();
            return;
        }
        new EasyAquery(getActivity()).setUrl(getString(R.string.real_mode_server_url) + "reset").addParam("type", "code").post(String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    phoneStatus.setKeyCode(object);
                    realm.commitTransaction();
                    codeTv.setText(object);
                }

            }
        });
    }

    private void updateCode() {
        if ((phoneStatus.getKeyCode()) == null) {
            EasyAquery aq = new EasyAquery(getActivity());
            aq.setUrl(getString(R.string.real_mode_server_url) + "register");
            aq.addParam("pnum", phoneStatus.getDeviceUID());
            aq.post(String.class, new AjaxCallback<String>() {
                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    phoneStatus.setKeyCode(object);
                    realm.commitTransaction();
                    String code = phoneStatus.getKeyCode();
                    if (code == null) {
                        codeTv.setText("코드를 가져오지 못했습니다.");
                        Toast.makeText(getActivity(), "서버 오류 발생", Toast.LENGTH_SHORT).show();
                    } else {
                        codeTv.setText(code);
                    }
                }
            });
        } else {
            codeTv.setText(phoneStatus.getKeyCode());
        }
    }

    private void disconnect() {
        String message = getResources().getString(R.string.refresh_alert_fake);
        new AlertDialog.Builder(getActivity()).setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshConnection();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void refreshConnection() {
        String url = getString(R.string.real_mode_server_url);
        new EasyAquery(getActivity()).setUrl(url + "reset").addParam("type", "conn").post();
    }
}
