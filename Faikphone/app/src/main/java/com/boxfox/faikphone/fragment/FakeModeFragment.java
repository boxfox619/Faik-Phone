package com.boxfox.faikphone.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.boxfox.faikphone.R;
import com.boxfox.faikphone.data.PhoneStatus;
import com.boxfox.faikphone.network.EasyAquery;
import com.boxfox.faikphone.service.FakeStatusBarService;
import com.gc.materialdesign.views.ButtonFlat;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roughike.swipeselector.OnSwipeItemSelectedListener;
import com.roughike.swipeselector.SwipeItem;
import com.roughike.swipeselector.SwipeSelector;

import io.realm.Realm;

/**
 * Created by boxfox on 2017-07-16.
 */

public class FakeModeFragment extends ModeFragment {
    private static final int REQUEST_MANAGE_OVERLAY_PERMISSION = 11;
    private PhoneStatus phoneStatus;

    private EditText realCodeEditText;
    private ImageButton connectBtn;
    private ButtonFlat disconnectBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fake_mode_layout, container, false);
        phoneStatus = Realm.getDefaultInstance().where(PhoneStatus.class).findFirst();
        connectBtn = (ImageButton) root.findViewById(R.id.btn_connect_real);
        realCodeEditText = (EditText) root.findViewById(R.id.et_real_code);
        connectBtn.setOnClickListener(view -> codeInputOk());
        realCodeEditText.setText(phoneStatus.getKeyCode());
        disconnectBtn = (ButtonFlat) root.findViewById(R.id.btn_disconnect);
        disconnectBtn.setOnClickListener(view -> disconnect());
        disconnectBtn.setEnabled(false);
        root.findViewById(R.id.btn_disconnect).setOnClickListener(view -> disconnect());
        SwipeSelector swipeSelector = (SwipeSelector) root.findViewById(R.id.selector_mobile_carrier);
        swipeSelector.setItems(
                new SwipeItem(0, "사용안함", "가상 상단바를 사용하지 않습니다."),
                new SwipeItem(1, "SKT", "SKT 통신사의 가상 상단바를 사용합니다."),
                new SwipeItem(2, "KT", "KT 통신사의 가상 상단바를 사용합니다."),
                new SwipeItem(3, "LG U+", "LG U+ 통신사의 가상 상단바를 사용합니다.")
        );
        swipeSelector.setOnItemSelectedListener(new OnSwipeItemSelectedListener() {
            @Override
            public void onItemSelected(SwipeItem item) {
                if (checkDrawOverlayPermission()) {
                    getActivity().startService(new Intent(getActivity(), FakeStatusBarService.class));
                }
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                phoneStatus.setStatusBar(((int) item.value) == 0 ? false : true);
                phoneStatus.setMobileCarrier(item.title);
                realm.commitTransaction();
                getActivity().sendBroadcast(new Intent(getString(R.string.preferences_changed_broadcast)));
            }
        });
        return root;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_MANAGE_OVERLAY_PERMISSION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(getActivity())) {
                        getActivity().startService(new Intent(getActivity(), FakeStatusBarService.class));
                        getActivity().finish();
                    }
                }
                break;
            default:
                break;
        }
    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(getActivity())) {
                startActivityForResult(
                        new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), REQUEST_MANAGE_OVERLAY_PERMISSION);
                return false;
            }
        }
        return true;
    }


    private void connect() {
        EasyAquery aq = new EasyAquery(getActivity());
        aq.setUrl(getString(R.string.fake_mode_server_url) + "register");
        aq.addParam("token", FirebaseInstanceId.getInstance().getToken());
        aq.addParam("code", realCodeEditText.getText());
        aq.post(String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (status.getCode() == 200) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    phoneStatus.setKeyCode(realCodeEditText.getText().toString());
                    realm.beginTransaction();
                    Toast.makeText(getActivity(), "연결에 성공했습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void codeInputOk() {
        //TODO 테스트 시에만 고정 토큰 넣어줌
        if (phoneStatus.getKeyCode() != null) {
            new AlertDialog.Builder(getActivity()).setMessage("이미 연결 되어 있습니다. 다른 기기에 연결하시겠습니까? ")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            connect();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        } else {
            connect();
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

    private void checkConnection() {
        EasyAquery aq = new EasyAquery(getActivity());
        aq.setUrl(getString(R.string.fake_mode_server_url) + "checkconn");
        aq.post(String.class, new AjaxCallback<String>() {
            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (phoneStatus.getKeyCode() == null) {
                    Toast.makeText(getActivity(), "연결되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                } else
                    new AlertDialog.Builder(getActivity()).setMessage("연결된 기기 : " + phoneStatus.getKeyCode())
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .show();
            }
        });
    }


    private void refreshConnection() {
        String url = (phoneStatus.mode()) ? getString(R.string.fake_mode_server_url) : getString(R.string.real_mode_server_url);
        new EasyAquery(getActivity()).setUrl(url + "reset").addParam("type", "conn").post();
    }

    @Override
    public void requestPermissionResult(int requestCode, @NonNull int[] grantResults) {

    }

    @Override
    public void release() {

    }
}
