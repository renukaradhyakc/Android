package com.thelinkphone.app.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.custom.ViewCopyShareIcons;
import com.thelinkphone.app.utils.OtherUtils;

import java.io.File;
import java.io.FileOutputStream;

import chtgupta.qrutils.qrview.ErrorCorrection;
import chtgupta.qrutils.qrview.QRParams;
import chtgupta.qrutils.qrview.QRView;
import chtgupta.qrutils.qrview.QRViewListener;

public class DialogShowQr extends BaseDialog {
    private final String link;
    private final String displayName;
    private final boolean theme;
    private Bitmap qrBitmap;

    public DialogShowQr(Context context, String link, String displayName, boolean theme) {
        super(context);
        this.link = link;
        this.displayName = displayName;
        this.theme = theme;
        setCancelable(true);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        int i = widthScreen / 25;

        LinearLayout outer = new LinearLayout(getContext());
        outer.setGravity(Gravity.CENTER);

        CardView cardView = new CardView(getContext());
        cardView.setRadius(i);
        cardView.setCardElevation(i * 6f);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams((int) (widthScreen * 0.8f), -2);
        int margin = (widthScreen * 3) / 30;
        cardParams.setMargins(margin, margin, margin, margin);
        outer.addView(cardView, cardParams);

        LinearLayout content = new LinearLayout(getContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_HORIZONTAL);
        content.setPadding(i, i, i, i);
        cardView.addView(content, -1, -2);

        TextW title = new TextW(getContext());
        title.setText("CallALink QR Code");
        title.setupText(700, 5.0f);
        title.setPadding(0, 0, 0, i);
        content.addView(title, -2, -2);

        QRView qrView = new QRView(getContext());
        int qrSizeDp = 200;
        int qrSizePx = dpToPx(qrSizeDp);
        content.addView(qrView, qrSizePx, qrSizePx);

        qrView.setData(link)
                .setSize(qrSizeDp, QRParams.DP)
                .setErrorCorrectionLevel(ErrorCorrection.H)
                .setQRForegroundColor(Color.BLACK)
                .setQRBackgroundColor(Color.WHITE)
                .addListener(new QRViewListener() {
                    @Override public void onQRInitiated() {}
                    @Override public void onQRGenerating() {}
                    @Override public void onQRGenerated(Bitmap bitmap) { qrBitmap = bitmap; }
                    @Override public void onError() {}
                })
                .build();

        TextW tvDescription = new TextW(getContext());
        tvDescription.setText(!TextUtils.isEmpty(displayName)
                ? "Scan this QR code to call " + displayName + " using the CallALink app"
                : "Scan this QR code with the CallALink app to make a call");
        tvDescription.setupText(400, 3.2f);
        tvDescription.setTextColor(Color.parseColor("#8A8A8E"));
        tvDescription.setGravity(Gravity.CENTER);
        tvDescription.setPadding(0, i, 0, i / 2);
        content.addView(tvDescription, -2, -2);

        LinearLayout buttonRow = new LinearLayout(getContext());
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-2, -2);
        rowParams.topMargin = i;
        content.addView(buttonRow, rowParams);

        int strokeColor = theme ? Color.parseColor("#D1D1D6") : Color.parseColor("#48484A");
        int textColor = theme ? Color.BLACK : Color.WHITE;
        int strokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                getContext().getResources().getDisplayMetrics());
        int cornerRadius = dpToPx(24);
        int padH = dpToPx(20);
        int padV = dpToPx(10);

        buttonRow.addView(makePillButton("Download", R.drawable.ic_download, strokeColor, textColor,
                strokeWidth, cornerRadius, padH, padV,
                v -> downloadQrToGallery()));

        LinearLayout.LayoutParams spacer = new LinearLayout.LayoutParams(dpToPx(12), 1);
        buttonRow.addView(new View(getContext()), spacer);

        buttonRow.addView(makePillButton("Share", R.drawable.ic_share, strokeColor, textColor,
                strokeWidth, cornerRadius, padH, padV,
                v -> shareQrBitmap()));

        setContentView(outer);

        if (theme) {
            cardView.setCardBackgroundColor(-1);
            title.setTextColor(-16777216);
        } else {
            cardView.setCardBackgroundColor(Color.parseColor("#424141"));
            title.setTextColor(-1);
        }
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    private void shareQrBitmap() {
        if (qrBitmap == null || qrBitmap.isRecycled()) {
            Toast.makeText(getContext(), "QR code not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File cachePath = new File(getContext().getCacheDir(), "images");
            if (!cachePath.exists()) cachePath.mkdirs();
            File qrFile = new File(cachePath, "callalink_qr_" + System.currentTimeMillis() + ".png");
            FileOutputStream stream = new FileOutputStream(qrFile);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", qrFile);

            String subject = !TextUtils.isEmpty(displayName)
                    ? displayName + "'s CallALink QR Code"
                    : "CallALink QR Code";

            String shareText = !TextUtils.isEmpty(displayName)
                    ? "Scan this QR code to call " + displayName + " using the CallALink app!\n\n" +
                    "📱 CallALink - Easy calling with QR codes"
                    : "Scan this QR code to make a call using the CallALink app!\n\n" +
                    "📱 CallALink - Easy calling with QR codes";

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setType("image/png");
            getContext().startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
        } catch (Exception e) {
            Toast.makeText(getContext(), "Unable to share QR Code", Toast.LENGTH_LONG).show();
        }
    }

    private LinearLayout makePillButton(String text, int iconRes, int strokeColor, int textColor,
                                        int strokeWidth, int cornerRadius, int padH, int padV,
                                        View.OnClickListener onClick) {
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.TRANSPARENT);
        bg.setStroke(strokeWidth, strokeColor);
        bg.setCornerRadius(cornerRadius);

        LinearLayout button = new LinearLayout(getContext());
        button.setOrientation(LinearLayout.HORIZONTAL);
        button.setGravity(Gravity.CENTER);
        button.setBackground(bg);
        button.setPadding(padH, padV, padH, padV);
        button.setClickable(true);
        button.setFocusable(true);
        button.setOnClickListener(onClick);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(textColor, android.graphics.PorterDuff.Mode.SRC_IN);
        int iconSize = dpToPx(18);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconParams.setMarginEnd(dpToPx(6));
        button.addView(icon, iconParams);

        TextW label = new TextW(getContext());
        label.setText(text);
        label.setupText(500, 3.4f);
        label.setTextColor(textColor);
        button.addView(label, -2, -2);

        return button;
    }

    private void downloadQrToGallery() {
        if (qrBitmap == null || qrBitmap.isRecycled()) {
            android.widget.Toast.makeText(getContext(), "QR code not ready yet", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        String fileName = "CallALink_QR_" + System.currentTimeMillis() + ".png";
        android.content.ContentResolver resolver = getContext().getContentResolver();
        android.content.ContentValues values = new android.content.ContentValues();
        values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png");

        Uri imageUri;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH,
                    android.os.Environment.DIRECTORY_PICTURES + "/CallALink");
            imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            File picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES);
            File appDir = new File(picturesDir, "CallALink");
            if (!appDir.exists()) appDir.mkdirs();
            File imageFile = new File(appDir, fileName);
            values.put(android.provider.MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
            imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }

        if (imageUri == null) {
            android.widget.Toast.makeText(getContext(), "Failed to save QR code", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        try (java.io.OutputStream out = resolver.openOutputStream(imageUri)) {
            if (out != null) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                android.widget.Toast.makeText(getContext(), "Saved to gallery", android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (java.io.IOException e) {
            android.widget.Toast.makeText(getContext(), "Failed to save QR code", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}