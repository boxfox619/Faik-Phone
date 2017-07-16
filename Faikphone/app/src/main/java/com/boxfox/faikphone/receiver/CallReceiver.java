package com.boxfox.faikphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import com.boxfox.faikphone.listener.PhoneStateRead;

/**
 * Created by BeINone on 2017-05-11.
 */

public class CallReceiver extends BroadcastReceiver {
    //    call
//    {
//        "type" : "call",
//            "number" : "010-2222-2222",
//            "name" : "" (NullAble)
//    }


    @Override
    public void onReceive(Context context, Intent intent) {
        PhoneStateRead phoneListener = new PhoneStateRead(context);
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener,PhoneStateRead.LISTEN_SERVICE_STATE);
        telephony.listen(phoneListener,PhoneStateRead.LISTEN_CALL_STATE);
//
//        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
//            boolean phoneMode = new AppPreferences(context).getPhoneMode();
//            if (!phoneMode) {
//                HttpClient httpClient = new RealHttpClient(context);
//                String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
//                try {
//                    JSONObject messageJSON = new JSONObject();
//                    messageJSON.put("event", "call");
//                    messageJSON.put("name", "");
//                    messageJSON.put("number", phoneNumber);
//                    String token = FirebaseInstanceId.getInstance().getToken();
//                    httpClient.doSendMessage(messageJSON, token);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }
}
