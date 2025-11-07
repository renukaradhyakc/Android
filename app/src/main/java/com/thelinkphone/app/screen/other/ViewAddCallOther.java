package com.thelinkphone.app.screen.other;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.thelinkphone.app.R;
import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewAddCallOther extends View {
    private ActionAcceptResult actionScreenResult;
    private int alpha;
    private final Drawable dAdd;
    private final Drawable dEnd;
    private final Handler handler;
    private boolean isRemove;
    private boolean isTouch;
    private final Paint paint;
    private final Rect rAdd;
    private final Rect rEnd;
    private final Runnable runnable;
    private final int size;
    private int status;
    private boolean touchAdd;
    private boolean touchEnd;
    private final int w;
    private int xAdd;
    private int xEnd;
    private float xStart;

    static  int access$208(ViewAddCallOther viewAddCallOther) {
        int i = viewAddCallOther.status;
        viewAddCallOther.status = i + 1;
        return i;
    }

    public void setActionScreenResult(ActionAcceptResult actionAcceptResult) {
        this.actionScreenResult = actionAcceptResult;
    }

    public ViewAddCallOther(Context context) {
        super(context);
        Runnable runnable = new Runnable() { 
            @Override 
            public void run() {
                ViewAddCallOther.this.handler.postDelayed(ViewAddCallOther.this.runnable, 500L);
                ViewAddCallOther.access$208(ViewAddCallOther.this);
                if (ViewAddCallOther.this.status == 3) {
                    ViewAddCallOther.this.status = 0;
                }
                ViewAddCallOther.this.invalidate();
            }
        };
        this.runnable = runnable;
        this.dAdd = getResources().getDrawable(R.drawable.im_add_call_other);
        this.dEnd = getResources().getDrawable(R.drawable.im_end_call_other);
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        this.size = (widthScreen * 19) / 100;
        this.xAdd = widthScreen / 5;
        this.xEnd = widthScreen - (widthScreen / 5);
        Paint paint = new Paint(1);
        this.paint = paint;
        paint.setColor(-1);
        paint.setStrokeWidth((widthScreen * 0.83f) / 100.0f);
        this.rAdd = new Rect();
        this.rEnd = new Rect();
        Handler handler = new Handler();
        this.handler = handler;
        handler.post(runnable);
        this.isTouch = true;
        this.alpha = 255;
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.paint.setAlpha(255);
        Rect rect = this.rAdd;
        int i = this.xAdd - (this.size / 2);
        int height = getHeight();
        int i2 = this.size;
        rect.set(i, (height - i2) / 2, this.xAdd + (i2 / 2), (getHeight() + this.size) / 2);
        Rect rect2 = this.rEnd;
        int i3 = this.xEnd - (this.size / 2);
        int height2 = getHeight();
        int i4 = this.size;
        rect2.set(i3, (height2 - i4) / 2, this.xEnd + (i4 / 2), (getHeight() + this.size) / 2);
        if (!this.touchAdd && !this.touchEnd) {
            this.paint.setStyle(Paint.Style.FILL);
            this.paint.setAlpha(100);
            int i5 = this.size;
            int i6 = (this.w / 5) + (i5 / 2) + (i5 / 4);
            canvas.drawCircle(i6, getHeight() / 2.0f, this.w / 150.0f, this.paint);
            canvas.drawCircle(this.w - i6, getHeight() / 2.0f, this.w / 150.0f, this.paint);
            int i7 = i6 + (this.size / 4);
            canvas.drawCircle(i7, getHeight() / 2.0f, this.w / 150.0f, this.paint);
            canvas.drawCircle(this.w - i7, getHeight() / 2.0f, this.w / 150.0f, this.paint);
            int i8 = this.status;
            if (i8 == 0) {
                canvas.drawCircle(this.xAdd, getHeight() / 2.0f, this.size / 2.0f, this.paint);
                canvas.drawCircle(this.xEnd, getHeight() / 2.0f, this.size / 2.0f, this.paint);
            } else if (i8 == 1) {
                this.paint.setAlpha(255);
                int i9 = this.size;
                int i10 = (this.w / 5) + (i9 / 2) + (i9 / 4);
                canvas.drawCircle(i10, getHeight() / 2.0f, this.w / 100.0f, this.paint);
                canvas.drawCircle(this.w - i10, getHeight() / 2.0f, this.w / 100.0f, this.paint);
            } else {
                this.paint.setAlpha(255);
                int i11 = (this.w / 5) + this.size;
                canvas.drawCircle(i11, getHeight() / 2.0f, this.w / 100.0f, this.paint);
                canvas.drawCircle(this.w - i11, getHeight() / 2.0f, this.w / 100.0f, this.paint);
            }
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setAlpha(255);
            canvas.drawCircle(this.xAdd, getHeight() / 2.0f, this.size / 2.0f, this.paint);
            canvas.drawCircle(this.xEnd, getHeight() / 2.0f, this.size / 2.0f, this.paint);
            this.dAdd.setAlpha(255);
            this.dEnd.setAlpha(255);
        }
        this.paint.setStyle(Paint.Style.STROKE);
        if (this.touchAdd) {
            this.dEnd.setAlpha(this.alpha);
            this.paint.setAlpha(255);
            canvas.drawCircle(this.xAdd, getHeight() / 2.0f, this.size / 2.0f, this.paint);
            this.paint.setAlpha(this.alpha);
            canvas.drawCircle(this.xEnd, getHeight() / 2.0f, this.size / 2.0f, this.paint);
        } else if (this.touchEnd) {
            this.dAdd.setAlpha(this.alpha);
            this.paint.setAlpha(this.alpha);
            canvas.drawCircle(this.xAdd, getHeight() / 2.0f, this.size / 2.0f, this.paint);
            this.paint.setAlpha(255);
            canvas.drawCircle(this.xEnd, getHeight() / 2.0f, this.size / 2.0f, this.paint);
        }
        this.dAdd.setBounds(this.rAdd);
        this.dAdd.draw(canvas);
        this.dEnd.setBounds(this.rEnd);
        this.dEnd.draw(canvas);
    }


    @Override 

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (this.isTouch) {
            int action = motionEvent.getAction();
            if (action == 0) {
                this.xStart = motionEvent.getX();
                if (Math.abs(motionEvent.getY() - (getHeight() / 2.0f)) < this.size / 2.0f) {
                    this.touchAdd = Math.abs(this.xStart - ((float) this.xAdd)) < ((float) this.size) / 2.0f;
                    this.touchEnd = Math.abs(this.xStart - ((float) this.xEnd)) < ((float) this.size) / 2.0f;
                }
                if (this.touchAdd || this.touchEnd) {
                    this.handler.removeCallbacks(this.runnable);
                }
            } else {
                if (action != 1) {
                    if (action == 2) {
                        float x = motionEvent.getX() - this.xStart;
                        int i = this.w;
                        int abs = 255 - ((int) ((Math.abs(x) * 255.0f) / ((i / 2) - (i / 5))));
                        this.alpha = abs;
                        if (abs < 0) {
                            this.alpha = 0;
                        } else if (abs > 255) {
                            this.alpha = 255;
                        }
                        if (this.touchAdd) {
                            int i2 = (int) ((i / 5) + x);
                            this.xAdd = i2;
                            if (i2 < i / 5) {
                                this.xAdd = i / 5;
                            } else if (i2 > i / 2) {
                                this.xAdd = i / 2;
                            }
                        } else if (this.touchEnd) {
                            int i3 = (int) ((i - (i / 5)) + x);
                            this.xEnd = i3;
                            if (i3 > i - (i / 5)) {
                                this.xEnd = i - (i / 5);
                            } else if (i3 < i / 2) {
                                this.xEnd = i / 2;
                            }
                        }
                    }
                }
                if (this.touchAdd) {
                    int i4 = this.xAdd;
                    int i5 = this.w;
                    if (i4 == i5 / 2) {
                        this.isTouch = false;
                        this.actionScreenResult.onAccept();
                    } else {
                        this.xAdd = i5 / 5;
                        this.touchAdd = false;
                        this.alpha = 255;
                        this.handler.post(this.runnable);
                    }
                } else if (this.touchEnd) {
                    int i6 = this.xEnd;
                    int i7 = this.w;
                    if (i6 == i7 / 2) {
                        this.isTouch = false;
                        this.actionScreenResult.onReject();
                    } else {
                        this.xEnd = i7 - (i7 / 5);
                        this.touchEnd = false;
                        this.alpha = 255;
                        this.handler.post(this.runnable);
                    }
                }
            }
            invalidate();
            return true;
        }
        return true;
    }

    public void onRemove() {
        this.handler.removeCallbacks(this.runnable);
        if (getVisibility() == View.GONE && getParent() != null) {
            this.isRemove = true;
            ((ViewGroup) getParent()).removeView(this);
        } else if (this.isRemove) {
        } else {
            this.isRemove = true;
            animate().alpha(0.0f).setDuration(500L).withEndAction(new Runnable() { 
                @Override 
                public final void run() {
                    ViewAddCallOther.this.m208x47c7c971();
                }
            }).start();
        }
    }



    public  void m208x47c7c971() {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }
}
