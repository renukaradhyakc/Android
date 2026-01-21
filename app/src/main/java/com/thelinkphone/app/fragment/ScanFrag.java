package com.thelinkphone.app.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.model.QRResponse;
import com.thelinkphone.app.model.QrRequest;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.SimUtils;
import com.google.zxing.Result;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ScanFrag extends BaseFragment {

    private CodeScanner mCodeScanner;
    CodeScannerView scannerView;

    private static final String BASE_URL = "https://app.thelinkphone.com/api/";

    public ScanFrag() {
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
        final Activity activity = getActivity();
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
         scannerView = view.findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(activity, scannerView);

        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                     //   Toast.makeText(activity, result.getText(), Toast.LENGTH_SHORT).show();
                        GetPhoneNumber(result.getText());
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
        return view;
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
            return;
        }

        if (domain == null || domain.isEmpty()) {
            Toast.makeText(getContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        QrRequest qrRequest = new QrRequest(domain);

        apiService.scanQr(qrRequest).enqueue(new Callback<QRResponse>() {
            @Override
            public void onResponse(Call<QRResponse> call, Response<QRResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String phoneNumber = response.body().getPhoneNumber();
                //    Toast.makeText(getContext(), "Phone Number: " + phoneNumber, Toast.LENGTH_LONG).show();
                    Callnumber(phoneNumber);
                } else {
                    Toast.makeText(getContext(), "User does not exist", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<QRResponse> call, Throwable t) {
                Toast.makeText(getContext(), "API call failed", Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", t.getMessage());
            }
        });


    }

    private void Callnumber(String phoneNumber) {
        // Add null checks and proper error handling
        if (getContext() == null || getActivity() == null) {
            Log.e("ScanFrag", "Context or Activity is null, cannot make call");
            return;
        }
        
        ArrayList<ItemSimInfo> arrSim = SimUtils.getAvailableSIMCardLabels(getContext());
        int posSim = MyShare.getPosSim(getContext());
        
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "Invalid phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (arrSim.size() == 0) {
            Toast.makeText(getContext(), "No SIM card available", Toast.LENGTH_SHORT).show();
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
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    OtherUtils.call(getContext(), finalPhoneNumber, finalPhoneAccountHandle);
                } catch (Exception e) {
                    Log.e("ScanFrag", "Error making call: " + e.getMessage());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to make call", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, 500); // 500ms delay
    }

    public void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    public void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }
}