package com.thelinkphone.app.screen;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.VideoLayout;
import com.thelinkphone.app.utils.MyShare;



public class BackgroundManager {
    private VideoLayout videoLayout;

    public BackgroundManager(RelativeLayout relativeLayout) {
        Context context = relativeLayout.getContext();
        String photo = MyShare.getPhoto(context);
        if (photo.isEmpty() || photo.toLowerCase().contains(".gif") || photo.toLowerCase().contains(".jpg") || photo.toLowerCase().contains(".png")) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            relativeLayout.addView(imageView, -1, -1);
            if (!photo.isEmpty()) {
                Glide.with(context).load(photo).into(imageView);
                return;
            }
            try {
                if (ActivityCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
                    imageView.setImageResource(R.drawable.im_splash);
                    return;
                } else {
                    imageView.setImageDrawable(WallpaperManager.getInstance(context).getDrawable());
                    return;
                }
            } catch (Exception unused) {
                imageView.setImageResource(R.drawable.im_splash);
                return;
            }
        }
        Point point = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        VideoLayout videoLayout = new VideoLayout(context);
        this.videoLayout = videoLayout;
        videoLayout.setGravity(VideoLayout.VGravity.centerCrop);
        this.videoLayout.setIsLoop(true);
        this.videoLayout.setSound(false);
        this.videoLayout.setPathOrUrl(photo);
        relativeLayout.addView(this.videoLayout, point.x, point.y);
    }

    public BackgroundManager(RelativeLayout relativeLayout, String str) {
        Context context = relativeLayout.getContext();
        if (str.isEmpty() || str.toLowerCase().contains(".gif") || str.toLowerCase().contains(".jpg") || str.toLowerCase().contains(".png")) {
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            relativeLayout.addView(imageView, -1, -1);
            if (!str.isEmpty()) {
                Glide.with(context).load(str).into(imageView);
                return;
            }
            try {
                if (ActivityCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE") != 0) {
                    imageView.setImageResource(R.drawable.im_splash);
                    return;
                } else {
                    imageView.setImageDrawable(WallpaperManager.getInstance(context).getDrawable());
                    return;
                }
            } catch (Exception unused) {
                imageView.setImageResource(R.drawable.im_splash);
                return;
            }
        }
        Point point = new Point();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
        VideoLayout videoLayout = new VideoLayout(context);
        this.videoLayout = videoLayout;
        videoLayout.setGravity(VideoLayout.VGravity.centerCrop);
        this.videoLayout.setIsLoop(true);
        this.videoLayout.setSound(false);
        this.videoLayout.setPathOrUrl(str);
        relativeLayout.addView(this.videoLayout, point.x, point.y);
    }

    public void onResume() {
        VideoLayout videoLayout = this.videoLayout;
        if (videoLayout != null) {
            videoLayout.onResumeVideoLayout();
        }
    }

    public void onPause() {
        VideoLayout videoLayout = this.videoLayout;
        if (videoLayout != null) {
            videoLayout.onPauseVideoLayout();
        }
    }

    public void onDestroy() {
        VideoLayout videoLayout = this.videoLayout;
        if (videoLayout != null) {
            videoLayout.onDestroyVideoLayout();
        }
    }
}
