package com.boxfox.faikphone.listener;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.boxfox.faikphone.R;
import com.boxfox.faikphone.network.EasyAquery;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

public class PhoneStateRead extends PhoneStateListener {
    private Context context;

    String TAG = "PHONE STATE READ";

    public PhoneStateRead(Context context) {
        this.context = context;
    }

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.i(TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_IDLE " + incomingNumber);
                try {
                    EasyAquery aq = new EasyAquery(context);
                    JSONObject messageJSON = new JSONObject();
                    messageJSON.put("event", "call_miss");
                    messageJSON.put("name", "");
                    messageJSON.put("number", incomingNumber);
                    messageJSON.put("time", "11:00");
                    aq.setUrl(context.getString(R.string.real_mode_server_url));
                    aq.addParam("token", FirebaseInstanceId.getInstance().getToken()).addParam("message", messageJSON.toString()).post();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.i(TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_OFFHOOK " + incomingNumber);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.i(TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_RINGING " + incomingNumber);
                try {
                    JSONObject messageJSON = new JSONObject();
                    messageJSON.put("event", "call");
                    messageJSON.put("name", "");
                    messageJSON.put("number", incomingNumber);
                    String token = FirebaseInstanceId.getInstance().getToken();
                    String url = context.getString(R.string.real_mode_server_url);
                    new EasyAquery(context).setUrl(url).addParam("token", token).addParam("message", messageJSON.toString()).post();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
                Log.i(TAG, "MyPhoneStateListener->onCallStateChanged() -> default -> " + Integer.toString(state));
                break;
        }
    }
}