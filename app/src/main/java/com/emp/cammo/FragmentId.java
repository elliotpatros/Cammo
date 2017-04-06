package com.emp.cammo;

import android.app.Fragment;

public enum FragmentId {
    MainMenu ("MainMenu", 0),
    Calibration ("Calibration", 1),
    Tracking ("Tracking", 2);

    public static final String TAG = "FragmentId";
    public final String name;
    public final int id;

    FragmentId(String arg_name, int arg_id) {
        name = arg_name;
        id = arg_id;
    }

    public Fragment newInstance() {
        switch (this) {
            case MainMenu: return FragmentMainMenu.newInstance();
            case Calibration: return FragmentCalibration.newInstance();
            case Tracking: return null;
            default: return null;
        }
    }
}
