package com.thelinkphone.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thelinkphone.app.utils.BillStatusPoller;

public class BillProcessingActivity extends AppCompatActivity {

    private BillStatusPoller billStatusPoller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("BillFlow", "BillProcessingActivity opened");
        setContentView(R.layout.activity_bill_processing);

        long billId = getIntent().getLongExtra("bill_id", 0);
        String token = getIntent().getStringExtra("token");

        TextView txtBill = findViewById(R.id.txtBill);
        txtBill.setText("Receipt Uploaded ✓\n\n" + "Bill #" + billId + "\n\n" + "Status: uploaded\n\n" + "Please wait");

        billStatusPoller = new BillStatusPoller();

        billStatusPoller.start(billId, "Bearer " + token, new BillStatusPoller.Listener() {

            @Override
            public void onStatusChanged(String status) {

                runOnUiThread(() -> {
                    txtBill.setText("Receipt Uploaded ✓\n\n" + "Bill #" + billId + "\n\n" + "Status: " + status + "\n\n" + "Please wait");
                });
            }

            @Override
            public void onCompleted() {

                runOnUiThread(() -> {

                    txtBill.setText("Receipt Uploaded ✓\n\n" + "Bill #" + billId + "\n\n" + "Status: Done ✓");
                    View overlay = findViewById(R.id.successOverlay);
                    TextView points = findViewById(R.id.txtPoints);
                    com.airbnb.lottie.LottieAnimationView anim =
                            findViewById(R.id.lottieSuccess);

                    overlay.setVisibility(View.VISIBLE);
                    anim.playAnimation();

                    points.setText("+410 Points Earned");

                    overlay.animate()
                            .alpha(1f)
                            .setDuration(200)
                            .start();

                    new android.os.Handler().postDelayed(() -> {
                        openLoyaltyWebView(billId);
                    }, 1600);
                });
            }

            @Override
            public void onFailed(String status) {

                runOnUiThread(() -> {

                    txtBill.setText("Receipt Uploaded ✓\n\n" + "Bill #" + billId + "\n\n" + "Status: Failed");
                    Toast.makeText(BillProcessingActivity.this, "Bill processing failed", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (billStatusPoller != null) {
            billStatusPoller.stop();
        }
    }

    private void openLoyaltyWebView(long billId) {
        Intent intent = new Intent(this, ActivityHome.class);
        intent.putExtra("OPEN_BILL", true);
        intent.putExtra("BILL_ID", billId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }
}