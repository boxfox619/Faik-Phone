package com.boxfox.faikphone.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.boxfox.faikphone.R;
import com.boxfox.faikphone.data.PhoneStatus;
import com.boxfox.faikphone.receiver.CallReceiver;
import com.boxfox.faikphone.receiver.SMSReceiver;

import io.realm.Realm;

/**
 * Created by boxfox on 2017-07-17.
 */

public class PhoneStatusMonitoringService extends Service {
    private PhoneStatus phoneStatus;
    private SMSReceiver smsReceiver;
    private CallReceiver callReceiver;

    private boolean callReceiverRegistered;
    private boolean smsReceiverRegistered;

    @Override
    public void onCreate() {
        super.onCreate();
        smsReceiver = new SMSReceiver();
        callReceiver = new CallReceiver();
        phoneStatus = Realm.getDefaultInstance().where(PhoneStatus.class).findFirst();
        if (phoneStatus.useCallRecive()) {
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(callReceiver, filter);
            callReceiverRegistered = true;
        }
        if (phoneStatus.useSmsRecive()) {
            IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
            registerReceiver(smsReceiver, filter);
            smsReceiverRegistered = true;
        }
        createChannel();
        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setTicker("Faikphone");
        mBuilder.setContentTitle("Faikphone");
        mBuilder.setContentText("Faikphone Service가 실행중입니다.");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId(getClass().getName());
        }
        startForeground(1, mBuilder.build());
    }

    private void createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(getClass().getName(), "Faikphone Real mode service", NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription("SMS 또는 전화 수신을 가짜폰에 전달하기 위해서 작동되는 Service입니다.");
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callReceiverRegistered) {
            unregisterReceiver(callReceiver);
        }
        if (smsReceiverRegistered) {
            unregisterReceiver(smsReceiver);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
