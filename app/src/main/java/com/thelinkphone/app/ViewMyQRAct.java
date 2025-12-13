package com.thelinkphone.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import chtgupta.qrutils.qrview.ErrorCorrection;
import chtgupta.qrutils.qrview.QRParams;
import chtgupta.qrutils.qrview.QRView;
import chtgupta.qrutils.qrview.QRViewListener;

public class ViewMyQRAct extends AppCompatActivity {
    private static final String TAG = "ViewMyQRAct";
    QRView qrView;
    MaterialButton shareButton;
    MaterialButton shareLinkButton;
    Bitmap qrBitmap;
    android.widget.TextView descriptionText;
    android.widget.ProgressBar progressBar;

    String mDomainQR;
    String mPhoneNumber;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_NAME = "app_prefs";
    private static final String DOMAIN_KEY = "auth_domain";
    private static final String PHONE_KEY = "auth_phone";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_my_qract);

        // Setup toolbar
        setupToolbar();

        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        getDomain();

        qrView = findViewById(R.id.qrView);
        shareButton = findViewById(R.id.share_button);
        shareLinkButton = findViewById(R.id.share_link_button);
        descriptionText = findViewById(R.id.description);
        progressBar = findViewById(R.id.progressBar);

        setupShareButton();
        setupShareLinkButton();

        if(mDomainQR != null && !mDomainQR.isEmpty())
        {
            Log.d(TAG, "Generating QR code for domain: " + mDomainQR);

            // Set initial state
            shareButton.setEnabled(false);
            shareButton.setText("GENERATING QR CODE...");
            descriptionText.setText("Generating your QR code...");
            progressBar.setVisibility(android.view.View.VISIBLE);

            // Build QR code
            qrView.setData(mDomainQR)
                    .setSize(280, QRParams.DP)
                    .setErrorCorrectionLevel(ErrorCorrection.H)
                    .setQRForegroundColor(Color.BLACK)
                    .setQRBackgroundColor(Color.WHITE)
                    .addListener(new QRViewListener() {
                        @Override
                        public void onQRInitiated() {
                            Log.d(TAG, "QR generation initiated");
                        }

                        @Override
                        public void onQRGenerating() {
                            Log.d(TAG, "QR generating...");
                        }

                        @Override
                        public void onQRGenerated(Bitmap qrBitmap) {
                            Log.d(TAG, "QR code generated successfully");
                            Log.d(TAG, "QR bitmap dimensions: " + (qrBitmap != null ? qrBitmap.getWidth() + "x" + qrBitmap.getHeight() : "null"));
                            ViewMyQRAct.this.qrBitmap = qrBitmap;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    shareButton.setEnabled(true);
                                    shareButton.setText("SHARE QR CODE");
                                    descriptionText.setText("Share this QR code with others to allow them to call you using the CallALink app");
                                    progressBar.setVisibility(android.view.View.GONE);
                                }
                            });
                        }

                        @Override
                        public void onError() {
                            Log.e(TAG, "Error generating QR code");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                             //       Toast.makeText(ViewMyQRAct.this, "Failed to generate QR code", Toast.LENGTH_LONG).show();
                                    shareButton.setEnabled(false);
                                    shareButton.setText("QR CODE UNAVAILABLE");
                                    descriptionText.setText("Failed to generate QR code. Please try again later.");
                                    progressBar.setVisibility(android.view.View.GONE);
                                }
                            });
                        }
                    })
                    .build();

            // Fallback: Enable share button after 3 seconds if callback doesn't work
            qrView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (shareButton.getText().equals("GENERATING QR CODE...")) {
                        Log.d(TAG, "QR generation callback didn't fire, enabling button anyway");
                        shareButton.setEnabled(true);
                        shareButton.setText("SHARE QR CODE");
                        descriptionText.setText("Share this QR code with others to allow them to call you using the CallALink app");
                        progressBar.setVisibility(android.view.View.GONE);
                        // Try to get bitmap from QRView directly
                        qrView.buildDrawingCache();
                        if (qrView.getDrawingCache() != null) {
                            qrBitmap = Bitmap.createBitmap(qrView.getDrawingCache());
                        }
                    }
                }
            }, 3000);

        } else {
            Log.w(TAG, "Domain data not available for QR generation");
            Toast.makeText(this, "QR code data not available. Please login first.", Toast.LENGTH_LONG).show();
            shareButton.setEnabled(false);
            shareButton.setText("LOGIN REQUIRED");
            descriptionText.setText("Please login first to generate your QR code.");
            progressBar.setVisibility(android.view.View.GONE);
        }


    }

    private void getDomain()
    {
        mDomainQR = sharedPreferences.getString(DOMAIN_KEY, null);
        mPhoneNumber = sharedPreferences.getString(PHONE_KEY, null);
        Log.d(TAG, "Domain QR: " + mDomainQR);
        Log.d(TAG, "Phone Number: " + (mPhoneNumber != null ? mPhoneNumber.substring(0, Math.min(3, mPhoneNumber.length())) + "***" : "null"));
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("My QR Code");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupShareButton() {
        shareButton.setEnabled(false); // Disable until QR is generated
        shareButton.setText("LOADING...");
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareQRCode();
            }
        });
    }

    private void shareQRCode() {
        Log.d(TAG, "Share QR code requested");

        // Try multiple methods to get the QR bitmap
        Bitmap bitmapToShare = getBestQRBitmap();

        if (bitmapToShare != null) {
            Log.d(TAG, "QR bitmap available: " + bitmapToShare.getWidth() + "x" + bitmapToShare.getHeight());
            shareQRBitmap(bitmapToShare);
        } else {
            Log.w(TAG, "Unable to get QR bitmap for sharing");
            Toast.makeText(this, "QR code not available for sharing. Please wait for QR code to generate.", Toast.LENGTH_LONG).show();
            
            // Try to regenerate QR code if bitmap is null
            if (mDomainQR != null && !mDomainQR.isEmpty()) {
                Log.d(TAG, "Attempting to regenerate QR code for sharing");
                regenerateQRForSharing();
            }
        }
    }

    private Bitmap getBestQRBitmap() {
        // Method 1: Use the bitmap from callback
        if (qrBitmap != null && !qrBitmap.isRecycled()) {
            Log.d(TAG, "Using QR bitmap from callback");
            return qrBitmap;
        }

        // Method 2: Capture from QRView
        Log.d(TAG, "QR bitmap from callback is null, trying to capture from view");
        Bitmap capturedBitmap = captureQRView();
        if (capturedBitmap != null) {
            Log.d(TAG, "Successfully captured QR bitmap from view");
            return capturedBitmap;
        }

        // Method 3: Try alternative capture method
        Log.d(TAG, "Standard capture failed, trying alternative method");
        return captureQRViewAlternative();
    }

    private void shareQRBitmap(Bitmap bitmap) {
        try {
            Log.d(TAG, "Preparing to share QR bitmap");
            
            // Create cache directory
            File cachePath = new File(getCacheDir(), "images");
            if (!cachePath.exists()) {
                boolean created = cachePath.mkdirs();
                Log.d(TAG, "Cache directory created: " + created);
            }
            
            // Create unique filename with timestamp
            String fileName = "qr_code_" + System.currentTimeMillis() + ".png";
            File qrFile = new File(cachePath, fileName);
            
            // Save bitmap to file
            FileOutputStream stream = new FileOutputStream(qrFile);
            boolean compressed = bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();
            
            Log.d(TAG, "QR image compressed: " + compressed);
            Log.d(TAG, "QR image saved to: " + qrFile.getAbsolutePath());
            Log.d(TAG, "File size: " + qrFile.length() + " bytes");

            // Create content URI
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", qrFile);
            Log.d(TAG, "FileProvider URI: " + contentUri.toString());

            if (contentUri != null) {
                // Create share intent
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My LinkPhone QR Code");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Scan this QR code to call me using LinkPhone app!\n\n📱 LinkPhone - Easy calling with QR codes");
                shareIntent.setType("image/png");

                Log.d(TAG, "Starting share intent");
                startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
            } else {
                Log.e(TAG, "Failed to create content URI");
                Toast.makeText(this, "Failed to prepare QR code for sharing", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sharing QR code: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Unable to share QR code image: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void regenerateQRForSharing() {
        Log.d(TAG, "Regenerating QR code for sharing");
        
        // Show progress
        shareButton.setEnabled(false);
        shareButton.setText("GENERATING...");
        
        // Force regenerate QR code
        qrView.setData(mDomainQR)
                .setSize(280, QRParams.DP)
                .setErrorCorrectionLevel(ErrorCorrection.H)
                .setQRForegroundColor(Color.BLACK)
                .setQRBackgroundColor(Color.WHITE)
                .addListener(new QRViewListener() {
                    @Override
                    public void onQRInitiated() {
                        Log.d(TAG, "QR regeneration initiated");
                    }

                    @Override
                    public void onQRGenerating() {
                        Log.d(TAG, "QR regenerating...");
                    }

                    @Override
                    public void onQRGenerated(Bitmap qrBitmap) {
                        Log.d(TAG, "QR code regenerated successfully for sharing");
                        ViewMyQRAct.this.qrBitmap = qrBitmap;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                shareButton.setEnabled(true);
                                shareButton.setText("SHARE QR CODE");
                                // Automatically share after regeneration
                                shareQRBitmap(qrBitmap);
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        Log.e(TAG, "Error regenerating QR code");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                shareButton.setEnabled(true);
                                shareButton.setText("SHARE QR CODE");
                                Toast.makeText(ViewMyQRAct.this, "Failed to generate QR code for sharing", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                })
                .build();
    }

    private void setupShareLinkButton() {
        shareLinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareLinkPhoneLink();
            }
        });
    }

    private void shareLinkPhoneLink() {
        Log.d(TAG, "Share LinkPhone link requested");
        
        if (mDomainQR == null || mDomainQR.isEmpty()) {
            Toast.makeText(this, "Please login first to share your LinkPhone link", Toast.LENGTH_LONG).show();
            return;
        }

        // Create a clickable HTTPS URL using the existing app.thelinkphone.com domain
        String clickableLinkUrl = "https://app.callalink.com/call/" + mDomainQR;
        
        // Also create the direct app link for manual copying
        String directAppLink = "callalink://call?user=" + mDomainQR;
        
        Log.d(TAG, "Generated clickable LinkPhone URL: " + clickableLinkUrl);
        Log.d(TAG, "Generated direct app link: " + directAppLink);

        // Create share intent
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Call me using LinkPhone");
        
        String shareText = "Hi! You can call me directly using LinkPhone app.\n\n" +
                "🔗 Click this link to call me: " + clickableLinkUrl + "\n\n" +
                "If the link doesn't work, copy and paste this in your browser: " + directAppLink + "\n\n" +
                "Make sure you have LinkPhone app installed first!\n\n" +
                "📱 LinkPhone - Easy calling with QR codes and links!";
        
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        Log.d(TAG, "Starting share intent for LinkPhone link");
        startActivity(Intent.createChooser(shareIntent, "Share LinkPhone Link"));
    }

    private Bitmap captureQRView() {
        try {
            Log.d(TAG, "Attempting to capture QR view as bitmap");
            qrView.setDrawingCacheEnabled(true);
            qrView.buildDrawingCache(true);
            Bitmap bitmap = qrView.getDrawingCache();
            if (bitmap != null) {
                // Create a copy of the bitmap
                Bitmap capturedBitmap = Bitmap.createBitmap(bitmap);
                qrView.setDrawingCacheEnabled(false);
                Log.d(TAG, "Successfully captured QR view bitmap");
                return capturedBitmap;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error capturing QR view: " + e.getMessage());
        }
        return null;
    }

    private Bitmap captureQRViewAlternative() {
        try {
            Log.d(TAG, "Attempting alternative QR view capture method");
            
            // Method 1: Try to get bitmap directly from view
            if (qrView.getWidth() > 0 && qrView.getHeight() > 0) {
                Bitmap bitmap = Bitmap.createBitmap(qrView.getWidth(), qrView.getHeight(), Bitmap.Config.ARGB_8888);
                android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                qrView.draw(canvas);
                
                if (bitmap != null && !bitmap.isRecycled()) {
                    Log.d(TAG, "Alternative capture method successful");
                    return bitmap;
                }
            }
            
            // Method 2: Force measure and layout if view dimensions are 0
            if (qrView.getWidth() == 0 || qrView.getHeight() == 0) {
                Log.d(TAG, "QR view has zero dimensions, forcing measure");
                int measureSpec = View.MeasureSpec.makeMeasureSpec(280 * 3, View.MeasureSpec.EXACTLY); // 280dp * density
                qrView.measure(measureSpec, measureSpec);
                qrView.layout(0, 0, qrView.getMeasuredWidth(), qrView.getMeasuredHeight());
                
                if (qrView.getMeasuredWidth() > 0 && qrView.getMeasuredHeight() > 0) {
                    Bitmap bitmap = Bitmap.createBitmap(qrView.getMeasuredWidth(), qrView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    qrView.draw(canvas);
                    
                    if (bitmap != null && !bitmap.isRecycled()) {
                        Log.d(TAG, "Alternative capture with forced measure successful");
                        return bitmap;
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error in alternative QR view capture: " + e.getMessage());
            e.printStackTrace();
        }
        
        Log.w(TAG, "All alternative capture methods failed");
        return null;
    }
}
