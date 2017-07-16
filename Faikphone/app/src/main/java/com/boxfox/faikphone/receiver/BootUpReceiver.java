package com.boxfox.faikphone.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.boxfox.faikphone.service.FakeStatusBarService;
import com.boxfox.faikphone.service.FireBaseMessagingReceiver;

/**
 * Created by BeINone on 2017-04-14.
 */

public class BootUpReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        context.startActivity(new Intent(context, MainActivity.class));
        context.startService(new Intent(context, FakeStatusBarService.class));
        context.startService(new Intent(context, FireBaseMessagingReceiver.class));
    }
}
