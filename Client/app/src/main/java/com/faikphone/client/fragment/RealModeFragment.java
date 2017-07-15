package com.faikphone.client.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.faikphone.client.R;
import com.faikphone.client.network.EasyAquery;
import com.faikphone.client.utils.AppPreferences;

/**
 * Created by boxfox on 2017-07-16.
 */

public class RealModeFragment extends Fragment {
    private AppPreferences mAppPrefs;

    private TextView codeTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.real_mode_layout, container, false);
        mAppPrefs = new AppPreferences(getActivity());
        codeTv = (TextView)root.findViewById(R.id.tv_real_key_code);
        return root;
    }

    private void refreshCode(){
        if ((mAppPrefs.getKeyCode()) == null) {
            Toast.makeText(getActivity(), "등록되지 않은 기기 입니다. 인증 코드를 발급해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        new EasyAquery(getActivity()).setUrl(getString(R.string.real_mode_server_url) + "reset").addParam("type", "code").post(String.class, new AjaxCallback<String>() {

            @Override
            public void callback(String url, String object, AjaxStatus status) {
                if (object != null) {
                    mAppPrefs.setKeyCode(object);
                    new AlertDialog.Builder(getActivity()).setMessage(object)
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }

            }
        });
    }

    private void updateCode() {
        EasyAquery aq = new EasyAquery(getActivity());
        aq.setUrl(getString(R.string.real_mode_server_url) + "register");
        String code;
        if ((code = mAppPrefs.getKeyCode()) == null) {
            aq.addParam("pnum", mAppPrefs.getKeyDevicePhoneNumber());
            aq.post(String.class, new AjaxCallback<String>() {
                @Override
                public void callback(String url, String object, AjaxStatus status) {
                    mAppPrefs.setKeyCode(object);
                    String code2 = mAppPrefs.getKeyCode();
                    if (code2 == null) {
                        Toast.makeText(getActivity(), "서버 오류 발생", Toast.LENGTH_SHORT).show();
                    } else {
                        new AlertDialog.Builder(getActivity()).setMessage(code2)
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                })
                                .show();
                    }
                }
            });
        } else {
            new AlertDialog.Builder(getActivity()).setMessage(code)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

    private void disconnect(){
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
        String url = (mAppPrefs.getPhoneMode()) ? getString(R.string.fake_mode_server_url) : getString(R.string.real_mode_server_url);
        new EasyAquery(getActivity()).setUrl(url + "reset").addParam("type", "conn").post();
    }
}
