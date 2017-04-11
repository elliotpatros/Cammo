package com.emp.cammo;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentMainMenu extends Fragment {
    // fragment settings
    public static final int SCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    public static final boolean HOME_ARROW = false;
    public static final int THEME = R.style.AppTheme;

    public static FragmentMainMenu newInstance() {
        return new FragmentMainMenu();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) return;

        view.findViewById(R.id.btn_goto_calibration).setOnClickListener(activity);
        view.findViewById(R.id.btn_goto_tracking).setOnClickListener(activity);
    }
}
