package com.boxfox.faikphone.data;

import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.RealmClass;

/**
 * Created by boxfox on 2017-07-16.
 */

@RealmClass
public class PhoneStatus extends RealmObject {
    private String deviceUID;
    private String mobileCarrier;

    private String keyCode;

    private boolean mode;
    private boolean statusBar;

    public void init(){
        deviceUID = UUID.randomUUID().toString();
        mode = true;
    }

    public boolean useStatusBar() {
        return statusBar;
    }

    public void setStatusBar(boolean statusBar) {
        this.statusBar = statusBar;
    }

    public boolean mode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public String getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(String keyCode) {
        this.keyCode = keyCode;
    }

    public String getMobileCarrier() {
        return mobileCarrier;
    }

    public void setMobileCarrier(String mobileCarrier) {
        this.mobileCarrier = mobileCarrier;
    }

    public String getDeviceUID() {
        return deviceUID;
    }

    public void setDeviceUID(String deviceUID) {
        this.deviceUID = deviceUID;
    }
}
