package com.emp.cammo;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class FragmentMainMenu extends Fragment {

    public static FragmentMainMenu newInstance() {
        return new FragmentMainMenu();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);

        // set layout orientation to match configuration
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout_main_menu);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            layout.setOrientation(LinearLayout.VERTICAL);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        if (null != activity) {
            view.findViewById(R.id.btn_goto_calibration).setOnClickListener(activity);
            view.findViewById(R.id.btn_goto_tracking).setOnClickListener(activity);
            view.findViewById(R.id.btn_goto_preferences).setOnClickListener(activity);
        }
    }
}
