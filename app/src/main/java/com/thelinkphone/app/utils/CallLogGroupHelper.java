package com.thelinkphone.app.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.LayoutShowRecent;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.item.ItemRecent;

import java.util.ArrayList;
import java.util.Calendar;

public class CallLogGroupHelper {

    public interface OnViewAllClick {
        void onClick();
    }

    /** Full list, no truncation — used by FragmentCallLogs. */
    public static void addRecentsGroupedByDay(LinearLayout container, Context context,
                                              ArrayList<ItemRecent> arrRecent, boolean theme,
                                              int displayMode, CallBlockReasonResolver blockReasonResolver) {
        addRecentsGroupedByDay(container, context, arrRecent, theme, displayMode, blockReasonResolver, -1, null);
    }

    /** Truncated list with an inline "View all" row — used by FragmentInfo / FragmentInfoAnother. */
    public static void addRecentsGroupedByDay(LinearLayout container, Context context,
                                              ArrayList<ItemRecent> arrRecent, boolean theme,
                                              int displayMode, CallBlockReasonResolver blockReasonResolver,
                                              int maxItems, OnViewAllClick onViewAllClick) {

        ArrayList<ItemRecent> filtered = new ArrayList<>();
        for (ItemRecent recent : arrRecent) {
            if (displayMode == CallDisplayMode.MISSED && recent.type != 3) continue;
            if (displayMode == CallDisplayMode.BLOCKED && recent.type != 6) continue;
            filtered.add(recent);
        }

        boolean truncate = maxItems > 0 && filtered.size() > maxItems;
        int limit = truncate ? maxItems : filtered.size();

        Calendar cal = Calendar.getInstance();
        int lastYear = -1;
        int lastDayOfYear = -1;
        int widthScreen = OtherUtils.getWidthScreen(context);
        int pad = widthScreen / 25;
        float radius = (widthScreen * 3.0f) / 100.0f;
        int bgColor = theme ? -1 : Color.parseColor("#424141");
        LinearLayout currentDayBlock = null;

        for (int i = 0; i < limit; i++) {
            ItemRecent recent = filtered.get(i);
            cal.setTimeInMillis(recent.time);
            int year = cal.get(Calendar.YEAR);
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);

            if (year != lastYear || dayOfYear != lastDayOfYear) {
                currentDayBlock = new LinearLayout(context);
                currentDayBlock.setOrientation(LinearLayout.VERTICAL);
                currentDayBlock.setBackground(OtherUtils.bgIcon(bgColor, radius));
                currentDayBlock.setPadding(pad, pad / 2, pad, pad / 2);

                LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(-1, -2);
                blockParams.setMargins(0, pad, 0, 0);
                container.addView(currentDayBlock, blockParams);

                TextW header = new TextW(context);
                header.setupText(400, 3.3f);
                header.setText(OtherUtils.longToTimeTitle(context, recent.time));
                header.setPadding(0, pad / 2, 0, pad / 2);
                header.setTextColor(theme ? Color.parseColor("#8A8A8E") : Color.parseColor("#F5F5F5"));
                currentDayBlock.addView(header, -1, -2);

                lastYear = year;
                lastDayOfYear = dayOfYear;
            }

            LayoutShowRecent layoutShowRecent = new LayoutShowRecent(context);
            layoutShowRecent.setRecent(recent, theme, blockReasonResolver);
            currentDayBlock.addView(layoutShowRecent, -1, -2);
        }

        if (truncate && currentDayBlock != null && onViewAllClick != null) {
            LinearLayout viewAllRow = new LinearLayout(context);
            viewAllRow.setOrientation(LinearLayout.HORIZONTAL);
            viewAllRow.setGravity(Gravity.CENTER);
            viewAllRow.setPadding(0, pad, 0, pad / 2);
            viewAllRow.setClickable(true);
            viewAllRow.setFocusable(true);

            TextW tvViewAll = new TextW(context);
            tvViewAll.setText(R.string.view_all);
            tvViewAll.setupText(400, 3.5f);
            tvViewAll.setTextColor(Color.parseColor("#007AFF"));
            viewAllRow.addView(tvViewAll, -2, -2);

            ImageView chevron = new ImageView(context);
            chevron.setImageResource(R.drawable.ic_chevron_down);
            chevron.setColorFilter(Color.parseColor("#007AFF"));
            chevron.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            LinearLayout.LayoutParams chevronParams = new LinearLayout.LayoutParams(pad, pad);
            chevronParams.gravity = Gravity.CENTER_VERTICAL;
            chevronParams.setMargins(pad / 4, 0, 0, 0);
            viewAllRow.addView(chevron, chevronParams);

            final LinearLayout finalBlock = currentDayBlock;
            viewAllRow.setOnClickListener(v -> onViewAllClick.onClick());
            finalBlock.addView(viewAllRow, -1, -2);
        }
    }
}