package com.thelinkphone.app.screen.pixel;

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
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewAdd extends RelativeLayout {
    private boolean isRemove;
    private final ViewAddCallPixel viewAddCallPixel;

    public void setItfAddCall(ActionAcceptResult actionAcceptResult) {
        this.viewAddCallPixel.setItfAddCall(actionAcceptResult);
    }

    public ViewAdd(Context context) {
        super(context);
        ViewAddCallPixel viewAddCallPixel = new ViewAddCallPixel(context);
        this.viewAddCallPixel = viewAddCallPixel;
        int widthScreen = (OtherUtils.getWidthScreen(context) * 18) / 100;
        LayoutParams layoutParams = new LayoutParams(widthScreen, -1);
        layoutParams.addRule(14);
        addView(viewAddCallPixel, layoutParams);
        TextW initText = initText(R.string.swipe_up_to_answer);
        initText.setupText(400, 3.8f);
        LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        layoutParams2.addRule(14);
        int i = widthScreen / 4;
        layoutParams2.setMargins(0, i, 0, 0);
        addView(initText, layoutParams2);
        TextW initText2 = initText(R.string.swipe_down_to_reject);
        initText2.setupText(600, 3.8f);
        LayoutParams layoutParams3 = new LayoutParams(-2, -2);
        layoutParams3.addRule(14);
        layoutParams3.addRule(12);
        layoutParams3.setMargins(0, 0, 0, i);
        addView(initText2, layoutParams3);
    }

    private TextW initText(int i) {
        TextW textW = new TextW(getContext());
        textW.setTextColor(-1);
        textW.setAlpha(0.8f);
        textW.setText(i);
        return textW;
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
                    ViewAdd.this.m217x318b6057();
                }
            }).start();
        }
    }



    public  void m217x318b6057() {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }


    public class ViewAddCallPixel extends View {
        private final Bitmap bm;
        private final Bitmap bm1;
        private final Bitmap bm2;
        private final Handler handler;
        private boolean isDone;
        private ActionAcceptResult itfAddCall;
        private final Paint p;
        private final Paint p1;
        private final Paint p2;
        private Rect rect;
        private float run;
        private final Runnable runnable;
        private final int size;
        private float start;
        private boolean touch;
        private boolean tren;
        private boolean up;





        public void setItfAddCall(ActionAcceptResult actionAcceptResult) {
            this.itfAddCall = actionAcceptResult;
        }

        public ViewAddCallPixel(Context context) {
            super(context);
            this.runnable = new Runnable() { 
                @Override 
                public void run() {
                    ViewAddCallPixel.this.handler.postDelayed(this, 30L);
                    if (ViewAddCallPixel.this.tren) {
                        if (ViewAddCallPixel.this.up) {
                            ViewAddCallPixel viewAddCallPixel = ViewAddCallPixel.this;

                            float f2 = viewAddCallPixel.run - ((viewAddCallPixel.size / 2.0f) - ViewAddCallPixel.this.run) / 6.0f;
                            viewAddCallPixel.run = f2;


                            if (ViewAddCallPixel.this.run >= (ViewAddCallPixel.this.size / 2.0f) - 1.0f) {
                                ViewAddCallPixel viewAddCallPixel2 = ViewAddCallPixel.this;
                                viewAddCallPixel2.run = (viewAddCallPixel2.size / 2.0f) - 1.0f;
                                ViewAddCallPixel.this.up = false;
                            }
                        } else {

                            float f2 = viewAddCallPixel.run - 2.0f;
                            viewAddCallPixel.run = f2;

                            if (ViewAddCallPixel.this.run < 0.0f) {
                                ViewAddCallPixel.this.run = 0.0f;
                                ViewAddCallPixel.this.handler.removeCallbacks(this);
                                ViewAddCallPixel.this.handler.postDelayed(this, 800L);
                                ViewAddCallPixel.this.tren = false;
                            }
                        }
                    } else if (!ViewAddCallPixel.this.up) {
                        ViewAddCallPixel viewAddCallPixel3 = ViewAddCallPixel.this;

                        float f2 = viewAddCallPixel3.run - (((-viewAddCallPixel3.size) / 2.0f) - ViewAddCallPixel.this.run) / 6.0f;
                        viewAddCallPixel3.run = f2;


                        if (ViewAddCallPixel.this.run <= ((-ViewAddCallPixel.this.size) / 2.0f) + 1.0f) {
                            ViewAddCallPixel viewAddCallPixel4 = ViewAddCallPixel.this;
                            viewAddCallPixel4.run = ((-viewAddCallPixel4.size) / 2.0f) + 1.0f;
                            ViewAddCallPixel.this.up = true;
                        }
                    } else {
                        float f2 = ViewAddCallPixel.this.run - 2.0f;
                        ViewAddCallPixel.this.run = f2;


                        if (ViewAddCallPixel.this.run > 0.0f) {
                            ViewAddCallPixel.this.run = 0.0f;
                            ViewAddCallPixel.this.handler.removeCallbacks(this);
                            ViewAddCallPixel.this.handler.postDelayed(this, 800L);
                            ViewAddCallPixel.this.tren = true;
                        }
                    }
                    if (ViewAddCallPixel.this.rect != null) {
                        ViewAddCallPixel.this.updateRect();
                        ViewAddCallPixel.this.invalidate();
                    }
                }
            };
            this.up = true;
            this.tren = true;
            this.size = (OtherUtils.getWidthScreen(getContext()) * 18) / 100;
            this.run = 0.0f;
            this.p = new Paint(1);
            Paint paint = new Paint(1);
            this.p2 = paint;
            Paint paint2 = new Paint(1);
            this.p1 = paint2;
            this.bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_call);
            this.bm1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_call_pad);
            this.bm2 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_end_call);
            paint.setAlpha(0);
            paint2.setAlpha(0);
            this.handler = new Handler();
        }

        @Override 
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (this.rect == null) {
                this.rect = new Rect((getWidth() - this.size) / 2, (getHeight() - this.size) / 2, (getWidth() + this.size) / 2, (getHeight() + this.size) / 2);
                this.handler.postDelayed(this.runnable, 800L);
            }
            if (!this.touch) {
                canvas.drawBitmap(this.bm, (Rect) null, this.rect, (Paint) null);
                return;
            }
            if (this.p.getAlpha() > 2) {
                canvas.drawBitmap(this.bm, (Rect) null, this.rect, this.p);
            }
            if (this.p1.getAlpha() != 0) {
                canvas.drawBitmap(this.bm1, (Rect) null, this.rect, this.p1);
            }
            if (this.p2.getAlpha() != 0) {
                canvas.drawBitmap(this.bm2, (Rect) null, this.rect, this.p2);
            }
        }

        @Override 
        public boolean onTouchEvent(MotionEvent motionEvent) {
            int i = 0;
            if (this.isDone) {
                return true;
            }
            int action = motionEvent.getAction();
            boolean z = false;
            if (action == 0) {
                if (motionEvent.getY() > (getHeight() - this.size) / 2.0f && motionEvent.getY() < (getHeight() + this.size) / 2.0f) {
                    z = true;
                }
                this.touch = z;
                if (z) {
                    this.start = motionEvent.getY();
                    this.tren = true;
                    this.up = true;
                    this.run = 0.0f;
                    updateRect();
                    stopAnim();
                }
            } else if (action != 1) {
                if (action == 2 && this.touch) {
                    float y = this.start - motionEvent.getY();
                    this.run = y;
                    int i2 = this.size;
                    if (y > i2) {
                        this.run = i2;
                    } else if (y < (-i2)) {
                        this.run = -i2;
                    }
                    updateRect();
                    if (this.run > 0.0f) {
                        this.p2.setAlpha(0);
                        this.p1.setAlpha((int) ((this.run * 255.0f) / this.size));
                        this.p.setAlpha((int) (255.0f - ((this.run * 255.0f) / this.size)));
                    } else {
                        this.p1.setAlpha(0);
                        this.p2.setAlpha((int) ((this.run * (-255.0f)) / this.size));
                        this.p.setAlpha((int) (((this.run * 255.0f) / this.size) + 255.0f));
                    }
                }
            } else if (this.touch) {
                float f = this.run;
                if (f == this.size) {
                    this.isDone = true;
                    this.itfAddCall.onAccept();
                } else if (f == (-i)) {
                    this.isDone = true;
                    this.itfAddCall.onReject();
                } else {
                    this.p.setAlpha(255);
                    this.p1.setAlpha(0);
                    this.p2.setAlpha(0);
                    this.touch = false;
                    this.tren = true;
                    this.up = true;
                    this.run = 0.0f;
                    updateRect();
                    this.handler.postDelayed(this.runnable, 800L);
                }
            }
            invalidate();
            return true;
        }

        private void stopAnim() {
            this.handler.removeCallbacks(this.runnable);
        }


        public void updateRect() {
            int i = (int) this.run;
            this.rect.set((getWidth() - this.size) / 2, ((getHeight() - this.size) / 2) - i, (getWidth() + this.size) / 2, ((getHeight() + this.size) / 2) - i);
        }
    }
}
