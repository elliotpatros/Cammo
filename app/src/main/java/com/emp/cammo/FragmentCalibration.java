package com.emp.cammo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class FragmentCalibration extends Fragment {

    // members
    private ProgressBar mProgressBar = null;
    private Button mButtonCalibrate = null;
    private EditText mEditText = null;

    // constructor
    public static FragmentCalibration newInstance() {
        return new FragmentCalibration();
    }

    // fragment lifecycle
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calibration, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // get reference to main activity
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) return;

        if (null != mProgressBar) {
            setProgressBarVisibility(activity.getIsCalibrating());
        }
        if (null != mButtonCalibrate) {
            mButtonCalibrate.setOnClickListener(activity);
        }
        if (null != mEditText) {
            mEditText.setText(activity.getCalibrationFolder());
            mEditText.setOnEditorActionListener(activity);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // setup widgets
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar_calibration);
        mButtonCalibrate = (Button) view.findViewById(R.id.btn_start_calibration);
        mEditText = (EditText) view.findViewById(R.id.editText_calibration_folder);
    }

    // widgets
    public void setProgressBarVisibility(boolean visible) {
        if (null == mProgressBar) return;

        if (visible) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
