package com.thelinkphone.app.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.thelinkphone.app.R;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkPreviewCard extends LinearLayout {
    private ImageView ivThumbnail;
    private TextView tvTitle;
    private String url;

    public LinkPreviewCard(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setPadding(24, 16, 24, 16);
        setGravity(Gravity.CENTER_VERTICAL);
        
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor("#E8F5E9"));
        bg.setCornerRadius(16);
        setBackground(bg);
        
        ivThumbnail = new ImageView(context);
        LayoutParams imgParams = new LayoutParams(80, 80);
        imgParams.setMargins(0, 0, 16, 0);
        ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
        android.graphics.drawable.GradientDrawable imgBg = new android.graphics.drawable.GradientDrawable();
        imgBg.setColor(Color.parseColor("#C8E6C9"));
        imgBg.setCornerRadius(12);
        ivThumbnail.setBackground(imgBg);
        addView(ivThumbnail, imgParams);
        
        tvTitle = new TextView(context);
        tvTitle.setTextColor(Color.parseColor("#1B5E20"));
        tvTitle.setTextSize(14);
        tvTitle.setMaxLines(2);
        tvTitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LayoutParams txtParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        addView(tvTitle, txtParams);
        
        setOnClickListener(v -> {
            if (url != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(intent);
            }
        });
    }

    public void setLink(String link) {
        this.url = link;
        tvTitle.setText(link);
        fetchMetadata(link);
    }

    private void fetchMetadata(String urlStr) {
        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder html = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    html.append(line);
                }
                reader.close();
                
                String title = extractTitle(html.toString());
                String imageUrl = extractImage(html.toString(), urlStr);
                
                post(() -> {
                    if (title != null) tvTitle.setText(title);
                    if (imageUrl != null) {
                        Glide.with(getContext()).load(imageUrl).into(ivThumbnail);
                    } else {
                        ivThumbnail.setImageResource(R.drawable.ic_link);
                    }
                });
            } catch (Exception e) {
                post(() -> ivThumbnail.setImageResource(R.drawable.ic_link));
            }
        }).start();
    }

    private String extractTitle(String html) {
        Pattern pattern = Pattern.compile("<meta[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) return matcher.group(1);
        
        pattern = Pattern.compile("<title[^>]*>([^<]+)</title>", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(html);
        if (matcher.find()) return matcher.group(1);
        
        return null;
    }

    private String extractImage(String html, String baseUrl) {
        Pattern pattern = Pattern.compile("<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String img = matcher.group(1);
            if (img.startsWith("http")) return img;
            try {
                return new URL(new URL(baseUrl), img).toString();
            } catch (Exception e) {}
        }
        return null;
    }
}
