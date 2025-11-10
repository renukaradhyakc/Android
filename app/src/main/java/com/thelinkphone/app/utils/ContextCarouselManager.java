package com.thelinkphone.app.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.adapter.AdapterContextAndContent;
import com.thelinkphone.app.model.ContextAndContent;

import java.util.List;

public class ContextCarouselManager {

    private final RecyclerView recyclerView;
    private final List<ContextAndContent> dataList;
    private final AdapterContextAndContent adapter;
    private final Context context;

    private final int minimumItemsForEffects = 5;
    private final int scrollDelay = 2500;
    private int currentPosition = 0;
    private boolean isUserScrolling = false;

    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private PagerSnapHelper snapHelper;

    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (adapter == null || recyclerView == null) return;
            if (dataList.size() < minimumItemsForEffects) return;
            if (isUserScrolling) {
                autoScrollHandler.postDelayed(this, scrollDelay);
                return;
            }

            currentPosition++;
            int nextPosition = currentPosition % adapter.getItemCount();
            smoothScrollToPositionWithSpeed(recyclerView, nextPosition, 250f);
            Log.d("CAROUSEL", "→ Smooth scroll to " + nextPosition);

            autoScrollHandler.postDelayed(this, scrollDelay);
        }
    };

    public ContextCarouselManager(Context context, RecyclerView recyclerView, List<ContextAndContent> dataList) {
        this.recyclerView = recyclerView;
        this.dataList = dataList;
        this.context = context;

        this.adapter = new AdapterContextAndContent(context, dataList);
        initRecycler();
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setClipToPadding(false);
        recyclerView.setClipChildren(false);
        recyclerView.setAdapter(adapter);

        boolean enableEffects = dataList.size() >= minimumItemsForEffects;

        if (enableEffects) {
            setupCarouselEffects();
        } else {
            setupSimpleLayout();
        }
    }

    private void setupCarouselEffects() {
        snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        recyclerView.post(() -> {
            centerFirstItem();
            currentPosition = dataList.size() * 100;
            recyclerView.scrollToPosition(currentPosition);

            recyclerView.postDelayed(() -> {
                applyCarouselEffect(recyclerView);
                autoScrollHandler.postDelayed(autoScrollRunnable, scrollDelay);
                Log.d("CAROUSEL", "✓ Carousel started");
            }, 300);
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int lastSnappedPosition = RecyclerView.NO_POSITION;

            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                super.onScrolled(rv, dx, dy);
                applyCarouselEffect(rv);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                super.onScrollStateChanged(rv, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserScrolling = true;
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isUserScrolling = false;

                    View snapView = snapHelper.findSnapView(rv.getLayoutManager());
                    if (snapView != null) {
                        int pos = rv.getLayoutManager().getPosition(snapView);
                        if (pos != lastSnappedPosition) {
                            currentPosition = pos;
                            lastSnappedPosition = pos;
                            Log.d("CAROUSEL", "Snapped to " + pos);
                        }
                    }

                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                    autoScrollHandler.postDelayed(autoScrollRunnable, scrollDelay);
                }
            }
        });
    }

    private void setupSimpleLayout() {
        recyclerView.setLayoutFrozen(true);
        recyclerView.setNestedScrollingEnabled(false);
    }

    private void centerFirstItem() {
        View firstItem = recyclerView.getLayoutManager().findViewByPosition(0);
        if (firstItem != null) {
            int itemWidth = firstItem.getMeasuredWidth();
            int recyclerWidth = recyclerView.getWidth();
            int sidePadding = (recyclerWidth - itemWidth) / 2;
            recyclerView.setPadding(sidePadding, 0, sidePadding, 0);
            recyclerView.setClipToPadding(false);
        }
    }

    private void applyCarouselEffect(RecyclerView rv) {
        if (rv == null || rv.getChildCount() == 0) return;

        int midpoint = rv.getWidth() / 2;
        float d1 = 1.2f * midpoint;
        float s0 = 1f;
        float s1 = 0.6f;

        for (int i = 0; i < rv.getChildCount(); i++) {
            View child = rv.getChildAt(i);
            float childMid = (child.getLeft() + child.getRight()) / 2f;
            float d = Math.abs(midpoint - childMid);
            float scale = s0 + (s1 - s0) * (d / d1);
            scale = Math.max(s1, Math.min(scale, s0));

            child.setScaleX(scale);
            child.setScaleY(scale);
            child.setAlpha(0.6f + (scale - s1));
            child.setTranslationY((1 - scale) * 80);
        }
    }

    private void smoothScrollToPositionWithSpeed(RecyclerView rv, int position, float speedFactor) {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(context) {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics dm) {
                return speedFactor / dm.densityDpi;
            }
        };
        smoothScroller.setTargetPosition(position);
        rv.getLayoutManager().startSmoothScroll(smoothScroller);
    }

    public void startAutoScroll() {
        if (dataList.size() >= minimumItemsForEffects) {
            autoScrollHandler.postDelayed(autoScrollRunnable, scrollDelay);
        }
    }

    public void stopAutoScroll() {
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    public void release() {
        stopAutoScroll();
        recyclerView.setAdapter(null);
        recyclerView.clearOnScrollListeners();
    }

    public void setAvatarNameColor(int color) {
        if (adapter != null) {
            adapter.setAvatarNameColor(color);
        }
    }
}
