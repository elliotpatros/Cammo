package com.emp.cammo;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.main_activity_layout, FragmentMainMenu.newInstance()).commit();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.goto_calibration:
                Log.i(TAG, "onClick: GOTO CALIBRATION");
                break;
            case R.id.goto_tracking:
                Log.i(TAG, "onClick: GOTO TRACKING");
                break;
            default: break;
        }
    }
}
