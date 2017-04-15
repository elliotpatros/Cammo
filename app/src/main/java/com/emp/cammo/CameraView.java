package com.emp.cammo;

import android.content.Context;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

public class CameraView extends JavaCameraView {
    // constructor
    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected boolean initializeCamera(int width, int height) {
        return super.initializeCamera(width, height);
    }

    @Override
    public void setCameraIndex(int cameraIndex) {
        if (cameraIndex != mCameraIndex) {
            enableView();
            disableView();
            super.setCameraIndex(cameraIndex);
            enableView();
        }
    }
}
