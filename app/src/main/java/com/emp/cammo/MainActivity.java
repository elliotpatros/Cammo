package com.emp.cammo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FragmentId mCurrentFragmentId = null;
    public CameraParameters mCameraParameters = null;
    public UserPreferences mUserPreferences = null;
    private Bundle mBundle = null;

    static {System.loadLibrary("opencv_java3");}

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

        // restore user preferences
        mUserPreferences = new UserPreferences(savedInstanceState);

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

        // get camera parameters from bundle
        try {
            mBundle = null;
            mCameraParameters = new CameraParameters(savedInstanceState);
        } catch (UnsatisfiedLinkError e) {
            mCameraParameters = null;
            mBundle = savedInstanceState;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        if (null != mUserPreferences) {
            mUserPreferences.saveToBundle(outState);
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
            case R.id.btn_goto_preferences:
                setChildFragment(FragmentId.Preferences);
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

        // save fragment id name
        outState.putString(FragmentId.TAG, mCurrentFragmentId.name);

        // save mCamera parameters
        if (null != mCameraParameters) {
            mCameraParameters.saveState(outState);
        }
    }

    @Override
    public void onBackPressed() {
        if (!onMainMenu()) {
            setChildFragment(FragmentId.MainMenu);
        } else {
            super.onBackPressed();
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

            // activity layout changes
            setRequestedOrientation(fragmentId.orientation);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
