package com.thelinkphone.app.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;


public class DialogChooseGallery extends BaseDialogIOS {
    private final GalleryResult galleryResult;
    private final int textBot;
    private final int textTop;

    
    public interface GalleryResult {
        void onImageClick();

        void onVideoClick();
    }

    public DialogChooseGallery(Context context, int i, int i2, GalleryResult galleryResult) {
        super(context);
        this.galleryResult = galleryResult;
        this.textTop = i;
        this.textBot = i2;
    }

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int parseColor = Color.parseColor("#333333");
        if (!this.theme) {
            parseColor = -1;
        }
        addText(1000, this.textTop, parseColor);
        addDivider();
        addText(1001, this.textBot, parseColor);
    }

    @Override 
    void action(int i) {
        if (i == 1000) {
            this.galleryResult.onImageClick();
        } else if (i == 1001) {
            this.galleryResult.onVideoClick();
        }
    }
}
