package com.thelinkphone.app.screen.samsung;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.thelinkphone.app.R;
import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewAddCallGalaxy extends View {
    private ActionAcceptResult addCallGalaxyResult;
    private final Bitmap bm1;
    private final Bitmap bm2;
    private final Handler handler;
    private boolean isRemove;
    private boolean isTouch;
    private final Paint paint;
    private final Rect r1;
    private final Rect r2;
    private float radius;
    private final Runnable runnable;
    private final int size;
    private boolean t1;
    private boolean t2;
    private final int w;
    private final int x1;
    private final int x2;
    private final int y;

    static  float access$016(ViewAddCallGalaxy viewAddCallGalaxy, float f) {
        float f2 = viewAddCallGalaxy.radius + f;
        viewAddCallGalaxy.radius = f2;
        return f2;
    }

    public ViewAddCallGalaxy(Context context) {
        super(context);
        Runnable runnable = new Runnable() { 
            @Override 
            public void run() {
                ViewAddCallGalaxy viewAddCallGalaxy = ViewAddCallGalaxy.this;
                if (ViewAddCallGalaxy.this.radius < ((ViewAddCallGalaxy.this.w * 22.7d) / 100.0d) / 2.0d) {
                    ViewAddCallGalaxy.this.handler.postDelayed(this, 30L);
                    ViewAddCallGalaxy.access$016(ViewAddCallGalaxy.this, 1.0f);
                    ViewAddCallGalaxy.this.paint.setAlpha(100);
                } else {
                    ViewAddCallGalaxy.this.handler.postDelayed(this, 35L);
                    int alpha = ViewAddCallGalaxy.this.paint.getAlpha() - 4;
                    if (alpha > 0) {
                        ViewAddCallGalaxy.this.paint.setAlpha(alpha);
                    } else {
                        ViewAddCallGalaxy.this.radius = viewAddCallGalaxy.size / 2.0f;
                    }
                }
                ViewAddCallGalaxy.this.invalidate();
            }
        };
        this.runnable = runnable;
        setBackgroundColor(Color.parseColor("#30ffffff"));
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        this.bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_call);
        this.bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_end_call_galaxy);
        int i = (int) ((widthScreen * 16.7d) / 100.0d);
        this.size = i;
        this.radius = i / 2.0f;
        int i2 = (i * 8) / 7;
        this.x1 = i2;
        int i3 = widthScreen - ((i * 8) / 7);
        this.x2 = i3;
        int i4 = (((getResources().getDisplayMetrics().heightPixels / 2) + ((int) ((widthScreen * 57.6d) / 100.0d))) / 2) - ((widthScreen * 15) / 100);
        this.y = i4;
        this.r1 = new Rect(i2 - (i / 2), i4 - (i / 2), i2 + (i / 2), (i / 2) + i4);
        this.r2 = new Rect(i3 - (i / 2), i4 - (i / 2), i3 + (i / 2), i4 + (i / 2));
        Paint paint = new Paint(1);
        this.paint = paint;
        paint.setColor(-1);
        paint.setAlpha(100);
        this.isTouch = true;
        Handler handler = new Handler();
        this.handler = handler;
        handler.post(runnable);
    }

    public void setAddCallGalaxyResult(ActionAcceptResult actionAcceptResult) {
        this.addCallGalaxyResult = actionAcceptResult;
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this.t1 && !this.t2) {
            this.paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(this.x1, this.y, this.radius, this.paint);
            canvas.drawCircle(this.x2, this.y, this.radius, this.paint);
        } else if (this.paint.getAlpha() > 0) {
            float f = this.radius;
            float f2 = 2.0f * f;
            if (f2 < this.size * 2) {
                this.paint.setStyle(Paint.Style.FILL);
                f = f2;
            } else {
                this.paint.setStyle(Paint.Style.STROKE);
                Paint paint = this.paint;
                int i = this.size;
                paint.setStrokeWidth(i * 2 * (1.0f - ((this.radius - i) / i)));
            }
            if (this.t1) {
                canvas.drawCircle(this.x1, this.y, f, this.paint);
            } else {
                canvas.drawCircle(this.x2, this.y, f, this.paint);
            }
        }
        canvas.drawBitmap(this.bm1, (Rect) null, this.r1, (Paint) null);
        canvas.drawBitmap(this.bm2, (Rect) null, this.r2, (Paint) null);
    }

    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        if (this.isTouch) {
            int action = motionEvent.getAction();
            boolean z2 = false;
            if (action == 0) {
                if (motionEvent.getY() > this.y - (this.size / 2.0f) && motionEvent.getY() < this.y + (this.size / 2.0f)) {
                    float x = motionEvent.getX();
                    int i = this.x1;
                    int i2 = this.size;
                    this.t1 = x > ((float) i) - (((float) i2) / 2.0f) && x < ((float) i) + (((float) i2) / 2.0f);
                    int i3 = this.x2;
                    if (x > i3 - (i2 / 2.0f) && x < i3 + (i2 / 2.0f)) {
                        z2 = true;
                    }
                    this.t2 = z2;
                }
                if (this.t1 || this.t2) {
                    this.radius = 0.0f;
                    invalidate();
                    stopAnim();
                }
            } else if (action != 1) {
                if (action == 2 && ((z = this.t1) || this.t2)) {
                    if (z) {
                        this.radius = (float) Math.sqrt(Math.pow(motionEvent.getX() - this.x1, 2.0d) + Math.pow(motionEvent.getY() - this.y, 2.0d));
                    } else {
                        this.radius = (float) Math.sqrt(Math.pow(motionEvent.getX() - this.x2, 2.0d) + Math.pow(motionEvent.getY() - this.y, 2.0d));
                    }
                    float f = this.radius;
                    int i4 = this.size;
                    if (f > i4 * 2) {
                        this.radius = i4 * 2;
                        this.paint.setAlpha(0);
                    } else {
                        this.paint.setAlpha(100);
                    }
                }
            } else if (this.t1 || this.t2) {
                if (this.paint.getAlpha() == 0) {
                    this.isTouch = false;
                    if (this.t1) {
                        this.addCallGalaxyResult.onAccept();
                    } else {
                        this.addCallGalaxyResult.onReject();
                    }
                } else {
                    this.t2 = false;
                    this.t1 = false;
                    this.radius = this.size / 2.0f;
                    invalidate();
                    startAnim();
                }
            }
            invalidate();
            return true;
        }
        return true;
    }

    private void startAnim() {
        stopAnim();
        this.handler.post(this.runnable);
    }

    private void stopAnim() {
        this.handler.removeCallbacks(this.runnable);
    }

    public void onRemove() {
        if (getVisibility() == View.GONE && getParent() != null) {
            this.isRemove = true;
            ((ViewGroup) getParent()).removeView(this);
        } else if (this.isRemove) {
        } else {
            this.isRemove = true;
            animate().alpha(0.0f).setDuration(500L).withEndAction(new Runnable() { 
                @Override 
                public final void run() {
                    ViewAddCallGalaxy.this.m226xbe78a6b9();
                }
            }).start();
        }
    }

    
    
    public  void m226xbe78a6b9() {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }
}
