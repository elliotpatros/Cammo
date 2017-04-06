package com.emp.cammo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public final static String TAG = "MAIN_ACTIVITY";
    private FragmentId mCurrentFragmentId;

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

        // restore state
        if (savedInstanceState.containsKey(FragmentId.TAG)) {
            setChildFragment(FragmentId.valueOf(savedInstanceState.getString(FragmentId.TAG)));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_goto_calibration:
                setChildFragment(FragmentId.Calibration);
                break;
            default: break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (null == outState) return;

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
            setHomeArrow();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void setHomeArrow() {
        try {
            boolean shouldBeVisible = !onMainMenu();
            getSupportActionBar().setDisplayHomeAsUpEnabled(shouldBeVisible);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
