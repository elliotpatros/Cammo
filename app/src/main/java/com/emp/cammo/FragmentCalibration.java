package com.emp.cammo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

public class FragmentCalibration extends Fragment {

    // members
    ProgressBar mProgressBar = null;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // get reference to main activity
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) return;

        // setup progress bar
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar_calibration);
        setProgressBarVisibility(activity.getIsCalibrating());

        // tell main activity to start listening to the "start calibration" button
        view.findViewById(R.id.btn_start_calibration).setOnClickListener(activity);
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
