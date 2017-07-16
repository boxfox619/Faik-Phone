package com.boxfox.faikphone.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.boxfox.faikphone.network.EasyAquery;
import com.gc.materialdesign.views.ButtonFlat;

import io.realm.Realm;

/**
 * Created by boxfox on 2017-07-16.
 */

public class RealModeFragment extends Fragment {
    private PhoneStatus phoneStatus;

    private TextView codeTv;
    private ImageButton refreshBtn;
    private ButtonFlat disconnectBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.real_mode_layout, container, false);
        phoneStatus = Realm.getDefaultInstance().where(PhoneStatus.class).findFirst();
        codeTv = (TextView) root.findViewById(R.id.tv_real_key_code);
        refreshBtn = (ImageButton) root.findViewById(R.id.btn_refresh_code);
        refreshBtn.setOnClickListener(view -> refreshCode());
        disconnectBtn = (ButtonFlat) root.findViewById(R.id.btn_disconnect);
        disconnectBtn.setOnClickListener(view -> disconnect());
        disconnectBtn.setEnabled(false);
        updateCode();
        return root;
    }

    private void checkConnection(){
        disconnectBtn.setEnabled(true);
    }

    private void refreshCode() {
        if ((phoneStatus.getKeyCode()) == null) {
            Toast.makeText(getActivity(), "등록되지 않은 기기 입니다. 인증 코드를 발급해주세요.", Toast.LENGTH_SHORT).show();
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
