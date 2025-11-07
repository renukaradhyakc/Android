package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import androidx.exifinterface.media.ExifInterface;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewScrollAlphaB extends View {
    private final String[] alphaB;
    private AlphaBResult alphaBResult;
    private final float height;
    private final float pa;
    private final Paint paint;
    private int pos;
    private final float width;

    
    public interface AlphaBResult {
        void onAlphaBResult(String str);
    }

    public void setAlphaBResult(AlphaBResult alphaBResult) {
        this.alphaBResult = alphaBResult;
    }

    public ViewScrollAlphaB(Context context) {
        super(context);
        float widthScreen = OtherUtils.getWidthScreen(context);
        this.pa = (1.1f * widthScreen) / 100.0f;
        this.alphaB = OtherUtils.arrAlphaB();
        Rect rect = new Rect();
        Paint paint = new Paint(1);
        this.paint = paint;
        paint.setColor(Color.parseColor("#007AFF"));
        paint.setTextSize((widthScreen * 2.4f) / 100.0f);
        if (Build.VERSION.SDK_INT >= 28) {
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, 550, false));
        } else {
            paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, 0));
        }
        paint.getTextBounds(ExifInterface.GPS_MEASUREMENT_IN_PROGRESS, 0, 1, rect);
        paint.setTextAlign(Paint.Align.CENTER);
        this.height = rect.height();
        this.width = rect.width();
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = 0;
        while (true) {
            String[] strArr = this.alphaB;
            if (i >= strArr.length) {
                return;
            }
            float f = this.height;
            float f2 = this.pa;
            int i2 = i + 1;
            canvas.drawText(strArr[i], getWidth() - this.width, ((f + f2) * i2) + (i * f2), this.paint);
            i = i2;
        }
    }

    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int y = (int) (motionEvent.getY() / (this.height + (this.pa * 2.0f)));
        String[] strArr = this.alphaB;
        if (y >= strArr.length) {
            y = strArr.length - 1;
        } else if (y < 0) {
            y = 0;
        }
        AlphaBResult alphaBResult = this.alphaBResult;
        if (alphaBResult != null && this.pos != y) {
            this.pos = y;
            alphaBResult.onAlphaBResult(strArr[y]);
        }
        return true;
    }

    public int makeHeightView() {
        float f = this.height;
        String[] strArr = this.alphaB;
        return (int) ((f * strArr.length) + (this.pa * 2.0f * strArr.length));
    }
}
