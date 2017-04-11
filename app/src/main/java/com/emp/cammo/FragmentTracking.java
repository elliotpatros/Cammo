package com.emp.cammo;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class FragmentTracking extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {
    // fragment settings
    public static final int SCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    public static final boolean HOME_ARROW = false;
    public static final int THEME = R.style.AppTheme_FullScreen;

    static private final String TAG = "FragmentTracking";

    // widgets
    private CameraView mCameraView = null;
    private Mat imgRgb; // image of current camera view frame

    // constructor
    public static FragmentTracking newInstance() {
        return new FragmentTracking();
    }

    // fragment lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get parent activity (MainActivity)
        MainActivity parent = (MainActivity) getActivity();
        if (null != parent) {
            // set screen orientation
//            parent.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            // keep window on
            parent.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//            // hide status bar
//            View decorView = parent.getWindow().getDecorView();
//            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//            decorView.setSystemUiVisibility(uiOptions);
//            ActionBar actionBar = parent.getActionBar();
//            actionBar.hide();
        }
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

        // setup widgets
        try {
            // set listener
            mCameraView.setCvCameraViewListener(this);
            mCameraView.enableView();
            mCameraView.setVisibility(View.VISIBLE);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // done
        setRetainInstance(true);
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

    // camera view callbacks
    @Override
    public void onCameraViewStarted(int width, int height) {
        // setup image buffer just once before streaming
        imgRgb = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        // 'free' image buffer after streaming
        imgRgb.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame frame) {
        imgRgb = frame.rgba();

//        imgRot = imgRgb.t();
//        Core.flip(imgRgb.t(), imgRot, 1);
//        Imgproc.resize(imgRot, imgRot, imgRgb.size());

//        mRgba = inputFrame.rgba();
//        Mat mRgbaT = mRgba.t();
//        Core.flip(mRgba.t(), mRgbaT, 1);
//        Imgproc.resize(mRgbaT, mRgbaT, mRgba.size());
//        return mRgbaT;


        return imgRgb;
    }
}
