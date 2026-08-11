package com.thelinkphone.app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.thelinkphone.app.custom.ViewFragmentUnifiedSchedule;

public class FragmentUnifiedSchedule extends Fragment {

    private ViewFragmentUnifiedSchedule view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.view == null) {
            this.view = new ViewFragmentUnifiedSchedule(inflater.getContext());
        }
        return this.view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (view != null) {
            view.loadUnifiedSchedules();
            view.resetSearches();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}