package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.utils.OtherUtils;


import java.util.ArrayList;


public class LayoutListSim extends RelativeLayout {
    private LinearLayout llAll;
    private int pos;
    private ViewItem sim1;
    private ViewItem sim2;
    private SimItemClick simItemClick;

    
    public interface SimItemClick {
        void onSimClick(int i);
    }

    public LayoutListSim(Context context) {
        super(context);
        init();
    }

    public LayoutListSim(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public LayoutListSim(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    public void setSimItemClick(SimItemClick simItemClick) {
        this.simItemClick = simItemClick;
    }

    private void init() {
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        LinearLayout linearLayout = new LinearLayout(getContext());
        this.llAll = linearLayout;
        linearLayout.setBackground(OtherUtils.bgIcon(-1, widthScreen / 30.0f));
        this.llAll.setOrientation(LinearLayout.VERTICAL);
        ViewItem viewItem = new ViewItem(getContext());
        this.sim1 = viewItem;
        viewItem.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutListSim.this.m82x90751203(view);
            }
        });
        this.llAll.addView(this.sim1, -1, -2);
        View view = new View(getContext());
        view.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, 1);
        layoutParams.setMargins(widthScreen / 10, 0, 0, 0);
        this.llAll.addView(view, layoutParams);
        ViewItem viewItem2 = new ViewItem(getContext());
        this.sim2 = viewItem2;
        viewItem2.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view2) {
                LayoutListSim.this.m83xb6091b04(view2);
            }
        });
        this.llAll.addView(this.sim2, -1, -2);
        setAlpha(0.0f);
        setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view2) {
                LayoutListSim.this.m84xdb9d2405(view2);
            }
        });
    }

    
    
    public  void m82x90751203(View view) {
        this.pos = 0;
        hideView();
    }

    
    
    public  void m83xb6091b04(View view) {
        this.pos = 1;
        hideView();
    }

    
    
    public  void m84xdb9d2405(View view) {
        this.pos = -1;
        hideView();
    }

    public void setSimInfo(ArrayList<ItemSimInfo> arrayList) {
        if (arrayList.size() > 0) {
            this.sim1.setInfoSim(arrayList.get(0), 1);
        }
        if (arrayList.size() > 1) {
            this.sim2.setInfoSim(arrayList.get(1), 2);
        }
    }

    public void showView(int i) {
        if (this.llAll.getParent() == null) {
            int widthScreen = OtherUtils.getWidthScreen(getContext()) * 13;
            LayoutParams layoutParams = new LayoutParams(widthScreen / 20, -2);
            layoutParams.addRule(14);
            layoutParams.setMargins(0, i, 0, 0);
            addView(this.llAll, layoutParams);
            this.llAll.setPivotX(widthScreen / 40.0f);
            this.llAll.setPivotY(0.0f);
            this.llAll.setScaleX(0.2f);
            this.llAll.setScaleY(0.2f);
        }
        animate().alpha(1.0f).setDuration(400L).start();
        this.llAll.animate().scaleX(1.0f).scaleY(1.0f).setDuration(500L).setInterpolator(new DecelerateInterpolator(2.0f)).withEndAction(null).start();
    }

    public void hideView() {
        animate().alpha(0.0f).setDuration(400L).start();
        this.llAll.animate().scaleX(0.2f).scaleY(0.2f).setDuration(500L).setInterpolator(new DecelerateInterpolator(2.0f)).withEndAction(new Runnable() { 
            @Override 
            public final void run() {
                LayoutListSim.this.m81x6cdea9bd();
            }
        }).start();
    }

    
    
    public  void m81x6cdea9bd() {
        this.simItemClick.onSimClick(this.pos);
    }

    
    
    public class ViewItem extends LinearLayout {
        private final TextW tvName;
        private final TextW tvSim;

        public ViewItem(Context context) {
            super(context);
            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(16);
            int widthScreen = OtherUtils.getWidthScreen(context) / 50;
            setPadding(widthScreen, widthScreen, widthScreen, widthScreen);
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.im_sim);
            int i = widthScreen * 3;
            addView(imageView, i, i);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            LayoutParams layoutParams = new LayoutParams(-1, -2);
            layoutParams.setMargins(i / 2, 0, 0, 0);
            addView(linearLayout, layoutParams);
            TextW textW = new TextW(context);
            this.tvSim = textW;
            textW.setupText(400, 4.0f);
            textW.setTextColor(Color.parseColor("#8A8A8E"));
            linearLayout.addView(textW, -1, -2);
            TextW textW2 = new TextW(context);
            this.tvName = textW2;
            textW2.setupText(400, 2.8f);
            textW2.setTextColor(Color.parseColor("#B8B8B8"));
            linearLayout.addView(textW2, -1, -2);
        }

        public void setInfoSim(ItemSimInfo itemSimInfo, int i) {
            String str = itemSimInfo.phoneNumber;
            if (str == null || str.isEmpty()) {
                str = itemSimInfo.label;
            }
            if (str != null) {
                this.tvName.setText(str);
            }
            this.tvSim.setText("Sim " + i);
        }
    }
}
