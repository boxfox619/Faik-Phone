package com.boxfox.faikphone.service;

import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.boxfox.faikphone.NotificationBuilder;
import com.boxfox.faikphone.activity.CallActivity;
import com.boxfox.faikphone.data.PhoneStatus;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.URLDecoder;

import io.realm.Realm;

/**
 * Created by dsm_025 on 2017-04-13.
 */

public class FireBaseMessagingReceiver extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        PhoneStatus phoneStatus = Realm.getDefaultInstance().where(PhoneStatus.class).findFirst();
        try {
            JSONObject jsonObject = new JSONObject(remoteMessage.getData().get("json"));
            if (phoneStatus.mode()) {        // Fake Phone 일 경우
                switch (jsonObject.getString("event")) {
                    case "call":
                        Intent intent = new Intent(this, CallActivity.class);
                        intent.putExtra("name", jsonObject.getString("name"));
                        intent.putExtra("number", jsonObject.getString("number"));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                    case "call_miss":
                        Intent startMain = new Intent(Intent.ACTION_MAIN);
                        startMain.addCategory(Intent.CATEGORY_HOME);
                        startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        getApplicationContext().startActivity(startMain);
                        NotificationBuilder.missedCall(getApplicationContext(), jsonObject.getString("name") != null
                                ? jsonObject.getString("name") : null,jsonObject.getString("number"));
                        break;
                    case "sms":
                        NotificationBuilder.sms(getApplicationContext(), jsonObject.getString("name") != null
                                ? jsonObject.getString("number") : null, URLDecoder.decode(jsonObject.getString("content")));
                        break;
                }
            } else {
                    switch (jsonObject.getString("event")) {
                    case "call_receive":
                        break;
                    case "call_refusal":
                        try {
                            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                            Class<?> c = Class.forName(tm.getClass().getName());
                            Method m = c.getDeclaredMethod("getITelephony");
                            m.setAccessible(true);
                            Object telephonyService = m.invoke(tm);

                            c = Class.forName(telephonyService.getClass().getName());
                            m = c.getDeclaredMethod("endCall");
                            m.setAccessible(true);
                            m.invoke(telephonyService);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
