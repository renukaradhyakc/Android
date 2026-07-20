package com.thelinkphone.app.custom;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.CallDisplayMode;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;

import java.util.ArrayList;


public class ViewModeRecent extends RelativeLayout {
    private int currentMode = CallDisplayMode.ALL;
    private ModeResult modeResult;
    private final boolean theme;
    private final ArrayList<TextW> tabs = new ArrayList<>();
    private final ImageView vRun;

    
    public interface ModeResult {
        void onMode(int mode);
    }

    public void setModeResult(ModeResult modeResult) {
        this.modeResult = modeResult;
    }

    public ViewModeRecent(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 60;
        int tabWidth = (widthScreen * 19) / 100;
        boolean theme = MyShare.getTheme(context);
        this.theme = theme;
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(400L);
        layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        setLayoutTransition(layoutTransition);
        int[] labels = {R.string.all, R.string.missed_call, R.string.blocked_calls_tab};
        int i3 = widthScreen / 200;
        ImageView imageView = new ImageView(context);
        this.vRun = imageView;
        imageView.setPadding(i3, i3, i3, i3);
        addView(imageView, new LayoutParams(tabWidth, -1));

        TextW previous = null;
        for (int idx = 0; idx < labels.length; idx++) {
            TextW tab = new TextW(context);
            tab.setId(View.generateViewId());
            tab.setupText(400, 2.9f);
            tab.setGravity(1);
            tab.setPadding(0, i, 0, i);
            tab.setText(labels[idx]);
            final int modeForTab = idx;
            tab.setOnClickListener(v -> onTabClick(modeForTab));
            tabs.add(tab);

            LayoutParams lp = new LayoutParams(tabWidth, -2);
            if (previous != null) {
                lp.addRule(RelativeLayout.RIGHT_OF, previous.getId());
            }
            addView(tab, lp);
            previous = tab;
        }

        LayoutParams runParams = (LayoutParams) imageView.getLayoutParams();
        runParams.addRule(RelativeLayout.ALIGN_TOP, tabs.get(0).getId());
        runParams.addRule(RelativeLayout.ALIGN_BOTTOM, tabs.get(0).getId());
        runParams.addRule(RelativeLayout.ALIGN_LEFT, tabs.get(0).getId());
        imageView.setLayoutParams(runParams);


        if (theme) {
            float f = widthScreen;
            setBackground(OtherUtils.bgIcon(Color.parseColor("#DCDCDC"), f / 50.0f));
            imageView.setImageDrawable(OtherUtils.bgIcon(-1, f / 60.0f));
            for (TextW tab : tabs) {
                tab.setTextColor(-16777216);
            }
        } else {
            float f2 = widthScreen;
            setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2 / 50.0f));
            imageView.setImageDrawable(OtherUtils.bgIcon(Color.parseColor("#B8B8B8"), f2 / 60.0f));
        }
        updateLayout();
    }

    private void onTabClick(int mode) {
        if (mode == currentMode) return;
        currentMode = mode;
        updateLayout();
        modeResult.onMode(mode);
    }

    private void updateLayout() {
        LayoutParams layoutParams = (LayoutParams) vRun.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.ALIGN_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_LEFT, tabs.get(currentMode).getId());
        vRun.setLayoutParams(layoutParams);

        if (!theme) {
            for (int i = 0; i < tabs.size(); i++) {
                boolean active = (i == currentMode);
                tabs.get(i).setTextColor(active ? Color.parseColor("#2C2C2C") : -1);
            }
        }
    }
}
