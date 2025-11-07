package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.VectorDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.core.view.GravityCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;


public class SvgRatingBar extends AppCompatRatingBar {
    private Bitmap sampleTile;

    public SvgRatingBar(Context context) {
        super(context);
        init();
    }

    public SvgRatingBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public SvgRatingBar(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {
        setProgressDrawable((LayerDrawable) createTile(getProgressDrawable(), false));
    }

    private Drawable createTile(Drawable drawable, boolean z) {
        if (drawable instanceof DrawableWrapper) {
            DrawableWrapper drawableWrapper = (DrawableWrapper) drawable;
          /*  Drawable wrappedDrawable = drawableWrapper.getWrappedDrawable();
            if (wrappedDrawable != null) {
                drawableWrapper.setWrappedDrawable(createTile(wrappedDrawable, z));
                return drawable;
            }*/
            return drawable;
        } else if (drawable instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) drawable;
            int numberOfLayers = layerDrawable.getNumberOfLayers();
            Drawable[] drawableArr = new Drawable[numberOfLayers];
            for (int i = 0; i < numberOfLayers; i++) {
                int id = layerDrawable.getId(i);
                drawableArr[i] = createTile(layerDrawable.getDrawable(i), id == 16908301 || id == 16908303);
            }
            LayerDrawable layerDrawable2 = new LayerDrawable(drawableArr);
            for (int i2 = 0; i2 < numberOfLayers; i2++) {
                layerDrawable2.setId(i2, layerDrawable.getId(i2));
            }
            return layerDrawable2;
        } else if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (this.sampleTile == null) {
                this.sampleTile = bitmap;
            }
            ShapeDrawable shapeDrawable = new ShapeDrawable(getDrawableShape());
            shapeDrawable.getPaint().setShader(new BitmapShader(bitmap, Shader.TileMode.REPEAT, Shader.TileMode.CLAMP));
            shapeDrawable.getPaint().setColorFilter(bitmapDrawable.getPaint().getColorFilter());
            return z ? new ClipDrawable(shapeDrawable, GravityCompat.START, 1) : shapeDrawable;
        } else if (Build.VERSION.SDK_INT < 21 || !(drawable instanceof VectorDrawable)) {
            return drawable instanceof VectorDrawableCompat ? createTile(getBitmapDrawableFromVectorDrawable(drawable), z) : drawable;
        } else {
            return createTile(getBitmapDrawableFromVectorDrawable(drawable), z);
        }
    }

    private BitmapDrawable getBitmapDrawableFromVectorDrawable(Drawable drawable) {
        Bitmap createBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return new BitmapDrawable(getResources(), createBitmap);
    }


    @Override 
    public synchronized void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        Bitmap bitmap = this.sampleTile;
        if (bitmap != null) {
            setMeasuredDimension(resolveSizeAndState(bitmap.getWidth() * getNumStars(), i, 0), getMeasuredHeight());
        }
    }

    private Shape getDrawableShape() {
        return new RoundRectShape(new float[]{5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f}, null, null);
    }
}
