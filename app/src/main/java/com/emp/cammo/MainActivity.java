package com.emp.cammo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        TextView.OnEditorActionListener
{
    private final static String TAG = "MAIN_ACTIVITY";

    // child fragment state
    private Fragment mChildFragment = null;
    private static final String STATE_FRAGMENT = "STATE_FRAGMENT";

    // calibration state
    private boolean mIsCalibrating = false;
    private static final String STATE_IS_CALIBRATING = "STATE_IS_CALIBRATING";
    private String mCalibrationFolder = "/storage/extSdCard/Images/front-calibration";
    private static final String STATE_CALIB_FOLDER = "STATE_CALIB_FOLDER";

    // activity lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == savedInstanceState) {
            // no state to restore. set defaults
            setIsCalibrating(false);
            setChildFragment(FragmentMainMenu.newInstance());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (null == outState) return;
        outState.putBoolean(STATE_IS_CALIBRATING, getIsCalibrating());
        outState.putString(STATE_CALIB_FOLDER, getCalibrationFolder());
        getFragmentManager().putFragment(outState, STATE_FRAGMENT, mChildFragment);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (null == savedInstanceState) return;
        if (savedInstanceState.containsKey(STATE_CALIB_FOLDER)) {
            setCalibrationFolder(savedInstanceState.getString(STATE_CALIB_FOLDER));
        }
        if (savedInstanceState.containsKey(STATE_IS_CALIBRATING)) {
            setIsCalibrating(savedInstanceState.getBoolean(STATE_IS_CALIBRATING));
        }
        if (savedInstanceState.containsKey(STATE_FRAGMENT)) {
            setChildFragment(getFragmentManager().getFragment(savedInstanceState, STATE_FRAGMENT));
        }
    }

    @Override
    public void onBackPressed() {
        if (!onMainMenu()) {
            setChildFragment(FragmentMainMenu.newInstance());
        }
    }

    // fragment transactions
    private void setChildFragment(Fragment fragment) {
        if (null != fragment) {
            getFragmentManager().beginTransaction().replace(R.id.activity_main_layout, fragment).commit();
        }

        mChildFragment = fragment;
    }

    // handle user actions
    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            // open the calibration fragment
            case R.id.btn_goto_calibration:
                setChildFragment(FragmentCalibration.newInstance());
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

    // calibration
    private void startCalibrator() {
        // tell progress bar to start (this is just a placeholder function)
        // ... this should be internal to FragmentCalibration when its finished
        setIsCalibrating(true);
    }

    private void setIsCalibrating(boolean isCalibrating) {
        mIsCalibrating = isCalibrating;

        if (null != mChildFragment && FragmentCalibration.class == mChildFragment.getClass()) {
            ((FragmentCalibration) mChildFragment).setProgressBarVisibility(mIsCalibrating);
        }
    }

    public boolean getIsCalibrating() {
        return mIsCalibrating;
    }
    public String getCalibrationFolder() {return mCalibrationFolder; }
    public void setCalibrationFolder(String msg) {mCalibrationFolder = msg; }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        findViewById(R.id.activity_main_layout).requestFocus();
    }

    // fragment management
    private boolean onMainMenu() {
        return FragmentMainMenu.class == mChildFragment.getClass();
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            setCalibrationFolder(view.getText().toString());
            hideKeyboard(view);
            return true;
        }

        return false;
    }
}
