package com.thelinkphone.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.CallBlockActivity;
import com.thelinkphone.app.ActivityPaywall;
import com.thelinkphone.app.PrivacyPolicyActivity;
import com.thelinkphone.app.R;
import com.thelinkphone.app.ViewMyQRAct;
import com.thelinkphone.app.dialog.CallsDialogFragment;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.SpamProtectionManager;


public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";
    ConstraintLayout mCallsBtn, mQRBtn, mPrivacyBtn, mRecordBtn, mBlockBtn, mProBtn, mManageSubBtn;
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
        mProBtn = view.findViewById(R.id.Settings_PRO);
        mManageSubBtn = view.findViewById(R.id.Settings_ManageSubscription);

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

        mProBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toPaywallAct = new Intent(getContext(), ActivityPaywall.class);
                toPaywallAct.putExtra("ENTRY_SOURCE", "SETTINGS");
                startActivity(toPaywallAct);
            }
        });

        mManageSubBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences accessPrefs = getContext().getSharedPreferences("paywall_subscription_cache", Context.MODE_PRIVATE);
                String sku = accessPrefs.getString("subscription_sku", null);
                StringBuilder urlBuilder = new StringBuilder("https://play.google.com/store/account/subscriptions?package=")
                        .append(getContext().getPackageName());
                if (sku != null) {
                    urlBuilder.append("&sku=").append(sku);
                }
                String url = urlBuilder.toString();
                Intent toManageSub = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                toManageSub.setPackage("com.android.vending");

                try {
                    startActivity(toManageSub);
                } catch (android.content.ActivityNotFoundException e) {
                    // Fallback: Play Store app not installed/available — open in browser without setPackage
                    Intent fallback = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                    startActivity(fallback);
                }
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
