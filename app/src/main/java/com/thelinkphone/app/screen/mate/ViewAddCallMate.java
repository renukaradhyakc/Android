package com.thelinkphone.app.screen.mate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.thelinkphone.app.R;
import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewAddCallMate extends View {
    private final Bitmap bm1;
    private final Bitmap bm2;
    private int c;
    private final Handler handler;
    private boolean isRemove;
    private boolean isTouch;
    private ActionAcceptResult itfAddCall;
    private final Paint pDot;
    private final Paint paint;
    private Rect r1;
    private Rect r2;
    private int run;
    private final Runnable runnable;
    private final int size;
    private int step;
    private boolean touch;
    private final int w;

    static  int access$108(ViewAddCallMate viewAddCallMate) {
        int i = viewAddCallMate.run;
        viewAddCallMate.run = i + 1;
        return i;
    }

    public void setItfAddCall(ActionAcceptResult actionAcceptResult) {
        this.itfAddCall = actionAcceptResult;
    }

    public ViewAddCallMate(Context context) {
        super(context);
        Runnable runnable = new Runnable() { 
            @Override 
            public void run() {
                ViewAddCallMate.this.handler.postDelayed(this, 200L);
                ViewAddCallMate.access$108(ViewAddCallMate.this);
                if (ViewAddCallMate.this.run == 5) {
                    ViewAddCallMate.this.run = 0;
                }
                ViewAddCallMate.this.invalidate();
            }
        };
        this.runnable = runnable;
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        this.c = 0;
        Handler handler = new Handler();
        this.handler = handler;
        Paint paint = new Paint(1);
        this.pDot = paint;
        paint.setColor(-1);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);
        Paint paint2 = new Paint(1);
        this.paint = paint2;
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(3.0f);
        paint2.setColor(-1);
        this.size = (widthScreen * 18) / 100;
        this.bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_add_call_mate);
        this.bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_end_call_mate);
        handler.post(runnable);
        this.isTouch = true;
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
                    ViewAddCallMate.this.m199x6c78e435();
                }
            }).start();
        }
    }

    
    
    public  void m199x6c78e435() {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.r1 == null) {
            int height = getHeight();
            int i = this.size;
            this.r1 = new Rect(0, (height - i) / 2, i, (getHeight() + this.size) / 2);
            this.r2 = new Rect(getWidth() - this.size, (getHeight() - this.size) / 2, getWidth(), (getHeight() + this.size) / 2);
            int i2 = this.size;
            this.step = (((getWidth() / 2) - (i2 / 2)) - i2) / 6;
        }
        if (!this.touch) {
            for (int i3 = 0; i3 < 5; i3++) {
                if (this.run == i3) {
                    this.pDot.setStrokeWidth(this.w / 80.0f);
                } else {
                    this.pDot.setStrokeWidth(this.w / 120.0f);
                }
                canvas.drawPoint((getWidth() / 2.0f) + ((this.size * 3.0f) / 4.0f) + (this.step * i3), getHeight() / 2.0f, this.pDot);
                canvas.drawPoint(((getWidth() / 2.0f) - ((this.size * 3.0f) / 4.0f)) - (this.step * i3), getHeight() / 2.0f, this.pDot);
            }
        }
        canvas.drawCircle((getWidth() / 2.0f) + this.c, getHeight() / 2.0f, this.size / 2.0f, this.paint);
        canvas.drawBitmap(this.bm1, (Rect) null, this.r1, (Paint) null);
        canvas.drawBitmap(this.bm2, (Rect) null, this.r2, (Paint) null);
    }

    @Override 
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.isTouch) {
            int action = motionEvent.getAction();
            if (action == 0) {
                boolean z = motionEvent.getX() > ((float) (getWidth() - this.size)) / 2.0f && motionEvent.getX() < ((float) (getWidth() + this.size)) / 2.0f;
                this.touch = z;
                if (z) {
                    this.run = 0;
                    this.handler.removeCallbacks(this.runnable);
                }
            } else if (action != 1) {
                if (action == 2 && this.touch) {
                    int x = (int) (motionEvent.getX() - (getWidth() / 2));
                    this.c = x;
                    if (x < (-(getWidth() - this.size)) / 2) {
                        this.c = (-(getWidth() - this.size)) / 2;
                    } else if (this.c > (getWidth() - this.size) / 2) {
                        this.c = (getWidth() - this.size) / 2;
                    }
                }
            } else if (this.c == (-(getWidth() - this.size)) / 2) {
                this.itfAddCall.onAccept();
                this.isTouch = false;
            } else if (this.c == (getWidth() - this.size) / 2) {
                this.itfAddCall.onReject();
                this.isTouch = false;
            } else {
                this.c = 0;
                this.touch = false;
                this.handler.post(this.runnable);
            }
            invalidate();
            return true;
        }
        return true;
    }
}
