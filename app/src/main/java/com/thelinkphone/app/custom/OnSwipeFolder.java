package com.thelinkphone.app.custom;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class OnSwipeFolder implements View.OnTouchListener {
    private float disX;
    private boolean flagScroll;
    private boolean flagTapUp;
    private final GestureDetector gestureDetector;
    private float oldX;
    private final TouchResult touchResult;
    private float value;
    private final int w;
    private final Runnable runnable = new Runnable() { 
        @Override 
        public void run() {
            if (OnSwipeFolder.this.flagTapUp) {
                return;
            }
            OnSwipeFolder.this.flagTapUp = true;
            OnSwipeFolder.this.h.postDelayed(this, 250L);
            if (OnSwipeFolder.this.flagScroll) {
                if (OnSwipeFolder.this.value > OnSwipeFolder.this.w / 2.0f) {
                    OnSwipeFolder.this.touchResult.onSwipeRight();
                } else if (OnSwipeFolder.this.value < (-OnSwipeFolder.this.w) / 2.0f) {
                    OnSwipeFolder.this.touchResult.onSwipeLeft();
                } else {
                    OnSwipeFolder.this.touchResult.onCancel();
                }
            }
        }
    };
    private final Handler h = new Handler();

    
    public interface TouchResult {
        void onCancel();

        void onClick();

        void onLongClick();

        void onMoveHorizontal(float f);

        void onSwipeLeft();

        void onSwipeRight();
    }

    public OnSwipeFolder(Context context, TouchResult touchResult) {
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        this.touchResult = touchResult;
        this.w = context.getResources().getDisplayMetrics().widthPixels / 2;
    }

    @Override 
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.flagTapUp = false;
            this.h.removeCallbacks(this.runnable);
            this.oldX = motionEvent.getRawX();
            this.flagScroll = false;
            this.value = 0.0f;
        } else if (motionEvent.getAction() == 1) {
            this.h.postDelayed(this.runnable, 100L);
        } else if (motionEvent.getAction() == 2) {
            this.disX = motionEvent.getRawX() - this.oldX;
            this.oldX = motionEvent.getRawX();
        }
        return this.gestureDetector.onTouchEvent(motionEvent);
    }

    
    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 20;

        @Override 
        public boolean onDown(MotionEvent motionEvent) {
            return true;
        }

        private GestureListener() {
        }

        @Override 
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            if (!OnSwipeFolder.this.flagScroll) {
                OnSwipeFolder.this.flagScroll = true;
            }
            OnSwipeFolder.this.value = motionEvent2.getRawX() - motionEvent.getRawX();
            OnSwipeFolder.this.touchResult.onMoveHorizontal(OnSwipeFolder.this.value);
            return super.onScroll(motionEvent, motionEvent2, f, f2);
        }

        @Override 
        public void onLongPress(MotionEvent motionEvent) {
            super.onLongPress(motionEvent);
            OnSwipeFolder.this.touchResult.onLongClick();
        }

        @Override 
        public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
            OnSwipeFolder.this.touchResult.onClick();
            return super.onSingleTapConfirmed(motionEvent);
        }

        @Override 
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent2, float f, float f2) {
            OnSwipeFolder.this.flagScroll = false;
            float rawX = motionEvent2.getRawX() - motionEvent.getRawX();
            if (Math.abs(rawX) <= 20.0f) {
                OnSwipeFolder.this.touchResult.onCancel();
                return false;
            }
            if (rawX > 0.0f) {
                if (OnSwipeFolder.this.disX >= 0.0f) {
                    OnSwipeFolder.this.touchResult.onSwipeRight();
                } else {
                    OnSwipeFolder.this.touchResult.onCancel();
                }
            } else if (OnSwipeFolder.this.disX <= 0.0f) {
                OnSwipeFolder.this.touchResult.onSwipeLeft();
            } else {
                OnSwipeFolder.this.touchResult.onCancel();
            }
            return true;
        }
    }
}
