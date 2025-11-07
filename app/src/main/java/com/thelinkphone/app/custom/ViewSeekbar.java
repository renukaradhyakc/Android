package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewSeekbar extends View {
    private final Paint paint;
    private final float ra;
    private int sec;
    private SecResult secResult;

    
    public interface SecResult {
        void onSecResult(int i);
    }

    public void setSecResult(SecResult secResult) {
        this.secResult = secResult;
    }

    public ViewSeekbar(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        Paint paint = new Paint(1);
        this.paint = paint;
        float f = widthScreen;
        paint.setStrokeWidth(f / 120.0f);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setColor(Color.parseColor("#FFB01A"));
        this.ra = f / 50.0f;
    }

    public void setSec(int i) {
        this.sec = i;
        invalidate();
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setColor(Color.parseColor("#B8B8B8"));
        canvas.drawLine(this.ra, getHeight() / 2.0f, getWidth() - this.ra, getHeight() / 2.0f, this.paint);
        this.paint.setColor(Color.parseColor("#FF2828"));
        float f = this.ra;
        float f2 = this.ra;
        float width = f + (((getWidth() - (f2 * 2.0f)) * this.sec) / 61.0f);
        canvas.drawLine(f2, getHeight() / 2.0f, width, getHeight() / 2.0f, this.paint);
        this.paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(width, getHeight() / 2.0f, this.ra, this.paint);
    }

    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float f = this.ra;
        if (x < f) {
            x = f;
        } else if (x > getWidth() - this.ra) {
            x = getWidth() - this.ra;
        }
        this.sec = (int) (((x - this.ra) * 61.0f) / (getWidth() - (this.ra * 2.0f)));
        invalidate();
        SecResult secResult = this.secResult;
        if (secResult != null) {
            secResult.onSecResult(this.sec);
            return true;
        }
        return true;
    }
}
