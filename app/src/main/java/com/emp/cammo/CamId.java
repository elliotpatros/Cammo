package com.emp.cammo;


import android.hardware.Camera;

public enum CamId {
    none(-1, "no camera"),
    back(Camera.CameraInfo.CAMERA_FACING_BACK, "back"),
    front(Camera.CameraInfo.CAMERA_FACING_FRONT, "front");

    int id;
    String name;
    CamId(int i, String str) {
        id = i;
        name = str;
    }

    public CamId toggle() {
        switch (this) {
            case back:
                return front;
            case front:
                return back;
            default:
                return none;
        }
    }
}
