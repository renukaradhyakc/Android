package com.thelinkphone.app.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.thelinkphone.app.model.BillStatusResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillStatusPoller {

    public interface Listener {
        void onStatusChanged(String status);
        void onCompleted(int points);
        void onFailed(String status);
    }

    private final ApiService apiService;
    private final Handler handler;

    private Runnable pollingRunnable;
    private boolean stopped = false;

    public BillStatusPoller() {
        apiService = ApiClient.getClient().create(ApiService.class);
        handler = new Handler(Looper.getMainLooper());
    }

    public void start(long billId, String token, @NonNull Listener listener) {

        stopped = false;
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (stopped) {
                    return;
                }

                apiService.getBillStatus(token, billId)
                        .enqueue(new Callback<BillStatusResponse>() {

                            @Override
                            public void onResponse(
                                    Call<BillStatusResponse> call,
                                    Response<BillStatusResponse> response
                            ) {

                                if (stopped) {
                                    return;
                                }

                                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                                    scheduleNext();
                                    return;
                                }

                                String status = response.body().getData().getStatus();
                                int points = response.body().getData().getPoints();

                                Log.d("BillStatus", "Current status = " + status);

                                listener.onStatusChanged(status);

                                if ("done".equalsIgnoreCase(status)) {
                                    stop();
                                    listener.onCompleted(points);

                                } else if ("failed".equalsIgnoreCase(status)) {
                                    stop();
                                    listener.onFailed(status);

                                } else {
                                    scheduleNext();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<BillStatusResponse> call,
                                    Throwable t
                            ) {
                                if (!stopped) {
                                    scheduleNext();
                                }
                            }
                        });
            }

            private void scheduleNext() {
                handler.postDelayed(pollingRunnable, 3000);
            }
        };
        handler.post(pollingRunnable);
    }

    public void stop() {
        stopped = true;
        if (pollingRunnable != null) {
            handler.removeCallbacks(pollingRunnable);
        }
    }
}