package com.emp.cammo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";

    // child fragment state
    private Fragment mChildFragment = null;
    private final static String stateChildFragment = "ChildFragment";

    // calibration state
    private boolean mIsCalibrating = false;
    private static final String stateIsCalibrating = "IsCalibrating";

    // activity lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // is there a fragment to restore?
        replaceFragment(getFragmentManager().findFragmentByTag(stateChildFragment));
        if (null == mChildFragment)
            replaceFragment(FragmentMainMenu.newInstance());

        if (null != savedInstanceState) return;
            setIsCalibrating(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (null == outState) return;
        outState.putBoolean(stateIsCalibrating, getIsCalibrating());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (null == savedInstanceState) return;
        setIsCalibrating(savedInstanceState.getBoolean(stateIsCalibrating));
    }

    // fragment transactions
    private void replaceFragment(Fragment fragment) {
        replaceFragment(fragment, false);
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        if (null == fragment) return;

        FragmentTransaction transaction = getFragmentManager().beginTransaction()
                .replace(R.id.activity_main_layout, fragment, stateChildFragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        mChildFragment = fragment;
    }


    // handle user actions
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            // open the calibration fragment
            case R.id.btn_goto_calibration:
                replaceFragment(FragmentCalibration.newInstance(), true);
                break;

            // open the tracking fragment
            case R.id.btn_goto_tracking:
                Log.i(TAG, "onClick: GOTO TRACKING");
                break;

            // start calibration script
            case R.id.btn_start_calibration:
                Log.i(TAG, "onClick: START CALIBRATION");
                startCalibrator();
                break;

            default: break;
        }
    }

    private void startCalibrator() {
        // tell progress bar to start (this is just a placeholder function)
        // ... this should be internal to FragmentCalibration when its finished
        setIsCalibrating(true);
    }


    // calibration
    private void setIsCalibrating(boolean isCalibrating) {
        mIsCalibrating = isCalibrating;

        if (null != mChildFragment && // activity has a child fragment
            FragmentCalibration.class == mChildFragment.getClass()) // the current class is FragmentCalibration
        {
            ((FragmentCalibration) mChildFragment).setProgressBarVisibility(mIsCalibrating);
        }
    }

    public boolean getIsCalibrating() {
        return mIsCalibrating;
    }
}
