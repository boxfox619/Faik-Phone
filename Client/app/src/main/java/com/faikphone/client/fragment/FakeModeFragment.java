package com.faikphone.client.fragment;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.faikphone.client.R;
import com.faikphone.client.network.EasyAquery;
import com.faikphone.client.utils.AppPreferences;
import com.google.firebase.iid.FirebaseInstanceId;
import com.roughike.swipeselector.OnSwipeItemSelectedListener;
import com.roughike.swipeselector.SwipeItem;
import com.roughike.swipeselector.SwipeSelector;

/**
 * Created by boxfox on 2017-07-16.
 */

public class FakeModeFragment extends Fragment {
    private AppPreferences mAppPrefs;

    private EditText realCodeEditText;
    private ImageButton connectBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fake_mode_layout, container, false);
        mAppPrefs = new AppPreferences(getActivity());
        connectBtn = (ImageButton) root.findViewById(R.id.btn_connect_real);
        realCodeEditText = (EditText) root.findViewById(R.id.et_real_code);
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
                Log.d("Test", item.value + "");
                mAppPrefs.setFakeStatusBarMode(((int) item.value) == 0 ? false : true);
                mAppPrefs.setMobileCarrier(item.title);
                getActivity().sendBroadcast(new Intent(getString(R.string.preferences_changed_broadcast)));
            }
        });
        return root;
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
                    mAppPrefs.setRealPhoneNum(object);
                    Toast.makeText(getActivity(), "연결에 성공했습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "연결에 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void ok() {
        //TODO 테스트 시에만 고정 토큰 넣어줌
        if (mAppPrefs.getKeyRealPhoneNum() != null) {
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
}
