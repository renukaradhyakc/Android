package com.thelinkphone.app.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrialStartDialog extends Dialog {
    private MaterialButton btnStartTrial, btnMaybeLater;
    private TextView tvTitle, tvMessage;
    private ApiService apiService;
    private String email;
    private TrialDialogListener listener;

    public interface TrialDialogListener {
        void onTrialStarted();
        void onMaybeLater();
    }

    public TrialStartDialog(@NonNull Context context, String email, TrialDialogListener listener) {
        super(context);
        this.email = email;
        this.listener = listener;
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_trial_info);

        setCanceledOnTouchOutside(false);
        setCancelable(false);

        tvTitle = findViewById(R.id.tvTrialTitle);
        tvMessage = findViewById(R.id.tvTrialMessage);
        btnStartTrial = findViewById(R.id.btnStartTrial);
        btnMaybeLater = findViewById(R.id.btnMaybeLater);

        tvTitle.setText("14-Day Free Trial");
        tvMessage.setText("Try all premium features completely free for 14 days. No credit card required!");

        btnStartTrial.setOnClickListener(v -> startTrial());
        btnMaybeLater.setOnClickListener(v -> {
            dismiss();
            if (listener != null) {
                listener.onMaybeLater();
            }
        });
    }

    private void startTrial() {
        btnStartTrial.setEnabled(false);
        btnStartTrial.setText("Starting Trial...");

        Call<Object> call = apiService.startTrial(email);
        call.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    dismiss();
                    if (listener != null) {
                        listener.onTrialStarted();
                    }
                } else {
                    btnStartTrial.setEnabled(true);
                    btnStartTrial.setText("Start Free Trial");
                    Toast.makeText(getContext(), "Trial already used or error occurred", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                btnStartTrial.setEnabled(true);
                btnStartTrial.setText("Start Free Trial");
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
