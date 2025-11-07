package com.thelinkphone.app.dialog;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.SpamProtectionManager;


public class CallsDialogFragment extends androidx.fragment.app.DialogFragment {

    private RadioGroup radioGroup;
    private RadioButton radioUnrestricted;
    private RadioButton radioPhonelinkScheduled;
    private MaterialButton saveButton;
    private SpamProtectionManager spamProtectionManager;

    public CallsDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.callssettings_layout, container, false);

        initViews(view);
        loadCurrentSettings();
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        radioGroup = view.findViewById(R.id.radioGroup);
        radioUnrestricted = view.findViewById(R.id.radioButton1);
        radioPhonelinkScheduled = view.findViewById(R.id.radioButton2);
        saveButton = view.findViewById(R.id.button);

        // Initialize spam protection manager
        spamProtectionManager = new SpamProtectionManager(getContext());
    }

    private void loadCurrentSettings() {
        if (getContext() != null) {
            int currentSetting = MyShare.getCallSetting(getContext());
            if (currentSetting == MyShare.CALL_SETTING_UNRESTRICTED) {
                radioUnrestricted.setChecked(true);
            } else {
                radioPhonelinkScheduled.setChecked(true);
            }
        }
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }

    private void saveSettings() {
        if (getContext() != null) {
            int selectedSetting;
            if (radioUnrestricted.isChecked()) {
                selectedSetting = MyShare.CALL_SETTING_UNRESTRICTED;
            } else {
                selectedSetting = MyShare.CALL_SETTING_PHONELINK_SCHEDULED;
            }

            MyShare.putCallSetting(getContext(), selectedSetting);

            // Apply privacy protection based on new setting
            if (spamProtectionManager != null) {
                spamProtectionManager.applyProtectionBasedOnSettings();
            }

            String message;
            if (selectedSetting == MyShare.CALL_SETTING_UNRESTRICTED) {
                message = "Call setting changed to Unrestricted - All calls allowed, TrueCaller/spam apps can show popups";
            } else {
                message = "Call setting changed to Phonelink Scheduled - Privacy protection enabled, spam apps blocked";
            }

            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            dismiss();
        }
    }
}
