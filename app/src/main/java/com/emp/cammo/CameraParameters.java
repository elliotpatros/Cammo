package com.emp.cammo;

import android.os.Bundle;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.Locale;

public class CameraParameters {
    // state tags
    private final static String TAG = "CameraParameters";
    private final static String stateIsCalibrated = "IS_CALIBRATED";
    private final static String stateCameraMatrix = "CAMERA_MATRIX";
    private final static String stateDistortionCoefficients = "DISTORTION_COEFFICIENTS";

    // private member variables
    private boolean mIsCalibrated = false;
    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();

    // constructors
    public CameraParameters() {
        this(null);
    }

    public CameraParameters(Bundle bundle) {
        mIsCalibrated = false;
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);

        restoreState(bundle);
    }

    // save and restore state
    public void restoreState(Bundle bundle) {
        if (null == bundle) return;

        // try to restore mIsCalibrated
        if (bundle.containsKey(stateIsCalibrated)) {
            mIsCalibrated = bundle.getBoolean(stateIsCalibrated);
        }

        // try to restore mCameraMatrix
        if (bundle.containsKey(stateCameraMatrix)) {
            mCameraMatrix = loadMatrix(bundle, stateCameraMatrix);
        }

        // try to restore mDistortion coefficients
        if (bundle.containsKey(stateDistortionCoefficients)) {
            mDistortionCoefficients = loadMatrix(bundle, stateDistortionCoefficients);
        }

        Log.i(TAG, "restoreState: " + this.toString());
    }

    public void saveState(Bundle bundle) {
        if (null == bundle) return;

        // try to save mIsCalibrated
        bundle.putBoolean(stateIsCalibrated, mIsCalibrated);

        // try to save mCameraMatrix
        saveMatrix(bundle, stateCameraMatrix, mCameraMatrix);

        // try to save mDistortionCoefficients
        saveMatrix(bundle, stateDistortionCoefficients, mDistortionCoefficients);
    }

    // sets
    public void checkIsCalibrated() {
        mIsCalibrated = Core.checkRange(mCameraMatrix) &&
                        Core.checkRange(mDistortionCoefficients);
    }

    // gets
    public Mat getCameraMatrix() {
        return mCameraMatrix;
    }

    public Mat getDistortion() {
        return mDistortionCoefficients;
    }

    // matrix/bundle conversions
    private void saveMatrix(Bundle bundle, String key, Mat mat) {
        if (null == bundle) return;

        bundle.putBoolean(key, true);

        // get metadata
        final int nRows = mat.rows();
        final int nCols = mat.cols();
        final int type = mat.type();

        // save metadata
        bundle.putInt(key + "_rows", nRows);
        bundle.putInt(key + "_cols", nCols);
        bundle.putInt(key + "_type", type);

        // save values
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                String location = String.format("_loc:%d,%d", row, col);
                bundle.putDoubleArray(key + location, mat.get(row, col));
            }
        }
    }

    private Mat loadMatrix(Bundle bundle, String key) {
        if (!bundle.containsKey(key)) return new Mat();

        // get metadata
        final int nRows = bundle.getInt(key + "_rows");
        final int nCols = bundle.getInt(key + "_cols");
        final int type = bundle.getInt(key + "_type");


        Mat mat = new Mat(nRows, nCols, type);

        // get values
        for (int row = 0; row < nRows; row++) {
            for (int col = 0; col < nCols; col++) {
                String location = String.format("_loc:%d,%d", row, col);
                double[] data = bundle.getDoubleArray(key + location);
                mat.put(row, col, data);
            }
        }

        return mat;
    }

    // debug
    @Override
    public String toString() {

        // format mCamera matrix
        String camera = String.format(Locale.US, "Camera matrix: %s", MatToString(mCameraMatrix));

        // format mDistortion coefficients
        String distort = String.format(Locale.US, "Distortion coefficients: %s", MatToString(mDistortionCoefficients));

        return camera + "\n" + distort;
    }

    private String MatToString(Mat mat) {
        if (null == mat) return "null";

        String s = "";
        final int nRows = mat.rows();
        final int nCols = mat.cols() - 1;
        for (int row = 0; row < nRows; row++) {
            s += "\n\t";
            for (int col = 0; col < nCols; col++) {
                s += mat.get(row, col)[0] + ", ";
            }
            s += mat.get(row, nCols)[0];
        }

        return s;
    }
}
