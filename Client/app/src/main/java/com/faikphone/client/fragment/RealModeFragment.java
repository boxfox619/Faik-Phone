package com.faikphone.client.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faikphone.client.R;

/**
 * Created by boxfox on 2017-07-16.
 */

public class RealModeFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.real_mode_layout, container, false);
    }
}
