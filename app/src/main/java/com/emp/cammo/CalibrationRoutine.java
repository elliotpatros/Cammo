package com.emp.cammo;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;

public class CalibrationRoutine {
    private static final String TAG = "CalibrationRoutine";

    private final Size mPatternSize = new Size(9, 6);
    private final int mCornersSize = (int)(mPatternSize.width * mPatternSize.height);
    private MatOfPoint2f mCorners = new MatOfPoint2f();
    private List<Mat> mCornersBuffer = new ArrayList<>();
    private boolean mIsCalibrated = false;

    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();
    private int mFlags;
    private double mRms;
    private double mSquareSize = 0.008;
    private Size mImageSize;

    public CalibrationRoutine(Size imageSize) {
        mImageSize = imageSize;
        mFlags = Calib3d.CALIB_FIX_PRINCIPAL_POINT + // todo: check all of these flags
                Calib3d.CALIB_ZERO_TANGENT_DIST +
                Calib3d.CALIB_FIX_ASPECT_RATIO +
                Calib3d.CALIB_USE_INTRINSIC_GUESS + // todo: not sure if this is a good idea
                Calib3d.CALIB_FIX_K4 +
                Calib3d.CALIB_FIX_K5;
        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
//        mCameraMatrix.put(0, 0, 1.0);
        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    public void processFrame(Mat grayFrame) {
        int flags = Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_FILTER_QUADS + Calib3d.CALIB_CB_NORMALIZE_IMAGE;
        boolean patternWasFound = Calib3d.findChessboardCorners(grayFrame, mPatternSize, mCorners, flags);

        if (patternWasFound) {
            mCornersBuffer.add(mCorners.clone());
        }
    }

    public void calibrate() {
        List<Mat> rvecs = new ArrayList<>();
        List<Mat> tvecs = new ArrayList<>();
        Mat reprojectionErrors = new Mat();
        List<Mat> objectPoints = new ArrayList<>();
        objectPoints.add(Mat.zeros(mCornersSize, 1, CvType.CV_32FC3));
        calcBoardCornerPositions(objectPoints.get(0));
        for (int i = 1; i < mCornersBuffer.size(); i++) {
            objectPoints.add(objectPoints.get(0));
        }

//        mCameraMatrix = Calib3d.initCameraMatrix2D(mCorners, mCornersBuffer, mImageSize);

        Calib3d.calibrateCamera(objectPoints,
                mCornersBuffer,
                mImageSize,
                mCameraMatrix,
                mDistortionCoefficients,
                rvecs,
                tvecs,
                mFlags);

        Calib3d.getOptimalNewCameraMatrix(mCameraMatrix, mDistortionCoefficients, mImageSize, 0.);

        mIsCalibrated = Core.checkRange(mCameraMatrix) && Core.checkRange(mDistortionCoefficients);

        mRms = computeReprojectionErrors(objectPoints, rvecs, tvecs, reprojectionErrors);
        Log.i(TAG, String.format("Average re-projection error: %f", mRms));
        Log.i(TAG, "Camera matrix: " + mCameraMatrix.dump());
        Log.i(TAG, "Distortion coefficients: " + mDistortionCoefficients.dump());
    }

    public void clearCorners() {
        mCornersBuffer.clear();
    }

    private void calcBoardCornerPositions(Mat corners) {
        final int cn = 3;
        float positions[] = new float[mCornersSize * cn];

        for (int i = 0; i < mPatternSize.height; i++) {
            for (int j = 0; j < mPatternSize.width * cn; j += cn) {
                positions[(int) (i * mPatternSize.width * cn + j + 0)] =
                        (2 * (j / cn) + i % 2) * (float) mSquareSize;
                positions[(int) (i * mPatternSize.width * cn + j + 1)] =
                        i * (float) mSquareSize;
                positions[(int) (i * mPatternSize.width * cn + j + 2)] = 0;
            }
        }
        corners.create(mCornersSize, 1, CvType.CV_32FC3);
        corners.put(0, 0, positions);
    }

    private double computeReprojectionErrors(List<Mat> objectPoints,
                                             List<Mat> rvecs, List<Mat> tvecs, Mat perViewErrors) {
        MatOfPoint2f cornersProjected = new MatOfPoint2f();
        double totalError = 0;
        double error;
        float viewErrors[] = new float[objectPoints.size()];
        MatOfDouble distortionCoefficients = new MatOfDouble(mDistortionCoefficients);
        int totalPoints = 0;
        for (int i = 0; i < objectPoints.size(); i++) {
            MatOfPoint3f points = new MatOfPoint3f(objectPoints.get(i));
            Calib3d.projectPoints(points, rvecs.get(i), tvecs.get(i), mCameraMatrix, distortionCoefficients, cornersProjected);
            error = Core.norm(mCornersBuffer.get(i), cornersProjected, Core.NORM_L2);
            int n = objectPoints.get(i).rows();
            viewErrors[i] = (float) Math.sqrt(error * error / n);
            totalError  += error * error;
            totalPoints += n;
        }
        perViewErrors.create(objectPoints.size(), 1, CvType.CV_32FC1);
        perViewErrors.put(0, 0, viewErrors);

        return Math.sqrt(totalError / totalPoints);
    }
}

//public class CalibrationRoutine {
//    private static final String TAG = "CalibrationRoutine";
//
//    // member variables
//    private final Size mPatternSize = new Size(6, 9); // checkerboard size
//    private final int mCornersSize = (int)(mPatternSize.width * mPatternSize.height); // nCorners
//    private boolean mPatternWasFound = false;
//    private MatOfPoint2f mCorners = new MatOfPoint2f(); // corner locations (in image)
//    private List<Mat> mCornersBuffer = new ArrayList<>(); // ?
//    private boolean mIsCalibrated = false;
//
//    private Mat mCameraMatrix = new Mat(); // intrinsics
//    private Mat mDistortionCoefficients = new Mat();
//    private int mFlags;
//    private double mRms;
//    private double mSquareSize = 0.0181; // todo: units???
//    private Size mImageSize;
//
//    // constructor
//    public CalibrationRoutine(Size imageSize) {
//        mImageSize = imageSize;
//        mFlags = Calib3d.CALIB_FIX_PRINCIPAL_POINT + Calib3d.CALIB_ZERO_TANGENT_DIST +
//                 Calib3d.CALIB_FIX_ASPECT_RATIO + Calib3d.CALIB_FIX_K4 + Calib3d.CALIB_FIX_K5;
//        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
//        mCameraMatrix.put(0, 0, 1.0);
//        Mat.zeros(5, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);
//        Log.i(TAG, "instantiated new CalibrationRoutine " + this.getClass());
//    }
//
//    // public functions
//    public void processFrame(Mat grayFrame) {
//        int flags = Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_FILTER_QUADS + Calib3d.CALIB_CB_NORMALIZE_IMAGE;
//        mPatternWasFound = findChessboardCorners(grayFrame, mPatternSize, mCorners, flags);
//    }
//
//    public void calibrate() {
//        ArrayList<Mat> rvecs = new ArrayList<>();
//        ArrayList<Mat> tvecs = new ArrayList<>();
//        Mat reprojectionErrors = new Mat();
//        ArrayList<Mat> objectPoints = new ArrayList<Mat>();
//        objectPoints.add(Mat.zeros(mCornersSize, 1, CvType.CV_32FC3));
//        calcBoardCornerPositions(objectPoints.get(0));
//        for (int i = 1; i < mCornersBuffer.size(); i++) {
//            objectPoints.add(objectPoints.get(0));
//        }
//
//        Calib3d.calibrateCamera(objectPoints, mCornersBuffer, mImageSize,
//                mCameraMatrix, mDistortionCoefficients, rvecs, tvecs, mFlags);
//
//        mIsCalibrated = Core.checkRange(mCameraMatrix)
//                && Core.checkRange(mDistortionCoefficients);
//
////        ArrayList<Mat> rvecs2 = new ArrayList<>();
////        for (Mat mat : rvecs) {
////            mat.reshape(3);
//////            Mat temp = new Mat(mat.size(), CvType.CV_64FC3);
//////            Imgproc.cvtColor(mat, temp, Imgproc.COLOR_GRAY2BGR, 0);
//////            mat.convertTo(temp, CvType.CV_64FC3);
//////            rvecs2.add(temp.clone());
////        }
////
////        ArrayList<Mat> tvecs2 = new ArrayList<>();
////        for (Mat mat : tvecs) {
////            Mat temp = new Mat(mat.size(), CvType.CV_32FC3);
////            mat.convertTo(temp, CvType.CV_32FC3);
////            tvecs2.add(temp.clone());
////        }
//
//        mRms = computeReprojectionErrors(objectPoints, rvecs, tvecs, reprojectionErrors);
//        Log.i(TAG, String.format("Average re-projection error: %f", mRms));
//        Log.i(TAG, "Camera matrix: " + mCameraMatrix.dump());
//        Log.i(TAG, "Distortion coefficients: " + mDistortionCoefficients.dump());
//    }
//
//    public void clearCorners() {
//        mCornersBuffer.clear();
//    }
//
//    public void addCorners() {
//        if (mPatternWasFound) {
//            mCornersBuffer.add(mCorners.clone());
//        }
//    }
//
//    // private functions
//    private void calcBoardCornerPositions(Mat corners) {
//        final int cn = 3; // dimensions
//        float positions[] = new float[mCornersSize * cn];
//
//        for (int i = 0; i < mPatternSize.height; i++) {
//            for (int j = 0; j < mPatternSize.width * cn; j += cn) {
//                positions[(int) (i * mPatternSize.width * cn + j + 0)] =
//                        (2 * (j / cn) + i % 2) * (float) mSquareSize;
//                positions[(int) (i * mPatternSize.width * cn + j + 1)] =
//                        i * (float) mSquareSize;
//                positions[(int) (i * mPatternSize.width * cn + j + 2)] = 0;
//            }
//        }
//        corners.create(mCornersSize, 1, CvType.CV_32FC3);
//        corners.put(0, 0, positions);
//    }
//
//    private double computeReprojectionErrors(List<Mat> objectPoints,
//                                             List<Mat> rvecs, List<Mat> tvecs, Mat perViewErrors) {
//        MatOfPoint2f cornersProjected = new MatOfPoint2f();
//        double totalError = 0;
//        double error;
//        float viewErrors[] = new float[objectPoints.size()];
//
//        MatOfDouble distortionCoefficients = new MatOfDouble(mDistortionCoefficients);
//        int totalPoints = 0;
//        for (int i = 0; i < objectPoints.size(); i++) {
//            MatOfPoint3f points = new MatOfPoint3f(objectPoints.get(i));
//            Calib3d.projectPoints(points, rvecs.get(i).reshape(3), tvecs.get(i).reshape(3),
//                    mCameraMatrix, distortionCoefficients, cornersProjected);
//            error = Core.norm(mCornersBuffer.get(i), cornersProjected, Core.NORM_L2);
//
//            int n = objectPoints.get(i).rows();
//            viewErrors[i] = (float) Math.sqrt(error * error / n);
//            totalError  += error * error;
//            totalPoints += n;
//        }
//        perViewErrors.create(objectPoints.size(), 1, CvType.CV_32FC1);
//        perViewErrors.put(0, 0, viewErrors);
//
//        return Math.sqrt(totalError / totalPoints);
//    }
//}
