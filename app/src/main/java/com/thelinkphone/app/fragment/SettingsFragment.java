package com.thelinkphone.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.CallBlockActivity;
import com.thelinkphone.app.LoginActivity;
import com.thelinkphone.app.PrivacyPolicyActivity;
import com.thelinkphone.app.R;
import com.thelinkphone.app.ViewMyQRAct;
import com.thelinkphone.app.dialog.CallsDialogFragment;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.SpamProtectionManager;


public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    ConstraintLayout mCallsBtn, mQRBtn, mPrivacyBtn, mRecordBtn, mBlockBtn;
    private SpamProtectionManager spamProtectionManager;

    public SettingsFragment() {
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
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mCallsBtn = view.findViewById(R.id.Settings_Calls);
        mQRBtn = view.findViewById(R.id.Settings_QR);
        mPrivacyBtn = view.findViewById(R.id.Settings_Privacy);
        mBlockBtn = view.findViewById(R.id.Settings_Block);
        mRecordBtn = view.findViewById(R.id.Settings_Recording);

        // Initialize spam protection manager
        spamProtectionManager = new SpamProtectionManager(getContext());

        mCallsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log current call setting before showing dialog
                String currentSetting = MyShare.getCallSettingName(getContext());
                Log.d(TAG, "Current call setting: " + currentSetting);

                // Show privacy protection status in background to avoid ANR
                if (spamProtectionManager != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                spamProtectionManager.showProtectionStatus();
                            } catch (Exception e) {
                                Log.e(TAG, "Error showing protection status: " + e.getMessage());
                            }
                        }
                    }).start();
                }

                CallsDialogFragment callsDialogFragment = new CallsDialogFragment();
                callsDialogFragment.show(getActivity().getSupportFragmentManager(),"My  Fragment");
            }
        });

        mBlockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toCallBlockActivity = new Intent(getContext(), CallBlockActivity.class);
                startActivity(toCallBlockActivity);
            }
        });

        mPrivacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toPrivacyPolicyAct = new Intent(getContext(), PrivacyPolicyActivity.class);
                startActivity(toPrivacyPolicyAct);
            }
        });

        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // showFragment(new FragmentBlock());
                ((ActivityHome) getActivity()).showFragment(new FragmentRecorder(), true);
                /*((ActivityHome) getActivity()).showFragment(new FragmentBlock(), true);*/
            }
        });

        mQRBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toQRAct = new Intent(getContext(), ViewMyQRAct.class);
                startActivity(toQRAct);
            }
        });

       /* mThemeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ActivityHome) getActivity()).onChangeTheme();
            }
        });*/

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Log call setting status when fragment resumes
        if (getContext() != null) {
            String currentSetting = MyShare.getCallSettingName(getContext());
            Log.d(TAG, "SettingsFragment resumed - Call setting: " + currentSetting);

            // Move heavy operations to background thread to avoid ANR
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (spamProtectionManager != null) {
                            spamProtectionManager.applyProtectionBasedOnSettings();

                            // Show user-friendly message about privacy status
                            boolean isPhonelinkScheduled = MyShare.isCallSettingPhonelinkScheduled(getContext());
                            boolean isDefaultDialer = spamProtectionManager.isDefaultDialer();

                            if (isDefaultDialer && isPhonelinkScheduled) {
                                Log.d(TAG, "Privacy protection: ACTIVE - TrueCaller and spam apps blocked");
                            } else if (isDefaultDialer && !isPhonelinkScheduled) {
                                Log.d(TAG, "Privacy protection: INACTIVE - Spam apps allowed (Unrestricted mode)");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in background privacy protection check: " + e.getMessage());
                    }
                }
            }).start();
        }
    }
}
