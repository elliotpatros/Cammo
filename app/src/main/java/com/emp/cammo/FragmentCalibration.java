package com.emp.cammo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentCalibration extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {
    // tag
    public static final String TAG = "FRAGMENT_CALIBRATION";
    private static final String mDefaultFolder = "/storage/extSdCard/Images/front-calibration";
    private boolean mIsInitialized = false;

    // widgets
    private ProgressBar mProgressBar = null;
    private Button mButtonCalibrate = null;
    private EditText mEditText = null;
    private TextView mTextView = null;

    // calibration state
    private boolean mIsCalibrating;
    private String mCalibrationFolder;
    private CameraCalibrator mCalibrator;

    // constructor
    public static FragmentCalibration newInstance() {
        return new FragmentCalibration();
    }

    // fragment lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calibration, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // setup widgets
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar_calibration);
        mButtonCalibrate = (Button) view.findViewById(R.id.btn_start_calibration);
        mEditText = (EditText) view.findViewById(R.id.editText_calibration_folder);
        mTextView = (TextView) view.findViewById(R.id.textView_calibration);
    }

    @Override
    public void onResume() {
        super.onResume();

        // set widgets
        try {
            // progress bar
            setIsCalibrating(mIsInitialized && mIsCalibrating);
            // button
            mButtonCalibrate.setOnClickListener(this);
            // edit text
            setCalibrationFolder(mIsInitialized ? mCalibrationFolder : mDefaultFolder);
            mEditText.setOnEditorActionListener(this);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // done
        mIsInitialized = true;
        setRetainInstance(true);
    }

    // handle user actions
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_calibration:
                startCalibration();
                break;
            default:
                break;
        }
    }

    // widgets
    public void hideKeyboard(View done) {
        try {
            Activity activity = getActivity();

            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(done.getWindowToken(), 0);
            View gets = activity.findViewById(R.id.background_fragment_calibration);

            gets.requestFocus();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void updateProgressBarVisibility() {
        if (null == mProgressBar) return;
        mProgressBar.setVisibility(mIsCalibrating ? View.VISIBLE : View.GONE);
    }

    private void setCalibrationFolder(String text) {
        // todo: semaphore
        mCalibrationFolder = text;
        if (!mEditText.getText().toString().equals(mCalibrationFolder)) {
            mEditText.setText(mCalibrationFolder);
        }
    }

    private String getCalibrationFolder() {
        // todo: semaphore
        return mCalibrationFolder;
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (R.id.editText_calibration_folder == view.getId()) {
            setCalibrationFolder(view.getText().toString());

            if (EditorInfo.IME_ACTION_DONE == actionId) {
                hideKeyboard(view);
            }
            return true;
        }

        return false;
    }

    // calibration
    private void startCalibration() {
        if (!getIsCalibrating()) {
            mCalibrator = new CameraCalibrator();
            mCalibrator.execute();
        }
    }

    public boolean getIsCalibrating() {
        return mIsCalibrating;
    }

    public void setIsCalibrating(boolean isCalibrating) {
        mIsCalibrating = isCalibrating;
        updateProgressBarVisibility();
    }

    private class CameraCalibrator extends AsyncTask<Void, String, String> {
        CalibrationRoutine mRoutine;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setIsCalibrating(true);
            mRoutine = null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mTextView.setText(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setIsCalibrating(false);
            mTextView.setText("finished!");
        }

        @Override
        protected String doInBackground(Void... params) {
            publishProgress("starting...");
            List<String> imagePaths = getImagePaths();
            String imagePath = imagePaths.get(0);
            Size imageSize = getImageSize(imagePath);
            mRoutine = new CalibrationRoutine(imageSize);

            for (int i = 0; i < imagePaths.size(); i++) {
                publishProgress(String.format(Locale.ENGLISH, "processing image %d of %d", i+1, imagePaths.size()));
                imagePath = imagePaths.get(i);
                Mat image = Imgcodecs.imread(imagePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                mRoutine.processFrame(image);
            }

            publishProgress("calibrating...");
            mRoutine.calibrate();

            return null;
        }

        private List<String> getImagePaths() {
            // open picture directory
            File directory = new File(getCalibrationFolder());
            File[] files = directory.listFiles();

            // setup path list
            ArrayList<String> images = new ArrayList<>(files.length);
            for (File file : files) {
                images.add(file.getAbsolutePath());
            }

            return images;
        }

        private Size getImageSize(String imagePath) {
            Mat image = Imgcodecs.imread(imagePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
            return image.size();
        }
    }
}













//    private class CameraCalibrator extends AsyncTask<Void, Void, String> {
//        private static final String TAG = "CameraCalibrator";
//        private Mat image;
//
//        // todo: fix this lazy shit
////        private Mat objectPoint;
////        private final Size chessboardSize = new Size(9, 6);
////        private final int squareSize = 8;
//        private final int findCheckerboardFlags =
//                CALIB_CB_ADAPTIVE_THRESH + CALIB_CB_FILTER_QUADS + CALIB_CB_NORMALIZE_IMAGE;
//
//        // calibration stuff
//        ArrayList<Mat> objectPoints = new ArrayList<>();
//        ArrayList<Mat> imagePoints = new ArrayList<>();
//
//        // camera stuff
//        Mat intrinsic = new Mat();
//        Mat distCoefs = new Mat();
//        ArrayList<Mat> rvecs = new ArrayList<>();
//        ArrayList<Mat> tvecs = new ArrayList<>();
//
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            setIsCalibrating(true);
//        }
//
//        @Override
//        protected void onPostExecute(String message) {
//            super.onPostExecute(message);
//            setIsCalibrating(false);
//            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//            // setup object points
//            setupConstants();
//
//            // find images
//            List<String> imagePaths = getImagesInFolder();
//
//            // process images
//            processImages(imagePaths);
//
//            return "woo hoo finished!";
//        }
//
//        // private functions
//        private void setupConstants() { // todo: fix laziness
//            // aspect ratio todo: check that this isn't wrong
//            intrinsic = Mat.eye(3, 3, CV_64F);
//            // possibly: intrinsic.put(0, 0, aspectRatio)
//
//            rvecs.add(new Mat());
//            tvecs.add(new Mat());
//
//            // object point
//            // todo this is fucked...
//            final int nRows = (int) chessboardSize.height;
//            final int nCols = (int) chessboardSize.width;
//            objectPoint = new Mat(nRows * nCols, 1, CV_64F);
//
//            int index = 0;
//            for (int row = 0; row < nRows; row++) {
//                for (int col = 0; col < nCols; col++) {
//                    double[] point = {col * squareSize, row * squareSize, 0};
//                    objectPoint.put(index++, 1, point);
//                }
//            }
//        }
//
//        private List<String> getImagesInFolder() {
//            // open picture directory
//            File directory = new File(getCalibrationFolder());
//            File[] files = directory.listFiles();
//
//            // setup path list
//            ArrayList<String> images = new ArrayList<>(files.length);
//            for (File file : files) {
//                images.add(file.getAbsolutePath());
//            }
//
//            return images;
//        }
//
//        private void processImages(List<String> imagePaths) {
//            MatOfPoint2f corners = new MatOfPoint2f();
//
//            for (int i = 0; i < imagePaths.size(); i++) { //String imagePath : imagePaths) {
//                // get this image path
//                final String imagePath = imagePaths.get(i);
//
//                // find chessboards
//                image = imread(imagePath, 0 /* return gray image */);
//                boolean foundCorners = findChessboardCorners(image, chessboardSize, corners, findCheckerboardFlags);
//                if (foundCorners) {
//                    imagePoints.add(corners);
//                }
//
//                // report object points (world points in matlab)
//                objectPoints.add(objectPoint.clone());
//
//                // todo: start here!
//                calibrateCamera(objectPoints, imagePoints, image.size(), intrinsic, distCoefs, rvecs, tvecs);
//            }
//        }
//    }
//}

//    interface CalibrationCallbacks {
//        void onPreExecute();
//        void onCancelled();
//        void onPostExecute();
//    }
//}
