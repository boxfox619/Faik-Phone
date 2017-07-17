package com.boxfox.faikphone.fragment;

import android.app.Fragment;
import android.support.annotation.NonNull;

/**
 * Created by boxfox on 2017-07-17.
 */

public abstract class ModeFragment extends Fragment {

    public abstract void requestPermissionResult(int requestCode, @NonNull int[] grantResults);
    public abstract void release();

}
