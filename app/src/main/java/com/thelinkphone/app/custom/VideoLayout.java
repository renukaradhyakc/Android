package com.thelinkphone.app.custom;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import java.io.IOException;
import java.util.HashMap;


public class VideoLayout extends FrameLayout implements TextureView.SurfaceTextureListener {
    private boolean ADJUSTVIEWBOUNDS;
    private String FILE_NAME;
    private boolean IS_LOOP;
    private boolean SOUND;
    private String TAG;
    private int VIDEO_GRAVITY;
    private boolean isUrl;
    private MediaPlayer mMediaPlayer;
    private float mVideoHeight;
    private float mVideoWidth;
    private TextureView videoSurface;

    @Override 
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override 
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    @Override 
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    
    
    
    public static  class AnonymousClass1 {
        static final  int[] $SwitchMap$com$callos14$callscreen$colorphone$custom$VideoLayout$VGravity;

        static {
            int[] iArr = new int[VGravity.values().length];
            $SwitchMap$com$callos14$callscreen$colorphone$custom$VideoLayout$VGravity = iArr;
            try {
                iArr[VGravity.end.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$callos14$callscreen$colorphone$custom$VideoLayout$VGravity[VGravity.start.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$com$callos14$callscreen$colorphone$custom$VideoLayout$VGravity[VGravity.centerCrop.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$com$callos14$callscreen$colorphone$custom$VideoLayout$VGravity[VGravity.centerInside.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$com$callos14$callscreen$colorphone$custom$VideoLayout$VGravity[VGravity.fitXY.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
        }
    }

    
    public enum VGravity {
        start,
        end,
        centerCrop,
        fitXY,
        centerInside;

        public int getValue() {
            int i = AnonymousClass1.$SwitchMap$com$callos14$callscreen$colorphone$custom$VideoLayout$VGravity[ordinal()];
            if (i != 1) {
                int i2 = 2;
                if (i != 2) {
                    if (i != 3) {
                        i2 = 4;
                        if (i != 4) {
                            return 3;
                        }
                    }
                    return i2;
                }
                return 0;
            }
            return 1;
        }
    }

    public VideoLayout(Context context) {
        super(context);
        this.TAG = "VideoLayout";
        this.ADJUSTVIEWBOUNDS = true;
    }

    public void setSound(boolean z) {
        this.SOUND = z;
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            try {
                if (!z) {
                    mediaPlayer.setVolume(0.0f, 0.0f);
                } else {
                    mediaPlayer.setVolume(0.5f, 0.5f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setAdjustViewBounds(boolean z) {
        this.ADJUSTVIEWBOUNDS = z;
    }

    private void initViews() {
        this.videoSurface = new TextureView(getContext());
    }

    private void setListeners() {
        this.videoSurface.setSurfaceTextureListener(this);
    }

    private void calculateVideoSize() {
        try {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            if (this.isUrl) {
                mediaMetadataRetriever.setDataSource(this.FILE_NAME, new HashMap());
            } else {
                AssetFileDescriptor openFd = getContext().getAssets().openFd(this.FILE_NAME);
                mediaMetadataRetriever.setDataSource(openFd.getFileDescriptor(), openFd.getStartOffset(), openFd.getLength());
            }
            String extractMetadata = mediaMetadataRetriever.extractMetadata(19);
            String extractMetadata2 = mediaMetadataRetriever.extractMetadata(18);
            this.mVideoHeight = Float.parseFloat(extractMetadata);
            this.mVideoWidth = Float.parseFloat(extractMetadata2);
            mediaMetadataRetriever.release();
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private void updateTextureViewSize(int i, int i2) {
        float f;
        int i3 = this.VIDEO_GRAVITY;
        int i4 = i3 == 0 ? 0 : i3 == 1 ? i : i / 2;
        int i5 = i2 / 2;
        Matrix matrix = new Matrix();
        float f2 = 1.0f;
        if (this.VIDEO_GRAVITY == 4) {
            matrix.setScale(1.0f, (i / this.mVideoWidth) / (i2 / this.mVideoHeight), 0, i5);
        } else {
            float f3 = this.mVideoWidth;
            float f4 = i;
            if (f3 > f4) {
                float f5 = this.mVideoHeight;
                float f6 = i2;
                if (f5 > f6) {
                    f2 = f3 / f4;
                    f = f5 / f6;
                    matrix.setScale(f2, f, i4, i5);
                }
            }
            if (f3 < f4) {
                float f7 = this.mVideoHeight;
                float f8 = i2;
                if (f7 < f8) {
                    f2 = f8 / f7;
                    f = f4 / f3;
                    matrix.setScale(f2, f, i4, i5);
                }
            }
            if (f4 > f3) {
                f = (f4 / f3) / (i2 / this.mVideoHeight);
            } else {
                float f9 = i2;
                float f10 = this.mVideoHeight;
                if (f9 > f10) {
                    f = 1.0f;
                    f2 = (f9 / f10) / (f4 / f3);
                } else {
                    f = 1.0f;
                }
            }
            matrix.setScale(f2, f, i4, i5);
        }
        this.videoSurface.setLayoutParams(new LayoutParams(i, i2));
    }

    private void surfaceSetup() {
        Point point = new Point();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealSize(point);
        float f = point.y;
        float f2 = point.x;
        float f3 = this.mVideoWidth;
        float f4 = this.mVideoHeight;
        if (f2 / f > f3 / f4) {
            updateTextureViewSize((int) f2, (int) ((f4 * f2) / f3));
        } else {
            updateTextureViewSize((int) ((f3 * f) / f4), (int) f);
        }
    }

    private void surfaceAvailableWorkers(SurfaceTexture surfaceTexture) {
        Surface surface = new Surface(surfaceTexture);
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            this.mMediaPlayer = mediaPlayer;
            mediaPlayer.setDataSource(this.FILE_NAME);
            if (!this.SOUND) {
                this.mMediaPlayer.setVolume(0.0f, 0.0f);
            }
            this.mMediaPlayer.setSurface(surface);
            this.mMediaPlayer.setLooping(this.IS_LOOP);
            this.mMediaPlayer.prepareAsync();
            this.mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { 
                @Override 
                public final void onPrepared(MediaPlayer mediaPlayer2) {
                    VideoLayout.this.m88x741f8788(mediaPlayer2);
                }
            });
        } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException unused) {
        }
    }

    
    
    public  void m88x741f8788(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        this.mVideoHeight = mediaPlayer.getVideoHeight();
        this.mVideoWidth = mediaPlayer.getVideoWidth();
        if (this.VIDEO_GRAVITY != 3) {
            surfaceSetup();
        }
    }

    private void changeVideo() {
        try {
            onDestroyVideoLayout();
            MediaPlayer mediaPlayer = new MediaPlayer();
            this.mMediaPlayer = mediaPlayer;
            mediaPlayer.setDataSource(this.FILE_NAME);
            if (!this.SOUND) {
                this.mMediaPlayer.setVolume(0.0f, 0.0f);
            }
            this.mMediaPlayer.setLooping(this.IS_LOOP);
            this.mMediaPlayer.setSurface(new Surface(this.videoSurface.getSurfaceTexture()));
            this.mMediaPlayer.prepareAsync();
            this.mMediaPlayer.setOnPreparedListener(VideoLayout$$ExternalSyntheticLambda1.INSTANCE);
        } catch (IOException | IllegalArgumentException | IllegalStateException | SecurityException unused) {
        }
    }

    @Override 
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        surfaceAvailableWorkers(surfaceTexture);
    }

    public void onDestroyVideoLayout() {
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                this.mMediaPlayer.release();
                this.mMediaPlayer = null;
            } catch (IllegalStateException unused) {
            }
        }
    }

    public void onResumeVideoLayout() {
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer == null || mediaPlayer.isPlaying()) {
            return;
        }
        try {
            this.mMediaPlayer.start();
        } catch (IllegalStateException unused) {
        }
    }

    public void onPauseVideoLayout() {
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer == null || !mediaPlayer.isPlaying()) {
            return;
        }
        try {
            this.mMediaPlayer.pause();
        } catch (IllegalStateException unused) {
        }
    }

    public MediaPlayer getMediaPlayer() {
        return this.mMediaPlayer;
    }

    public TextureView getVideoSurface() {
        return this.videoSurface;
    }

    public void setPathOrUrl(String str) {
        this.FILE_NAME = str;
        this.isUrl = str.contains("http://") || str.contains("https://");
        if (this.videoSurface == null) {
            initViews();
            addView(this.videoSurface);
            setListeners();
        }
        if (this.videoSurface != null) {
            changeVideo();
        }
    }

    public void setIsLoop(boolean z) {
        this.IS_LOOP = z;
    }

    public void setGravity(VGravity vGravity) {
        this.VIDEO_GRAVITY = vGravity.getValue();
    }
}
