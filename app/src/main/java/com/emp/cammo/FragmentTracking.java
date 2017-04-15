package com.emp.cammo;

import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.hardware.Camera;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


public class FragmentTracking extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    // member variables
    public int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Mat mMatRgba; // color image of current frame given by CameraView
    private Mat mMatGray; // gray  image of current frame given by CameraView
    private final static int mFindFlags = Calib3d.CALIB_CB_FAST_CHECK;

    // face detector
    FaceDetector mFaceDetector = null;
    SparseArray<Face> mFaces = null;
    Bitmap mBitmap = null;
    Scalar mColor = new Scalar(0, 255, 0);
    final static int mScale = 8;

    // widgets
    private CameraView mCameraView = null;

    // constructor
    public static FragmentTracking newInstance() {
        return new FragmentTracking();
    }

    // fragment lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracking, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // setup widgets
        mCameraView = (CameraView) view.findViewById(R.id.cameraView);
    }

    @Override
    public void onResume() {
        super.onResume();

        // setup mCamera view widget
        if (null != mCameraView) {
            mCameraView.setCvCameraViewListener(this);  // set listener for mCamera view callbacks
            mCameraView.enableView();                   // turn mCamera stream on
            mCameraView.setVisibility(View.VISIBLE);    // set widget visibility on

            if (Camera.CameraInfo.CAMERA_FACING_BACK != mCameraIndex) {
                mCameraView.setCameraIndex(mCameraIndex);
            }
        }

        // done
        setRetainInstance(true);

        // keep window on
        MainActivity parent = (MainActivity) getActivity();
        if (null != parent) {
            parent.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mCameraView) {
            mCameraView.disableView();
            mCameraView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (null != mCameraView) {
            mCameraView.disableView();
            mCameraView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // mCamera view callbacks
    @Override
    public void onCameraViewStarted(int width, int height) { // size of stream or preview?
        // setup image buffer just once before streaming
        mMatRgba = new Mat(height, width, CvType.CV_8UC4);
        mMatGray = new Mat(height/mScale, width/mScale, CvType.CV_8UC1);

        mFaceDetector = new FaceDetector.Builder(getActivity().getBaseContext())
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();
//        mFaceDetector.setProcessor(
//                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
        // checkout https://github.com/googlesamples/android-vision/blob/master/visionSamples/FaceTracker/app/src/main/java/com/google/android/gms/samples/vision/face/facetracker/FaceTrackerActivity.java

        mFaces = new SparseArray<>(1);
        mBitmap = Bitmap.createBitmap(width/mScale, height/mScale, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void onCameraViewStopped() {
        // 'free' image buffer after streaming
        mMatRgba.release();
        mFaceDetector.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        // get color frame from mCamera
        mMatRgba = frame.rgba();
        Imgproc.resize(frame.gray(), mMatGray, mMatGray.size(), 0, 0, Imgproc.INTER_AREA);

        // front mCamera is flipped, fix that here if it's active
        if (Camera.CameraInfo.CAMERA_FACING_FRONT == mCameraIndex) {
            Core.flip(mMatRgba, mMatRgba, Core.ROTATE_180);
            Core.flip(mMatGray, mMatGray, Core.ROTATE_180);
        }

        if (mFaceDetector.isOperational()) {
            // convert Mat to Frame
            Utils.matToBitmap(mMatGray, mBitmap);
            Frame faceFrame = new Frame.Builder().setBitmap(mBitmap).build();

            // detect face
            mFaces = mFaceDetector.detect(faceFrame);

            if (0 < mFaces.size()) {
                final Face face = mFaces.valueAt(0);
                PointF pointf = face.getPosition();
                pointf.x *= mScale;
                pointf.y *= mScale;
                final float w = face.getWidth() * mScale;
                final float h = face.getHeight() * mScale;
                Imgproc.rectangle(
                        mMatRgba,
                        new Point(pointf.x, pointf.y),
                        new Point(pointf.x + w, pointf.y + h),
                        mColor);

                Log.i("onCameraFrame", String.valueOf(face.getEulerY()));
            }
        }

        // return the image we want to preview
        return mMatRgba;
    }

}
