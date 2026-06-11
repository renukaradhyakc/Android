package com.thelinkphone.app.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;

import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.BillCaptureActivity;
import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.model.QRResponse;
import com.thelinkphone.app.model.QrRequest;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ScanConstants;
import com.thelinkphone.app.utils.SimUtils;
import com.google.zxing.Result;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ScanFrag extends BaseFragment {

    private CodeScanner mCodeScanner;
    private CodeScannerView scannerView;
    private boolean isProcessing = false;
    private int mode = ScanConstants.MODE_QR;
    private ModeSwitchListener modeSwitchListener;

    public ScanFrag() {
        // Required empty public constructor
    }

    public interface ModeSwitchListener {
        void openBillScanner();
    }

    public void setModeSwitchListener(ModeSwitchListener listener) {
        this.modeSwitchListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getInt("mode", ScanConstants.MODE_QR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        Activity activity = getActivity();
        if (activity == null) return view;

        scannerView = view.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(activity, scannerView);

        ImageButton btnSwitchMode = view.findViewById(R.id.btnSwitchMode);

        btnSwitchMode.setOnClickListener(v -> {
            if (modeSwitchListener != null) {
                modeSwitchListener.openBillScanner();
            }
        });

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {

                if (isProcessing) return;
                isProcessing = true;

                Activity act = getActivity();
                if (act == null) {
                    isProcessing = false;
                    return;
                }

                act.runOnUiThread(()-> {
                    String data = result.getText();

                    //   Toast.makeText(activity, result.getText(), Toast.LENGTH_SHORT).show();
                    //   GetPhoneNumber(result.getText());
                    if (mode == ScanConstants.MODE_BILL) {
                        openBillFlow(data);
                    } else {
                        GetPhoneNumber(data);
                    }
                });
            }
        });
        scannerView.setOnClickListener(v-> {
            if (mCodeScanner != null) {
                mCodeScanner.startPreview();
            }
        });
        return view;
    }

    private void openBillFlow(String data) {
        Activity activity = getActivity();

        if (activity == null) {
            resetProcessing();
            return;
        }

        Intent intent = new Intent(getContext(), BillCaptureActivity.class);
        intent.putExtra("QR_DATA", data);
        startActivity(intent);
    }

    private void GetPhoneNumber(String text)
    {
        String domain;

        try {
            if (text.startsWith("http")) {
                Uri uri = Uri.parse(text);
                domain = uri.getLastPathSegment(); // renukaradhyakc
            } else {
                domain = text; // fallback if raw domain QR
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
            resetProcessing();
            return;
        }

        if (domain == null || domain.isEmpty()) {
            Toast.makeText(getContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
            resetProcessing();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        QrRequest qrRequest = new QrRequest(domain);

        apiService.scanQr(qrRequest).enqueue(new Callback<QRResponse>() {
            @Override
            public void onResponse(Call<QRResponse> call, Response<QRResponse> response) {
                if (!isAdded()) {
                    resetProcessing();
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    String phoneNumber = response.body().getPhoneNumber();
                //    Toast.makeText(getContext(), "Phone Number: " + phoneNumber, Toast.LENGTH_LONG).show();
                    Callnumber(phoneNumber);
                } else {
                    Toast.makeText(getContext(), "User does not exist", Toast.LENGTH_LONG).show();
                    resetProcessing();
                }
            }

            @Override
            public void onFailure(Call<QRResponse> call, Throwable t) {
                Log.e("API_ERROR", t.getMessage() != null ? t.getMessage() : "error");
                if (getContext() != null) {Toast.makeText(getContext(), "API call failed", Toast.LENGTH_LONG).show();}
                resetProcessing();
            }
        });


    }

    private void Callnumber(String phoneNumber) {
        // Add null checks and proper error handling
        if (getContext() == null || getActivity() == null) {
            Log.e("ScanFrag", "Context or Activity is null, cannot make call");
            resetProcessing();
            return;
        }
        
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
            resetProcessing();
            return;
        }

        ArrayList<ItemSimInfo> arrSim = SimUtils.getAvailableSIMCardLabels(getContext());
        int posSim = MyShare.getPosSim(getContext());
        
        if (arrSim.size() == 0) {
            Toast.makeText(getContext(), "No SIM card available", Toast.LENGTH_SHORT).show();
            resetProcessing();
            return;
        }
        
        final PhoneAccountHandle finalPhoneAccountHandle;
        if (posSim < arrSim.size()) {
            finalPhoneAccountHandle = arrSim.get(posSim).handle;
        } else {
            finalPhoneAccountHandle = arrSim.get(0).handle;
        }
        
        final String finalPhoneNumber = phoneNumber;
        
        // Add a small delay to ensure WebView operations complete before making the call
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();

                if (activity == null) {
                    resetProcessing();
                    return;
                }

                try {
                    OtherUtils.call(getContext(), finalPhoneNumber, finalPhoneAccountHandle);
                } catch (Exception e) {
                    Log.e("ScanFrag", "Error making call: " + e.getMessage());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to make call", Toast.LENGTH_SHORT).show();
                    }
                }
                resetProcessing();
            }
        }, 500); // 500ms delay
    }

    public void onResume() {
        super.onResume();
        resetProcessing();
    }

    @Override
    public void onPause() {
        if (mCodeScanner != null) {
            mCodeScanner.releaseResources();
        }
        super.onPause();
    }

    private void resetProcessing() {
        isProcessing = false;
        if (mCodeScanner != null) {
            mCodeScanner.startPreview();
        }
    }
}