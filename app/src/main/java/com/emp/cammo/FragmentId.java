package com.emp.cammo;

import android.app.Fragment;
import android.content.pm.ActivityInfo;

public enum FragmentId {
    MainMenu ("MainMenu", 0),
    Calibration ("Calibration", 1),
    Preferences ("Preferences", 2),
    Tracking ("Tracking", 3, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

    public static final String TAG = "FragmentId";
    public final String name;
    public final int id;
    public final int orientation;

    FragmentId(String arg_name, int arg_id) {
        this(arg_name, arg_id, ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    FragmentId(String arg_name, int arg_id, int arg_orientation) {
        name = arg_name;
        id = arg_id;
        orientation = arg_orientation;
    }

    public Fragment newInstance() {
        switch (this) {
            case MainMenu:    return FragmentMainMenu.newInstance();
            case Calibration: return FragmentCalibration.newInstance();
            case Preferences: return FragmentPreferences.newInstance();
            case Tracking:    return FragmentTracking.newInstance();
            default:          return null;
        }
    }

}
