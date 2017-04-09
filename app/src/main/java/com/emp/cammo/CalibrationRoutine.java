package com.emp.cammo;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class CalibrationRoutine {
    private static final String TAG = "CalibrationRoutine";

    private final Size mPatternSize = new Size(9, 6); // (cols, rows)
    private final int mCornersSize = (int)(mPatternSize.width * mPatternSize.height);
    private MatOfPoint2f mCorners = new MatOfPoint2f();
    private List<Mat> mCornersBuffer = new ArrayList<>();
    private boolean mIsCalibrated = false;

    private Mat mCameraMatrix = new Mat();
    private Mat mDistortionCoefficients = new Mat();
    private double mSquareSize = 8; // millimeters
    private Size mImageSize; // pixels width by pixels height
    private final static int mFlags =
                    Calib3d.CALIB_FIX_ASPECT_RATIO +
                    Calib3d.CALIB_ZERO_TANGENT_DIST +
                    Calib3d.CALIB_FIX_K4 +
                    Calib3d.CALIB_FIX_K5;

    public CalibrationRoutine(Size imageSize) {
        mImageSize = imageSize;

        Mat.eye(3, 3, CvType.CV_64FC1).copyTo(mCameraMatrix);
        Mat.zeros(8, 1, CvType.CV_64FC1).copyTo(mDistortionCoefficients);
    }

    public void processFrame(Mat grayFrame) {
        int flags = Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_FILTER_QUADS + Calib3d.CALIB_CB_NORMALIZE_IMAGE;
        boolean patternWasFound = Calib3d.findChessboardCorners(grayFrame, mPatternSize, mCorners, flags);

        if (patternWasFound) {
            Imgproc.cornerSubPix(grayFrame,
                    mCorners,
                    new Size(5, 5),
                    new Size(-1, -1),
                    new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 100, 0.1));

            mCornersBuffer.add(mCorners.clone());
        }
    }

    public void calibrate() {
        List<Mat> rotationVectors = new ArrayList<>();
        List<Mat> translationVectors = new ArrayList<>();
        List<Mat> objectPoints = new ArrayList<>();

        objectPoints.add(Mat.zeros(mCornersSize, 1, CvType.CV_32FC3));
        calcBoardCornerPositions(objectPoints.get(0));
        for (int i = 1; i < mCornersBuffer.size(); i++) {
            objectPoints.add(objectPoints.get(0));
        }

//        double rms =
        Calib3d.calibrateCamera(objectPoints,
                mCornersBuffer,
                mImageSize,
                mCameraMatrix,
                mDistortionCoefficients,
                rotationVectors,
                translationVectors,
                mFlags);


        mIsCalibrated = Core.checkRange(mCameraMatrix) && Core.checkRange(mDistortionCoefficients);
    }

    public Mat getCameraMatrix() {
        return mCameraMatrix;
    }
    public Mat getDistortionCoefficients() {
        return mDistortionCoefficients;
    }

    private void calcBoardCornerPositions(Mat corners) {
        float positions[] = new float[mCornersSize * 3];
        int i = 0;
        for (int row = 0; row < mPatternSize.height; row++) {
            for (int col = 0; col < mPatternSize.width; col++) {
                positions[i++] = (float)(col * mSquareSize);
                positions[i++] = (float)(row * mSquareSize);
                positions[i++] = 0.f;
            }
        }

        corners.create(mCornersSize, 1, CvType.CV_32FC3);
        corners.put(0, 0, positions);
    }

}
