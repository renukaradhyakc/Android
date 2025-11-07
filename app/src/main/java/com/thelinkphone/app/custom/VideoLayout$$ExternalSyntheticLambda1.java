package com.thelinkphone.app.custom;

import android.media.MediaPlayer;



public final  class VideoLayout$$ExternalSyntheticLambda1 implements MediaPlayer.OnPreparedListener {
    public static final  VideoLayout$$ExternalSyntheticLambda1 INSTANCE = new VideoLayout$$ExternalSyntheticLambda1();

    private  VideoLayout$$ExternalSyntheticLambda1() {
    }

    @Override 
    public final void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }
}
