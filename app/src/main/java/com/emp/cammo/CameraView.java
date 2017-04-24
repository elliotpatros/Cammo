package com.emp.cammo;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

import java.util.List;

public class CameraView extends JavaCameraView {
    final static private String TAG = "CameraView";

    // constructor
    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        disconnectCamera();
    }

    public void disconnect() {
        disconnectCamera();
    }

    public void connect() {
        if (null != mCamera) {
            final Camera.Size preferredSize = mCamera.getParameters().getPreferredPreviewSizeForVideo();

            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = getBestPreviewSize(previewSizes, preferredSize.width, preferredSize.height);

            connectCamera(previewSize.width, previewSize.height);
        }
    }

    public boolean isConnected() {
        return mCamera != null;
    }

    public void setErrorCallback(Camera.ErrorCallback callback) {
        if (null != mCamera) {
            mCamera.setErrorCallback(callback);
        }
    }

    private Camera.Size getBestPreviewSize(List<Camera.Size> sizes, int w, int h) {
        Camera.Size bestSize = null;
        int bestError = 9999999;

        for (Camera.Size size : sizes) {
            int error = Math.abs(w - size.width) + Math.abs(h - size.height);
            if (error < bestError) {
                bestSize = size;
                bestError = error;
            }
        }

        return bestSize;
    }
}

