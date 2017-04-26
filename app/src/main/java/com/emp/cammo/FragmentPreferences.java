package com.emp.cammo;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FragmentPreferences extends Fragment implements TextView.OnEditorActionListener {
    // widgets
    private EditText mEditTextIpAddress = null;
    private EditText mEditTextPortNumber = null;

    public static FragmentPreferences newInstance() {
        return new FragmentPreferences();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_preferences, container, false);

//        // set layout orientation to match configuration
//        LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout_preferences);
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            layout.setOrientation(LinearLayout.HORIZONTAL);
//        } else {
//            layout.setOrientation(LinearLayout.VERTICAL);
//        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
        if (null != activity) {
            // get widgets from layout
            mEditTextIpAddress = (EditText) view.findViewById(R.id.editText_ipAddress);
            mEditTextPortNumber = (EditText) view.findViewById(R.id.editText_portNumber);

            // restore widget text
            mEditTextIpAddress.setText(activity.mUserPreferences.mIpAddress);
            mEditTextPortNumber.setText(activity.mUserPreferences.mPortNumber);

            // set widget listeners
            mEditTextIpAddress.setOnEditorActionListener(this);
            mEditTextPortNumber.setOnEditorActionListener(this);
        } else {
            mEditTextIpAddress = null;
            mEditTextPortNumber = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            MainActivity activity  = (MainActivity) getActivity();
            activity.mUserPreferences.mIpAddress = mEditTextIpAddress.getText().toString();
            activity.mUserPreferences.mPortNumber = mEditTextPortNumber.getText().toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
        boolean handledEvent = false;

        if (EditorInfo.IME_ACTION_DONE == actionId) {
            hideKeyboard(view);
            handledEvent = true;
        }

        MainActivity activity = (MainActivity) getActivity();
        if (null == activity) return handledEvent;

        switch (view.getId()) {
            case R.id.editText_ipAddress:
                activity.mUserPreferences.mIpAddress = view.getText().toString();
                handledEvent = true;
                break;
            case R.id.editText_portNumber:
                activity.mUserPreferences.mPortNumber = view.getText().toString();
                handledEvent = true;
                break;
            default:
                break;
        }

        return handledEvent;
    }

    public void hideKeyboard(View done) {
        try {
            Activity activity = getActivity();

            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(done.getWindowToken(), 0);
            View gets = activity.findViewById(R.id.layout_preferences);

            gets.requestFocus();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
