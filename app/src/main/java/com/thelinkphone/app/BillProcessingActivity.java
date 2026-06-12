package com.thelinkphone.app;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
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

        billStatusPoller = new BillStatusPoller();

        billStatusPoller.start(billId, "Bearer " + token, new BillStatusPoller.Listener() {

            @Override
            public void onStatusChanged(String status) {
                //Nothing
            }

            @Override
            public void onCompleted(int points) {
                runOnUiThread(() -> showCelebration(billId, points));
            }

            @Override
            public void onFailed(String status) {

                runOnUiThread(() -> {
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

    private void showCelebration(long billId, int points) {

        View rootLayout = findViewById(R.id.rootLayout);
        View processingContainer = findViewById(R.id.processingContainer);
        View overlay = findViewById(R.id.successOverlay);
        TextView txtPoints = findViewById(R.id.txtPoints);
        com.airbnb.lottie.LottieAnimationView anim = findViewById(R.id.lottieSuccess);

        txtPoints.setText("+" + points + " Points");

        ValueAnimator bgAnim = ValueAnimator.ofObject(
                new ArgbEvaluator(),
                Color.WHITE,
                Color.parseColor("#0F172A")
        );
        bgAnim.setDuration(300);
        bgAnim.addUpdateListener(animator ->
                rootLayout.setBackgroundColor((int) animator.getAnimatedValue())
        );
        bgAnim.start();

        new Handler(getMainLooper()).postDelayed(() ->
                        processingContainer.animate()
                                .alpha(0f)
                                .setDuration(150)
                                .withEndAction(() ->
                                processingContainer.setVisibility(View.GONE))
                                .start(),
                150
        );

        new Handler(getMainLooper()).postDelayed(() -> {
            overlay.setAlpha(0f);
            overlay.setVisibility(View.VISIBLE);
            overlay.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            anim.playAnimation();
        }, 280);

        txtPoints.setAlpha(0f);
        txtPoints.setScaleX(0.12f);
        txtPoints.setScaleY(0.12f);
        txtPoints.setTranslationY(300f);
        txtPoints.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f);
        txtPoints.setTextColor(Color.parseColor("#8A6D00"));
        txtPoints.setTypeface(null, Typeface.BOLD);

        new Handler(getMainLooper()).postDelayed(() -> {

            ValueAnimator colorAnim = ValueAnimator.ofObject(
                    new ArgbEvaluator(),
                    Color.parseColor("#8A6D00"),
                    Color.parseColor("#FFD700")
            );

            colorAnim.setDuration(1200);

            colorAnim.addUpdateListener(a ->
                    txtPoints.setTextColor((Integer) a.getAnimatedValue())
            );

            colorAnim.start();

            txtPoints.animate()
                    .translationY(-220f)
                    .alpha(1f)
                    .scaleX(1.8f)
                    .scaleY(1.8f)
                    .setDuration(1100)
                    .setInterpolator(new DecelerateInterpolator(2f))
                    .withEndAction(() -> {

                        txtPoints.animate()
                                .translationY(-280f)
                                .scaleX(1.3f)
                                .scaleY(1.3f)
                                .setDuration(500)
                                .setInterpolator(new OvershootInterpolator(2f))
                                .start();
                    })
                    .start();

        }, 1200);

        new Handler(getMainLooper()).postDelayed(() ->
                        openLoyaltyWebView(billId),
                4000
        );
    }
}