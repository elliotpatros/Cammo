package com.emp.cammo;

import android.os.Bundle;
import android.util.Log;

public class UserPreferences {
    public final static String TAG = "UserPreferences";
    public final static String TAG_IP_ADDRESS = "IP_ADDRESS";
    public final static String TAG_PORT_NUMBER = "PORT_NUMBER";

    public String mIpAddress;
    public String mPortNumber;

    public UserPreferences() {
        this(null);
    }

    public UserPreferences(Bundle bundle) {
        handleBundle(bundle);
    }

    private void setDefaults() {
        mIpAddress = "192.168.0.10";
        mPortNumber = "9800";
    }

    private void handleBundle(Bundle bundle) {
        if (null != bundle) {
            final boolean hasIpAddress = bundle.containsKey(TAG_IP_ADDRESS);
            final boolean hasPortNumber = bundle.containsKey(TAG_PORT_NUMBER);

            if (hasIpAddress && hasPortNumber) {
                mIpAddress = bundle.getString(TAG_IP_ADDRESS);
                mPortNumber = bundle.getString(TAG_PORT_NUMBER);
                return;
            }
        }

        setDefaults();
    }

    public void saveWithBundle(Bundle bundle) {
        if (null == bundle) return;

        bundle.putString(TAG_IP_ADDRESS, mIpAddress);
        bundle.putString(TAG_PORT_NUMBER, mPortNumber);
    }
}
