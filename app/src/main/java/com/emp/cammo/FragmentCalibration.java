package com.emp.cammo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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

public class FragmentCalibration extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {
    // tag
    public static final String TAG = "FRAGMENT_CALIBRATION";
    private static final String mDefaultFolder = "/storage/extSdCard/Images/front-calibration";
    private boolean mIsInitialized = false;

    // widgets
    private ProgressBar mProgressBar = null;
    private Button mButtonCalibrate = null;
    private EditText mEditText = null;

    // calibration state
    private boolean mIsCalibrating;
    private String mCalibrationFolder;

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
    }

    @Override
    public void onResume() {
        super.onResume();

        // set widgets
        if (null != mProgressBar) {
            setIsCalibrating(mIsInitialized && mIsCalibrating);
        }
        if (null != mButtonCalibrate) {
            mButtonCalibrate.setOnClickListener(this);
        }
        if (null != mEditText) {
            mEditText.setOnEditorActionListener(this);
            setCalibrationFolder(mIsInitialized ? mCalibrationFolder : mDefaultFolder);
        }

        mIsInitialized = true;
        setRetainInstance(true);
    }

    // handle user actions
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_calibration:
                Log.i(TAG, "onClick: START CALIBRATION");
                setIsCalibrating(true);
                break;
            default: break;
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

    public boolean getIsCalibrating() {return mIsCalibrating; }
    private void setIsCalibrating(boolean isCalibrating) {
        mIsCalibrating = isCalibrating;
        updateProgressBarVisibility();
    }

    private void updateProgressBarVisibility() {
        if (null == mProgressBar) return;
        mProgressBar.setVisibility(mIsCalibrating ? View.VISIBLE : View.GONE);
    }

    private void setCalibrationFolder(String text) {
        mCalibrationFolder = text;
        if (!mEditText.getText().toString().equals(mCalibrationFolder)) {
            mEditText.setText(mCalibrationFolder);
        }
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
}
