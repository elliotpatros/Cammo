package com.emp.cammo;

import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    // members
    static public final String TAG = "MainActivity";
    private Mat _img;
    private Calibrator _calibrator;

    // widgets
    private CustomCameraView _cameraView = null;
    private Button _cameraToggle = null;

    private BaseLoaderCallback _loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    if (_cameraView != null)
                        _cameraView.initialize();
                    updateCameraButtonText();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    // class functions
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // set up camera view
        _cameraView = (CustomCameraView) findViewById(R.id.HelloCameraView);
        _cameraView.setVisibility(SurfaceView.VISIBLE);
        _cameraView.setCvCameraViewListener(this);

        // set up toggle button
        _cameraToggle = (Button) findViewById(R.id.CameraViewToggle);
        updateCameraButtonText();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // initialize opencv asynchronously
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, getApplicationContext(), _loaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (_cameraView != null)
            _cameraView.disableView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (_cameraView != null)
            _cameraView.disableView();
    }



    // configuration and display
    @Override
    public void onConfigurationChanged(Configuration config) {
        // just ignore configuration changes
        super.onConfigurationChanged(config);
    }

    private void updateCameraButtonText() {
        if (_cameraView == null || _cameraToggle == null) return;

        CamId id = _cameraView.getCameraId();
        _cameraToggle.setText(id.name);
    }

    public void toggleCamera(View view) {
        if (_cameraView == null || _cameraToggle == null) return;

        CamId newId = _cameraView.getCameraId().toggle();
        _cameraView.setCameraId(newId);
        updateCameraButtonText();
    }

    // opencv
    @Override
    public void onCameraViewStarted(int width, int height) {
        // setup image buffer just once before streaming
        _img = new Mat(height, width, CvType.CV_8UC1);

        _calibrator = new Calibrator(height, width);
    }

    @Override
    public void onCameraViewStopped() {
        // after streaming, 'free' image buffer
        _img.release();
        _calibrator.release();
    }



    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        // tell the camera that everything is working
        _cameraView.itsWorking();

        // get grayscale image from camera (flip LR if its the front camera)
        _img = inputFrame.gray();
        if (_cameraView.getCameraId() == CamId.front) {
            Core.flip(_img, _img, 1); // flip horizontally (when flipCode > 0)
        }

        // find checkerboard
        _calibrator.findBoard(_img);

//        Log.i(TAG, "onCameraFrame..." + found);


        // draw rectangle
//        Imgproc.rectangle(_img, new Point(10, 10), new Point(100, 100), new Scalar(0, 255, 0));

        // return image to be shown on screen
        return _img;
    }
}

// checkout https://github.com/opencv/opencv/tree/master/samples/android/tutorial-3-cameracontrol/src/org/opencv/samples/tutorial3/
