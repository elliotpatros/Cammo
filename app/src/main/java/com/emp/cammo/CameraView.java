package com.emp.cammo;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.View;

import com.google.android.gms.vision.CameraSource;

import org.opencv.android.JavaCameraView;

import java.util.List;

public class CameraView extends JavaCameraView {
    //----------------------------------------------------------------------------------------------
    // constructor
    //----------------------------------------------------------------------------------------------
    public CameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setCameraIndex(CAMERA_ID_FRONT);
    }

    //----------------------------------------------------------------------------------------------
    // public methods
    //----------------------------------------------------------------------------------------------
    public void shutdown() {
        // make this widget invisible
        setVisibility(View.GONE);
        requestLayout();

        // stop camera from streaming
        if (null != mCamera) {
            mCamera.stopPreview();
        }

        // stop camera mechanism
        setCvCameraViewListener((CvCameraViewListener2)null);   // disable listener
        disconnectCamera();                                     // disconnect hardware
        disableView();                                          // disable widget
    }

    public void startup(CvCameraViewListener2 listener) {

        // disconnect old camera
        shutdown();

        // connect camera at best preview size
        Camera.Size bestSize = getBestPreviewSize();
        if (null != bestSize) {
            connectCamera(bestSize.width, bestSize.height);
        }

        // toggle view on
        disableView();
        enableView();

        // set camera preview size
        if (null != mCamera) {
            // find best supported camera preview size
            final Camera.Size previewSize = getBestPreviewSize();

            // get current camera parameters
            Camera.Parameters parameters = mCamera.getParameters();
            mCamera.stopPreview();

            // set ideal camera
            if (null != previewSize) {
                parameters.setPreviewSize(previewSize.width, previewSize.height);
            }

            // tell android os that this layout is dirty
            requestLayout();

            // start up camera
            try {
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

        // finish turning on
        enableView();
        setCvCameraViewListener(listener);
        setVisibility(View.VISIBLE);
        requestLayout();
    }

    public boolean isFacingFront() {
        return mCameraIndex == CAMERA_ID_FRONT;
    }

    public void setErrorCallback(Camera.ErrorCallback listener) {
        mCamera.setErrorCallback(listener);
    }

    //----------------------------------------------------------------------------------------------
    // convenience
    //----------------------------------------------------------------------------------------------
    private Camera.Size getBestPreviewSize() {
        if (null == mCamera) return null;

        final Camera.Size preferredSize = mCamera.getParameters().getPreferredPreviewSizeForVideo();
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();

        Camera.Size bestSize = null;
        int bestError = -1;

        for (Camera.Size size : previewSizes) {
            int error = Math.abs(preferredSize.width - size.width) + Math.abs(preferredSize.height - size.height);
            if (error < bestError || bestError < 0) {
                bestSize = size;
                bestError = error;
            }
        }

        return bestSize;
    }
}
