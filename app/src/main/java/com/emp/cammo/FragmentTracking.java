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
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

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
    private Bitmap mBitmap = null;
    final private Scalar mColor = new Scalar(0, 255, 0);
    final private static int mScale = 4;

    // widgets
    private CameraView mCameraView = null;
    private Button mButtonTglCamera = null;

    // data streaming task
    private String mIpAddress = null;
    private int mPortNumber = -1;
    private InetAddress mInetHost;

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
            mIpAddress = parent.mUserPreferences.mIpAddress;
            mPortNumber = Integer.valueOf(parent.mUserPreferences.mPortNumber);
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

        try {
            mInetHost = InetAddress.getByName(mIpAddress);
        } catch (UnknownHostException e) {
            mInetHost = null;
        }

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
                SparseArray<Face> mFaces = mFaceDetector.detect(faceFrame);
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

                    // stream face data
                    // Euler Z is ear to shoulder
                    // Euler Y is shaking head no
                    byte[] packet = paddedByteMessage("/face", face.getEulerY());
                    sendUdpFloat(packet);
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

    private void sendUdpFloat(byte[] bytes) {
        if (null != mInetHost) {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, mInetHost, mPortNumber);

            try {
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private byte[] paddedByteMessage(final String address, final float value) {
        final byte[] packet = address.getBytes();
        final int nBytes = (packet.length + 3) & ~3;
        final ByteBuffer buffer = ByteBuffer.allocate(nBytes + 4);
        buffer.put(packet);
        buffer.putFloat(nBytes, value);
        return buffer.array();
    }
}













// from: https://github.com/opencv/opencv/tree/master/samples/android/face-detection
// also check out: http://free-d.org/3d-head-tracking-in-video/
// opencv detector (just replace current functions with these snippets)
//    // face detector
//    final private static Scalar mColor = new Scalar(0, 255, 0);
//    final private static int mScale = 2;
//    private File mCascadeFile;
//    private CascadeClassifier mJavaDetector;
//    private float mRelativeFaceSize = 0.2f;
//    private float mAbsoluteFaceSize = 0;

//    @Override
//    public void onCameraViewStarted(int width, int height) { // size of stream or preview?
//        mCameraView.setErrorCallback(this);
//
//        // setup image buffer just once before streaming
//        mMatRgba = new Mat(height, width, CvType.CV_8UC4);
//        mMatGray = new Mat(height/mScale, width/mScale, CvType.CV_8UC1);
//
//        try {
//            mHost = InetAddress.getByName("192.168.0.10");
//        } catch (UnknownHostException e) {
//            mHost = null;
//        }
//
//        setupCascadeDetector();
//        updateButtonTglCamera();
//    }

//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
//        try {
//            // get color frame from mCamera
//            mMatRgba = frame.rgba();
//            Imgproc.resize(frame.gray(), mMatGray, mMatGray.size(), 0, 0, Imgproc.INTER_AREA);
//
//            // front mCamera is flipped, fix that here if it's active
//            if (mCameraView.isFacingFront()) {
//                Core.flip(mMatRgba, mMatRgba, Core.ROTATE_180);
//                Core.flip(mMatGray, mMatGray, Core.ROTATE_180);
//            }
//
//
//            if (mAbsoluteFaceSize == 0) {
//                int height = mMatGray.rows();
//                if (Math.round(height * mRelativeFaceSize) > 0) {
//                    mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
//                }
//            }
//
//            MatOfRect faces = new MatOfRect();
//
//            if (mJavaDetector != null) {
//                mJavaDetector.detectMultiScale(
//                        mMatGray,                                       // image
//                        faces,                                          // objects
//                        1.1,                                            // scale factor
//                        2,                                              // min Neighbors
//                        2,                                              // flags
//                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), // min size
//                        new Size());                                    // max size
//            }
//
//            Rect[] facesArray = faces.toArray();
//            if (0 < facesArray.length) {
//                Point tl = facesArray[0].tl();
//                tl.x *= mScale;
//                tl.y *= mScale;
//
//                Point br = facesArray[0].br();
//                br.x *= mScale;
//                br.y *= mScale;
//
//                Imgproc.rectangle(mMatRgba, tl, br, mColor, 2);
//            }
//
//            // return the image we want to preview
//            return mMatRgba;
//        } catch (RuntimeException e) {
//            return new Mat(frame.rgba().size(), frame.rgba().type(), new Scalar(0));
//        }
//    }

//    private void setupCascadeDetector() {
//        try {
//            File cascadeDir = getActivity().getDir("cascade", Context.MODE_PRIVATE);
//            mCascadeFile = new File(cascadeDir, "cascade_frontalface.xml.xml");
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//
//            InputStream is = getResources().openRawResource(R.raw.cascade_frontalface.xml);
//            FileOutputStream os = new FileOutputStream(mCascadeFile);
//            while ((bytesRead = is.read(buffer)) != -1) {
//                os.write(buffer, 0, bytesRead);
//            }
//            is.close();
//            os.close();
//
//            mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
//            if (mJavaDetector.empty()) {
//                Log.e(TAG, "setupCascadeDetector: Failed to load cascade classifier");
//                mJavaDetector = null;
//                return;
//            } else {
//                Log.i(TAG, "setupCascadeDetector: loaded cascade classifier from " + mCascadeFile.getAbsolutePath());
//            }
//
//            cascadeDir.delete();
//        } catch (Exception e) {
//            Log.e(TAG, "setupCascadeDetector: failed :-(");
//            e.printStackTrace();
//            mCascadeFile = null;
//            mJavaDetector = null;
//        }
//    }
