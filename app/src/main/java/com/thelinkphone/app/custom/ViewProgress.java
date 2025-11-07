package com.thelinkphone.app.custom;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewProgress extends View {
    private final Paint paint;
    private float pos;
    private final float stroke;

    public ViewProgress(Context context) {
        super(context);
        float widthScreen = OtherUtils.getWidthScreen(context) / 150.0f;
        this.stroke = widthScreen;
        Paint paint = new Paint(1);
        this.paint = paint;
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(widthScreen);
        paint.setStyle(Paint.Style.STROKE);
        this.pos = 0.0f;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.setInterpolator(new LinearInterpolator());
        ofFloat.setDuration(2000L);
        ofFloat.setInterpolator(new DecelerateInterpolator(3.0f));
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { 
            @Override 
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewProgress.this.m94xb02b7188(valueAnimator);
            }
        });
        ofFloat.setStartDelay(400L);
        ofFloat.start();
    }



    public  void m94xb02b7188(ValueAnimator valueAnimator) {
        this.pos = ((Float) valueAnimator.getAnimatedValue()).floatValue() * 99.0f;
        invalidate();
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setColor(Color.parseColor("#D9D9D9"));
        canvas.drawLine(this.stroke, getHeight() / 2.0f, getWidth() - this.stroke, getHeight() / 2.0f, this.paint);
        this.paint.setColor(Color.parseColor("#007AFF"));
        float f = this.pos;
        float f2 = this.stroke;
        this.paint.setStrokeWidth(f2 + 1.0f);
        canvas.drawLine(this.stroke, getHeight() / 2.0f, ((f * (getWidth() - (f2 * 2.0f))) / 100.0f) + this.stroke, getHeight() / 2.0f, this.paint);
    }
}
