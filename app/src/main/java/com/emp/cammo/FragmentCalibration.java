package com.emp.cammo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
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
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FragmentCalibration extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {

    // tag
    public static final String TAG = "FRAGMENT_CALIBRATION";
    private static final String mDefaultFolder = "/storage/emulated/0/DCIM/Screenshots";
    private boolean mIsInitialized = false;

    // widgets
    private ProgressBar mProgressBar = null;
    private Button mButtonCalibrate = null;
    private EditText mEditText = null;
    private TextView mTextView = null;
    private String mTextViewStatus = "press 'calibrate' to start";

    // calibration state
    private boolean mIsCalibrating;
    private String mCalibrationFolder;
    private CameraCalibrator mCalibrator; // can't be local. needs to be saved by setRetainInstance

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

        Log.d(TAG, "onResume: " + mIsInitialized);
        // set widgets
        try {
            // progress bar
            setIsCalibrating(mIsInitialized && mIsCalibrating);

            // button
            mButtonCalibrate.setOnClickListener(this);

            // edit text
            setCalibrationFolder(mIsInitialized ? mCalibrationFolder : mDefaultFolder);
            mEditText.setOnEditorActionListener(this);

            // mTextView
            mTextView.setText(mTextViewStatus);

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

    private void setTextViewStatus(String status) {
        mTextViewStatus = status;
        mTextView.setText(mTextViewStatus);
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

        // set progress bar
        if (null != mProgressBar) {
            mProgressBar.setVisibility(mIsCalibrating ? View.VISIBLE : View.GONE);
        }
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
            setTextViewStatus(values[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // get parent (MainActivity)
            MainActivity parent = (MainActivity) getActivity();
            if (null == parent) return;

            // update mCamera parameters
            parent.mCameraParameters = mRoutine.getCameraParameters();

            // update widgets
            setIsCalibrating(false);
            setTextViewStatus("finished");
            Log.i(TAG, "onPostExecute: " + parent.mCameraParameters.toString());
        }

        @Override
        protected String doInBackground(Void... params) {
            publishProgress("starting...");

            // get image path
            List<String> imagePaths = getImagePaths();

            // make a new calibration routine
            mRoutine = new CalibrationRoutine(getImageSize(imagePaths.get(0)));

            // find checkerboard points in each image
            for (int i = 0; i < imagePaths.size(); i++) {
                publishProgress(String.format(Locale.US, "processing image %d of %d", i+1, imagePaths.size()));
                String imagePath = imagePaths.get(i);
                Mat image = Imgcodecs.imread(imagePath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                mRoutine.processFrame(image);
            }

            publishProgress("calibrating...");
            mRoutine.calibrate();

            /* write test images to disk */ // deleteme!
            CameraParameters parameters = mRoutine.getCameraParameters();
            for (int i = 0; i < imagePaths.size(); i++) {
                publishProgress(String.format(Locale.US, "writing image %d of %d", i+1, imagePaths.size()));
                Mat distorted = Imgcodecs.imread(imagePaths.get(i), Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
                Mat undistorted = distorted.clone();
                Imgproc.undistort(distorted, undistorted, parameters.getCameraMatrix(), parameters.getDistortion());
                Imgcodecs.imwrite(String.format(Locale.US, "/storage/emulated/0/Download/undistorted-%d.png", i), undistorted);
            }

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
