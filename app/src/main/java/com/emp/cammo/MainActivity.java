package com.emp.cammo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = "MAIN_ACTIVITY";
    private FragmentId mCurrentFragmentId;
    public CameraParameters mCameraParameters;
    private Bundle mBundle = null;

    // opencv loader
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    if (null == mCameraParameters) {
                        mCameraParameters = new CameraParameters(mBundle);
                    }
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    // activity lifecycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // if there's no state to restore, set everything to default
        if (null == savedInstanceState) {
            // setup main menu
            setChildFragment(FragmentId.MainMenu);
            return;
        }

        // restore fragment id
        if (savedInstanceState.containsKey(FragmentId.TAG)) {
            final String name = savedInstanceState.getString(FragmentId.TAG);
            final FragmentId id = FragmentId.valueOf(name);
            setChildFragment(id);
        }
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // save bundle
        try {
            mBundle = null;
            mCameraParameters = new CameraParameters(savedInstanceState);
        } catch (UnsatisfiedLinkError e) {
            Log.i(TAG, "onCreate: opencv isn't available yet");
            mCameraParameters = null;
            mBundle = savedInstanceState;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_goto_calibration:
                setChildFragment(FragmentId.Calibration);
                break;
            case R.id.btn_goto_tracking:
                setChildFragment(FragmentId.Tracking);
                break;
            default: break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null == outState) return;

        // save camera parameters
        mCameraParameters.saveState(outState);

        // save fragment id name
        outState.putString(FragmentId.TAG, mCurrentFragmentId.name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!onMainMenu()) {
            setChildFragment(FragmentId.MainMenu);
        }
    }

    // fragment management
    private boolean onMainMenu() {
        return FragmentId.MainMenu.id == mCurrentFragmentId.id;
    }

    private void setChildFragment(FragmentId fragmentId) {
        try {
            // remember the new fragment id
            mCurrentFragmentId = fragmentId;

            // try to restore the fragment first, and get a new one if that fails
            FragmentManager manager = getFragmentManager();
            Fragment fragment = manager.findFragmentByTag(fragmentId.name);
            if (null == fragment) {fragment = fragmentId.newInstance(); }

            // replace old fragment with new one
            manager.beginTransaction().replace(R.id.activity_main_layout, fragment, fragmentId.name).commit();

            // todo start here
            // theme should be changed before setting any view (in onCreate())
            // activity layout changes
            switch (fragmentId) {
                case MainMenu:
                    setRequestedOrientation(FragmentMainMenu.SCREEN_ORIENTATION);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(FragmentMainMenu.HOME_ARROW);
                    setTheme(FragmentMainMenu.THEME);
                    break;
                case Calibration:
                    setRequestedOrientation(FragmentCalibration.SCREEN_ORIENTATION);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(FragmentCalibration.HOME_ARROW);
                    setTheme(FragmentCalibration.THEME);
                    break;
                case Tracking:
                    setRequestedOrientation(FragmentTracking.SCREEN_ORIENTATION);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(FragmentTracking.HOME_ARROW);
                    setTheme(FragmentTracking.THEME);
                    break;
                default:
                    break;
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
