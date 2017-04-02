package com.emp.cammo;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import org.opencv.android.JavaCameraView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CustomCameraView extends JavaCameraView {

    // member variables
    public static final String TAG = "CustomCameraView";
    private CamId mCameraId = CamId.back;
    private boolean _tryingRestart = false;

    // constructor
    public CustomCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // overridden member functions
    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        super.surfaceChanged(arg0, arg1, arg2, arg3);
        // setMaxResolution();
    }

    public void initialize() {
        enableView();
        setCameraId(mCameraId);
    }

    public void itsWorking() {
        _tryingRestart = false;
    }

    // resolution
    private void setResolution(Camera.Size size) {
        // bail if...
        if (size == null ||                                         // argument is invalid
                mCamera == null ||                                      // camera isn't initialized
                mCamera.getParameters().getPreviewSize().equals(size))  // new size equals old size
            return;

        disconnectCamera();
        mMaxWidth = size.width;
        mMaxHeight = size.height;
        connectCamera(getWidth(), getHeight());
    }

    private void setMaxResolution() {
        // get supported sizes
        if (mCamera == null) return;
        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        if (sizes == null) return; // sizes guaranteed not to be null

        // sort from largest to smallest
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                return b.width * b.height - a.width * a.height;
            }
        });

        // set largest size
        setResolution(sizes.get(0));
    }

    // camera id
    public void setCameraId(CamId id) {
        mCameraId = id;
        if (mCamera == null) return;

        disableView();
        setCameraIndex(mCameraId.id);
        enableView();
        setupCameraErrorHandling();
    }

    public CamId getCameraId() {
        if (mCamera == null) return CamId.none;
        return mCameraId;
    }

    private void setupCameraErrorHandling() {
        mCamera.setErrorCallback(new Camera.ErrorCallback() {
            @Override
            public void onError(int error, Camera camera) {
                if (!_tryingRestart) {
                    setCameraId(mCameraId);
                    _tryingRestart = true;
                } else {
                    mCameraId = CamId.none;
                }
            }
        });
    }
}
