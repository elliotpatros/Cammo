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
import android.widget.Button;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class FragmentTracking extends Fragment
        implements
        CameraBridgeViewBase.CvCameraViewListener2,
        Camera.ErrorCallback,
        View.OnClickListener {

    private final static String TAG = "FragmentTracking";

    // member variables
    private Mat mMatRgba; // color image of current frame given by CameraView
    private Mat mMatGray; // gray  image of current frame given by CameraView

    // face detector
    private FaceDetector mFaceDetector = null;
    private SparseArray<Face> mFaces = null;
    private Bitmap mBitmap = null;
    final private Scalar mColor = new Scalar(0, 255, 0);
    final private static int mScale = 4;

    // widgets
    private CameraView mCameraView = null;
    private Button mButtonTglCamera = null;

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
        mButtonTglCamera = (Button) view.findViewById(R.id.btn_tgl_camera);
        mButtonTglCamera.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCamera();

        // keep window on
        MainActivity parent = (MainActivity) getActivity();
        if (null != parent) {
            parent.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // mCamera view callbacks
    @Override
    public void onCameraViewStarted(int width, int height) { // size of stream or preview?
        mCameraView.setErrorCallback(this);

        // setup image buffer just once before streaming
        mMatRgba = new Mat(height, width, CvType.CV_8UC4);
        mMatGray = new Mat(height/mScale, width/mScale, CvType.CV_8UC1);

        mFaceDetector = new FaceDetector.Builder(getActivity().getBaseContext())
                .setTrackingEnabled(true)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();

        mBitmap = Bitmap.createBitmap(width/mScale, height/mScale, Bitmap.Config.ARGB_8888);

        updateButtonTglCamera();
    }

    @Override
    public void onCameraViewStopped() {
        stopCamera();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        try {
            // get color frame from mCamera
            mMatRgba = frame.rgba();
            Imgproc.resize(frame.gray(), mMatGray, mMatGray.size(), 0, 0, Imgproc.INTER_AREA);

            // front mCamera is flipped, fix that here if it's active
            if (mCameraView.isFacingFront()) {
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
                    pointf.set(pointf.x * mScale, pointf.y * mScale * 1.25f);
                    final float w = face.getWidth() * mScale;
                    final float h = face.getHeight() * mScale;
                    Imgproc.rectangle(
                            mMatRgba,
                            new Point(pointf.x, pointf.y),
                            new Point(pointf.x + w, pointf.y + h),
                            mColor);

//                Log.i("onCameraFrame", String.valueOf(face.getEulerY()));
                }
            }

            // return the image we want to preview
            return mMatRgba;
        } catch (RuntimeException e) {
            return new Mat(frame.rgba().size(), frame.rgba().type(), new Scalar(0));
        }
    }

    private void stopCamera() {
        if (null != mMatRgba) {
            mMatRgba.release();
            mMatRgba = null;
        }

        if (null != mMatGray) {
            mMatGray.release();
            mMatGray = null;
        }

        if (null != mFaceDetector) {
            mFaceDetector.release();
            mFaceDetector = null;
        }

        mCameraView.shutdown();
    }

    private void startCamera() {
        mCameraView.startup(this);
    }

    private void toggleCamera() {
        if (null != mCameraView) {
            mCameraView.toggleCamera(this);
            updateButtonTglCamera();
        }
    }

    private void updateButtonTglCamera() {
        if (null != mButtonTglCamera) {
            final boolean facingFront = mCameraView.isFacingFront();
            final int btnLabel = facingFront ? R.string.btn_camera_front : R.string.btn_camera_back;
            mButtonTglCamera.setText(btnLabel);
        }
    }

    @Override
    public void onError(int error, Camera camera) {
        startCamera();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_tgl_camera:
                toggleCamera();
                break;
            default:
                break;
        }
    }
}
