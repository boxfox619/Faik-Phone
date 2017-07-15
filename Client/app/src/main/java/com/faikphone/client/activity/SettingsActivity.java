package com.faikphone.client.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.RequiresApi;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.faikphone.client.network.EasyAquery;
import com.faikphone.client.utils.AppPreferences;
import com.faikphone.client.R;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showfing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        private AppPreferences mAppPrefs;

        // fakeMode : true is fake mode, false is real mode.
        public boolean isFake;

        PreferenceScreen prefer_screen;

        SwitchPreference switchModeReference;

        EditTextPreference fakeConnectionPreference;

        Preference fakeDisconnectPreference;

        SwitchPreference fakeChangeBarPreference;

        Preference realCodeViewPreference;

        Preference realCodeRefreshPreference;

        Preference fakeCheckConnectionPreference;

        Preference realDisconnectPreference;

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAppPrefs = new AppPreferences(getActivity());
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            isFake = mAppPrefs.getPhoneMode();
            switchModeReference = (SwitchPreference) findPreference("fake_switch");
            switchModeReference.setOnPreferenceChangeListener(fakeSwitchChangeListener);
            switchModeReference.setChecked(mAppPrefs.getPhoneMode());

            prefer_screen = (PreferenceScreen) findPreference("prefer_screen");

            //Real Preferences


            realCodeViewPreference = findPreference("real_code_view");
            realCodeViewPreference.setOnPreferenceClickListener(realCodeVIewListener);

            realDisconnectPreference = findPreference("real_disconnect");
            realDisconnectPreference.setOnPreferenceClickListener(realDisconnectListener);

            ChangePreferences();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private Preference.OnPreferenceChangeListener fakeSwitchChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, final Object newValue) {
//                fakeChangeBarPreference.setChecked(!(Boolean) newValue);
                return false;
            }
        };

        private Preference.OnPreferenceClickListener fakeRefreshClickListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
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
                return true;
            }
        };

        private Preference.OnPreferenceClickListener realCodeVIewListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
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
                return true;
            }
        };

        private Preference.OnPreferenceClickListener realDisconnectListener = new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
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
                return true;
            }
        };


        private void ChangePreferences() {
            if (isFake) {
                prefer_screen.removePreference(realDisconnectPreference);
                prefer_screen.removePreference(realCodeViewPreference);
                prefer_screen.removePreference(realCodeRefreshPreference);
                prefer_screen.addPreference(fakeConnectionPreference);
                prefer_screen.addPreference(fakeDisconnectPreference);
                prefer_screen.addPreference(fakeChangeBarPreference);
                prefer_screen.addPreference(fakeCheckConnectionPreference);
            } else {
                prefer_screen.removePreference(fakeCheckConnectionPreference);
                prefer_screen.removePreference(fakeConnectionPreference);
                prefer_screen.removePreference(fakeDisconnectPreference);
                prefer_screen.removePreference(fakeChangeBarPreference);
                prefer_screen.addPreference(realCodeViewPreference);
                prefer_screen.addPreference(realCodeRefreshPreference);
                prefer_screen.addPreference(realDisconnectPreference);
            }
        }

        private void refreshConnection() {
            String url = (isFake) ? getString(R.string.fake_mode_server_url) : getString(R.string.real_mode_server_url);
            new EasyAquery(getActivity()).setUrl(url + "reset").addParam("type", "conn").post();
        }
    }
}
