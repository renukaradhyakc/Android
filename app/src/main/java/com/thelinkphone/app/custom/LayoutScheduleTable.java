package com.thelinkphone.app.custom;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.TableSearchUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LayoutScheduleTable extends LinearLayout {

    private final Context ctx;
    private final boolean theme;
    private final String titleColorHex;
    private LinearLayout rowsContainer;
    private HorizontalScrollView hScroll;
    private LinearLayout tableWell;
    private boolean cornersFullyRounded = false;
    private static final int PILL_WIDTH_DP = 96;
    private static final int PILL_HEIGHT_DP = 24;
    private LinearLayout emptyStateView;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private View lastRowView;
    private int lastRowBgColor;
    private LayoutSearchBar searchBar;
    private List<String> allHeaders;
    private List<RowEntry> allRowEntries = new ArrayList<>();
    private Runnable searchInteractionListener;


    private static final class RowEntry {
        final List<String> cells;
        final boolean badged;

        RowEntry(List<String> cells, boolean badged) {
            this.cells = cells;
            this.badged = badged;
        }
    }

    public LayoutScheduleTable(Context context, boolean theme, String title) {
        this(context, theme, title, "#007AFF");
    }

    public LayoutScheduleTable(Context context, boolean theme, String title, String titleColorHex) {
        super(context);
        this.ctx = context;
        this.theme = theme;
        setOrientation(VERTICAL);
        this.titleColorHex = titleColorHex;
        buildCard(title);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }

    private void buildCard(String title) {
        int cardColor = theme ? Color.WHITE : Color.parseColor("#2C2C2E");

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardColor);
        bg.setCornerRadius(dp(14));
        setBackground(bg);
        setElevation(dp(6));
        setClipToOutline(true);
        setPadding(dp(14), dp(14), 0, dp(14));

        LinearLayout titleRow = new LinearLayout(ctx);
        titleRow.setOrientation(HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams titleRowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        titleRowParams.bottomMargin = dp(10);
        addView(titleRow, titleRowParams);

        TextView tvTitle = new TextView(ctx);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.parseColor(titleColorHex));
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams tvTitleParams = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT);
        tvTitleParams.weight = 1;
        titleRow.addView(tvTitle, tvTitleParams);

        searchBar = new LayoutSearchBar(ctx, theme);
        searchBar.setOnQueryChangeListener(this::applyFilter);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(dp(130), dp(34));
        searchParams.rightMargin = dp(14);
        searchBar.setVisibility(GONE);
        titleRow.addView(searchBar, searchParams);

        int wellColor = theme ? Color.parseColor("#F4F5F9") : Color.parseColor("#1F1F21");
        tableWell = new LinearLayout(ctx);
        tableWell.setOrientation(VERTICAL);
        GradientDrawable wellBg = new GradientDrawable();
        wellBg.setColor(wellColor);
        wellBg.setCornerRadius(dp(10));
        tableWell.setBackground(wellBg);
//        tableWell.setClipToOutline(true);
        addView(tableWell, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        hScroll = new HorizontalScrollView(ctx);
        hScroll.setFillViewport(false);
        hScroll.setHorizontalScrollBarEnabled(false);
        hScroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

        LinearLayout scrollContent = new LinearLayout(ctx);
        scrollContent.setOrientation(LinearLayout.HORIZONTAL);

        rowsContainer = new LinearLayout(ctx);
        rowsContainer.setOrientation(VERTICAL);
        scrollContent.addView(rowsContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        View trailingGutter = new View(ctx);
        trailingGutter.setLayoutParams(new LinearLayout.LayoutParams(dp(14), LinearLayout.LayoutParams.MATCH_PARENT));
        scrollContent.addView(trailingGutter);

        hScroll.addView(scrollContent);
        tableWell.addView(hScroll, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        emptyStateView = buildEmptyState();
        LayoutParams emptyParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        emptyParams.rightMargin = dp(14);
        tableWell.addView(emptyStateView, emptyParams);


        hScroll.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> updateCardCorners());
        hScroll.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (hScroll.getWidth() > 0) {
                    updateCardCorners();
                    hScroll.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private LinearLayout buildEmptyState() {
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(16), dp(20), dp(16), dp(20));
        root.setVisibility(GONE);

        GradientDrawable emptyBg = new GradientDrawable();
        emptyBg.setColor(Color.parseColor("#FFFFFF"));
        emptyBg.setCornerRadius(dp(10));
        root.setBackground(emptyBg);

        int titleColor = Color.parseColor("#1C1C1E");
        int subtitleColor = Color.parseColor("#6C757D");

        tvEmptyTitle = new TextView(ctx);
        tvEmptyTitle.setTextColor(titleColor);
        tvEmptyTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tvEmptyTitle.setTypeface(tvEmptyTitle.getTypeface(), Typeface.BOLD);
        tvEmptyTitle.setGravity(Gravity.CENTER);
        root.addView(tvEmptyTitle);

        tvEmptySubtitle = new TextView(ctx);
        tvEmptySubtitle.setTextColor(subtitleColor);
        tvEmptySubtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        tvEmptySubtitle.setGravity(Gravity.CENTER);
        LayoutParams subParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(4);
        root.addView(tvEmptySubtitle, subParams);

        return root;
    }

    public void setEmptyState(String title, String subtitle) {
        hScroll.setVisibility(GONE);
        emptyStateView.setVisibility(VISIBLE);
        tvEmptyTitle.setText(title);
        tvEmptySubtitle.setText(subtitle);
        forceFullyRoundedCard();
        setOuterRightMarginSymmetric(true);

        allHeaders = null;
        allRowEntries = new ArrayList<>();
        setSearchVisible(false);
    }

    private void showNoResults() {
        hScroll.setVisibility(GONE);
        emptyStateView.setVisibility(VISIBLE);
        tvEmptyTitle.setText("No matches found");
        tvEmptySubtitle.setText("Try a different search term.");
        forceFullyRoundedCard();
        setOuterRightMarginSymmetric(true);
    }

    private void setSearchVisible(boolean visible) {
        searchBar.setVisibility(visible ? VISIBLE : GONE);
        if (!visible) {
            searchBar.clear();
        }
    }

    private void forceFullyRoundedCard() {
        cornersFullyRounded = true;
        int cardColor = theme ? Color.parseColor("#EFF3F7") : Color.parseColor("#2C2C2E");
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardColor);
        bg.setCornerRadius(dp(14));
        setBackground(bg);
    }

    private void setOuterRightMarginSymmetric(boolean symmetric) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            mlp.rightMargin = symmetric ? mlp.leftMargin : 0;
            setLayoutParams(mlp);
        }
    }

    private void updateCardCorners() {
        boolean atEnd = hScroll.getScrollX() + hScroll.getWidth() >= rowsContainer.getWidth();
        boolean noScrollNeeded = rowsContainer.getWidth() <= hScroll.getWidth();
        boolean shouldBeFullyRounded = atEnd || noScrollNeeded;

        if (shouldBeFullyRounded == cornersFullyRounded) return;
        cornersFullyRounded = shouldBeFullyRounded;
        setOuterRightMarginSymmetric(shouldBeFullyRounded);
        int cardColor = theme ? Color.parseColor("#EFF3F7") : Color.parseColor("#2C2C2E");
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardColor);
        float r = dp(14);
        if (shouldBeFullyRounded) {
            bg.setCornerRadius(r);
        } else {
            bg.setCornerRadii(new float[]{r, r, 0, 0, 0, 0, r, r});
        }
        setBackground(bg);

        if (lastRowView != null) {
            lastRowView.setBackground(bottomRowDrawable(lastRowBgColor, shouldBeFullyRounded));
        }
    }

    public void setData(List<String> headers, List<List<String>> rows) {
        setData(headers, rows, null);
    }

    public void setData(List<String> headers, List<List<String>> rows, Set<Integer> badgedRowIndices) {
        allHeaders = headers;
        allRowEntries = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            boolean badged = badgedRowIndices != null && badgedRowIndices.contains(i);
            allRowEntries.add(new RowEntry(rows.get(i), badged));
        }

        setSearchVisible(!rows.isEmpty());
        renderRows(allHeaders, allRowEntries);
    }

    private void applyFilter(String query) {
        if (allHeaders == null) return;

        if (query == null || query.trim().isEmpty()) {
            renderRows(allHeaders, allRowEntries);
            return;
        }

        List<RowEntry> filtered = new ArrayList<>();
        for (RowEntry entry : allRowEntries) {
            if (TableSearchUtils.matches(entry.cells, query)) {
                filtered.add(entry);
            }
        }

        if (filtered.isEmpty()) {
            showNoResults();
        } else {
            renderRows(allHeaders, filtered);
        }
    }

    private void renderRows(List<String> headers, List<RowEntry> entries) {
        List<List<String>> rows = new ArrayList<>();
        Set<Integer> badgedRowIndices = new HashSet<>();
        for (int i = 0; i < entries.size(); i++) {
            rows.add(entries.get(i).cells);
            if (entries.get(i).badged) badgedRowIndices.add(i);
        }
        renderTable(headers, rows, badgedRowIndices);
    }

    private void renderTable(List<String> headers, List<List<String>> rows, Set<Integer> badgedRowIndices) {
        Log.d("PILL_ALIGN", "setData called with rows.size()=" + rows.size());
        hScroll.setVisibility(VISIBLE);
        setOuterRightMarginSymmetric(false);
        emptyStateView.setVisibility(GONE);

        rowsContainer.removeAllViews();

        int textColor = theme ? Color.parseColor("#6C757D") : Color.parseColor("#F2F2F7");
        int mutedColor = theme ? Color.parseColor("#6C757D") : Color.parseColor("#C7C7CC");
        int borderColor = theme ? Color.parseColor("#E9ECEF") : Color.parseColor("#565658");
        int rowBg = theme ? Color.WHITE : Color.parseColor("#2C2C2E");
        int headerBg = theme ? Color.parseColor("#E9EDFB") : Color.parseColor("#26324A");
        int zebraBg = theme ? Color.parseColor("#F8F9FA") : Color.parseColor("#333335");

        int[] colWidths = computeColumnWidths(headers, rows, badgedRowIndices);

        LinearLayout headerRow = buildRow(headers, mutedColor, true, headers, colWidths, false);
        headerRow.setBackground(roundedTop(headerBg));
        rowsContainer.addView(headerRow);
        rowsContainer.addView(divider(borderColor));

        for (int i = 0; i < rows.size(); i++) {
            boolean showBadge = badgedRowIndices != null && badgedRowIndices.contains(i);
            LinearLayout row = buildRow(rows.get(i), textColor, false, headers, colWidths, showBadge);
            int bgColor = i % 2 == 1 ? zebraBg : rowBg;
            if (i == rows.size() - 1) {
                lastRowView = row;
                lastRowBgColor = bgColor;
                row.setBackground(bottomRowDrawable(bgColor, cornersFullyRounded));
            } else {
                row.setBackgroundColor(bgColor);
            }

            rowsContainer.addView(row);
            if (i < rows.size() - 1) {
                rowsContainer.addView(divider(borderColor));
            }
        }
        rowsContainer.post(this::updateCardCorners);
        rowsContainer.post(() -> logPillAlignment(headers));
        rowsContainer.post(() -> {
            if (searchBar.isSearchActive() && searchInteractionListener != null) {
                searchInteractionListener.run();
            }
        });
    }

    private void logPillAlignment(List<String> headers) {
        android.util.Log.d("PILL_ALIGN", "post fired, rowsContainer.getChildCount()=" + rowsContainer.getChildCount());
        int pillColIndex = -1;
        for (int i = 0; i < headers.size(); i++) {
            if (isPillColumn(headers.get(i))) { pillColIndex = i; break; }
        }
        if (pillColIndex == -1) return;

        int[] loc = new int[2];
        for (int r = 0; r < rowsContainer.getChildCount(); r++) {
            View child = rowsContainer.getChildAt(r);
            if (!(child instanceof LinearLayout)) continue; // skips the 1dp divider views
            LinearLayout row = (LinearLayout) child;
            if (pillColIndex >= row.getChildCount()) continue;

            View pillCell = row.getChildAt(pillColIndex);
            pillCell.getLocationOnScreen(loc);
            int left = loc[0];
            int right = loc[0] + pillCell.getWidth();
            android.util.Log.d("PILL_ALIGN", "childIdx=" + r + " left=" + left + " right=" + right + " width=" + pillCell.getWidth());
        }
    }

    private int[] computeColumnWidths(List<String> headers, List<List<String>> rows, Set<Integer> badgedRowIndices) {
        int colCount = headers.size();
        int[] widths = new int[colCount];
        TextPaint paint = new TextPaint();
        float headerSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12.5f, ctx.getResources().getDisplayMetrics());
        float cellSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, ctx.getResources().getDisplayMetrics());
        int badgeReserve = dp(16) + dp(12);

        for (int col = 0; col < colCount; col++) {
            String columnName = headers.get(col);
            if (isPillColumn(columnName)) {
                widths[col] = dp(PILL_WIDTH_DP) + dp(12) * 2;
                continue;
            }

            paint.setTextSize(headerSizePx);
            paint.setFakeBoldText(true);
            float max = paint.measureText(columnName.toUpperCase());

            paint.setFakeBoldText(false);
            paint.setTextSize(cellSizePx);
            boolean isName = isNameColumn(columnName);
            for (int r = 0; r < rows.size(); r++) {
                List<String> row = rows.get(r);
                if (col >= row.size()) continue;
                float rowTextWidth = paint.measureText(row.get(col));
                if (isName && badgedRowIndices != null && badgedRowIndices.contains(r)) {
                    rowTextWidth += badgeReserve;
                }
                max = Math.max(max, rowTextWidth);
            }

            int contentWidth = (int) max + dp(12) + dp(12);
            widths[col] = Math.max(contentWidth, dp(120));
        }
        return widths;
    }

    private LinearLayout buildRow(List<String> cells, int color, boolean isHeader, List<String> headers, int[] colWidths, boolean showBadge) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(HORIZONTAL);
        row.setPadding(0, dp(10), 0, dp(10));
        row.setClipChildren(false);

        for (int col = 0; col < cells.size(); col++) {
            String cellText = cells.get(col);
            String columnName = (headers != null && col < headers.size()) ? headers.get(col) : "";
            int width = (colWidths != null && col < colWidths.length) ? colWidths[col] : dp(120);

            if (!isHeader && isPillColumn(columnName)) {
                row.addView(buildPillCellContainer(cellText, width));
                continue;
            }

            if (!isHeader && isLinkColumn(columnName) && !"-".equals(cellText)) {
                TextView linkTv = buildLinkCell(cellText, color);
                linkTv.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));
                row.addView(linkTv);
                continue;
            }

            if (!isHeader && isNumberColumn(columnName) && !"-".equals(cellText)) {
                row.addView(buildNumberCell(cellText, width));
                continue;
            }

            if (!isHeader && isEmailColumn(columnName) && !"-".equals(cellText)) {
                row.addView(buildEmailCell(cellText, width));
                continue;
            }

            if (!isHeader && isNameColumn(columnName)) {
                row.addView(buildNameCell(cellText, color, width, showBadge));
                continue;
            }

            TextView tv = new TextView(ctx);
            tv.setText(cellText);
            tv.setTextColor(color);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, isHeader ? 12.5f : 14f);
            if (isHeader) {
                tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
                tv.setAllCaps(true);
            }
            tv.setGravity("-".equals(cellText) ? Gravity.CENTER : Gravity.CENTER_VERTICAL);
            tv.setMaxLines(1);
            tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tv.setPadding(dp(12), 0, dp(12), 0);
            row.addView(tv, new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        return row;
    }

    private LinearLayout buildPillCellContainer(String value, int width) {
        LinearLayout cell = new LinearLayout(ctx);
        cell.setOrientation(HORIZONTAL);
        cell.setGravity(Gravity.CENTER_VERTICAL);
        cell.setPadding(dp(12), 0, dp(12), 0);
        cell.addView(buildPillCell(value));
        cell.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT)); // fixed width, not minimumWidth
        return cell;
    }

    private boolean isPillColumn(String columnName) {
        return "Status".equalsIgnoreCase(columnName) || "Event Type".equalsIgnoreCase(columnName);
    }

    private boolean isLinkColumn(String columnName) {
        return "Link".equalsIgnoreCase(columnName);
    }

    private boolean isNameColumn(String columnName) {
        return "Name".equalsIgnoreCase(columnName);
    }

    private boolean isNumberColumn(String columnName) {
        return "Number".equalsIgnoreCase(columnName);
    }

    private boolean isEmailColumn(String columnName) {
        return "Email".equalsIgnoreCase(columnName);
    }

    private TextView buildLinkCell(String url, int color) {
        TextView tv = new TextView(ctx);
        tv.setText(url);
        tv.setTextColor(Color.parseColor("#007AFF"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tv.setSingleLine(true);
        tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setPadding(dp(12), 0, dp(12), 0);

        tv.setOnClickListener(v -> {
            try {
                ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(ctx, "No app found to open this link", Toast.LENGTH_SHORT).show();
            }
        });
        tv.setOnLongClickListener(v -> {
            OtherUtils.copyToClip(ctx, url, "callalink_link", R.string.link_copied);
            return true;
        });
        return tv;
    }

    private TextView buildNumberCell(String number, int width) {
        TextView tv = new TextView(ctx);
        tv.setText(number);
        tv.setTextColor(Color.parseColor("#007AFF"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tv.setSingleLine(true);
        tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setPadding(dp(12), 0, dp(12), 0);
        tv.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));

        tv.setOnClickListener(v -> {
            try {
                ctx.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(ctx, "No dialer app found", Toast.LENGTH_SHORT).show();
            }
        });
        tv.setOnLongClickListener(v -> {
            OtherUtils.copyToClip(ctx, number, "callalink_number", R.string.number_copied);
            return true;
        });
        return tv;
    }

    private TextView buildEmailCell(String email, int width) {
        TextView tv = new TextView(ctx);
        tv.setText(email);
        tv.setTextColor(Color.parseColor("#007AFF"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tv.setSingleLine(true);
        tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setPadding(dp(12), 0, dp(12), 0);
        tv.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));

        tv.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                ctx.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(ctx, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
        tv.setOnLongClickListener(v -> {
            OtherUtils.copyToClip(ctx, email, "callalink_email", R.string.email_copied);
            return true;
        });
        return tv;
    }

    private FrameLayout buildNameCell(String name, int color, int width, boolean showBadge) {
        FrameLayout cell = new FrameLayout(ctx);
        cell.setClipChildren(false);
        cell.setPadding(dp(12), 0, dp(12), 0);
        cell.setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tv = new TextView(ctx);
        tv.setText(name);
        tv.setTextColor(color);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
        tv.setMaxLines(1);
        tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        tvParams.gravity = "-".equals(name) ? Gravity.CENTER : (Gravity.START | Gravity.CENTER_VERTICAL);
        cell.addView(tv, tvParams);

        if (showBadge) {
            ImageView badge = new ImageView(ctx);
            badge.setImageResource(R.drawable.ic_verified);
            int badgeSize = dp(16);
            FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(badgeSize, badgeSize);
            badgeParams.gravity = Gravity.CENTER_VERTICAL;
            cell.addView(badge, badgeParams);

            tv.post(() -> {
                float textWidthPx = tv.getPaint().measureText(name);
                badgeParams.leftMargin = dp(4) + (int) textWidthPx;
                badge.setLayoutParams(badgeParams);
            });
        }

        return cell;
    }

    private TextView buildLegacySolidPill(String value) {
        TextView pill = new TextView(ctx);
        pill.setText(value);
        pill.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        pill.setTypeface(pill.getTypeface(), android.graphics.Typeface.BOLD);
        pill.setPadding(dp(14), dp(4), dp(14), dp(4));
        pill.setMinWidth(dp(90));
        pill.setGravity(Gravity.CENTER);

        int bg;
        int fg = Color.WHITE;
        String v = value == null ? "" : value.trim().toLowerCase();
        switch (v) {
            case "booked":
                bg = Color.parseColor("#0AC074");
                break;
            case "cancelled":
                bg = Color.parseColor("#F62947");
                break;
            case "free":
                bg = Color.parseColor("#0099FB");
                break;
            default:
                bg = theme ? Color.parseColor("#ADB5BD") : Color.parseColor("#5C5C5E");
                fg = theme ? Color.parseColor("#1C1C1E") : Color.WHITE;
                break;
        }
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(bg);
        pillBg.setCornerRadius(dp(100));
        pill.setBackground(pillBg);
        pill.setTextColor(fg);
        return pill;
    }

    private View buildPillCell(String value) {
        String v = value == null ? "" : value.trim().toLowerCase();
        switch (v) {
            case "active":
                return buildStatusBadge("Active", R.drawable.bg_badge_green, R.drawable.dot_green, Color.parseColor("#00BFA6"));
            case "upcoming":
                return buildStatusBadge("Upcoming", R.drawable.bg_badge_yellow, R.drawable.dot_yellow, Color.parseColor("#FFB800"));
            case "completed":
                return buildStatusBadge("Completed", R.drawable.bg_badge_red, R.drawable.dot_red, Color.parseColor("#FF4D4D"));
            case "cancelled":
                return buildStatusBadge("Cancelled", R.drawable.bg_badge_red, R.drawable.dot_red, Color.parseColor("#FF4D4D"));
            default:
                return buildLegacySolidPill(value);
        }
    }

    private LinearLayout buildStatusBadge(String label, int bgRes, int dotRes, int textColor) {
        LinearLayout badge = new LinearLayout(ctx);
        badge.setOrientation(HORIZONTAL);
        badge.setGravity(Gravity.CENTER);
        badge.setBackgroundResource(bgRes);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(PILL_WIDTH_DP), dp(PILL_HEIGHT_DP));
        badge.setLayoutParams(badgeParams);

        View dot = new View(ctx);
        dot.setBackgroundResource(dotRes);
        LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(6), dp(6));
        dotParams.setMarginEnd(dp(4));
        badge.addView(dot, dotParams);

        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextColor(textColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
        tv.setMaxLines(1);
        tv.setEllipsize(android.text.TextUtils.TruncateAt.END);
        badge.addView(tv);

        return badge;
    }

    private LinearLayout buildPillCellContainer(String value) {
        LinearLayout cell = new LinearLayout(ctx);
        cell.setOrientation(HORIZONTAL);
        cell.setGravity(Gravity.CENTER_VERTICAL);
        cell.setMinimumWidth(dp(120));
        cell.setPadding(dp(12), 0, dp(12), 0);
        cell.addView(buildPillCell(value));
        return cell;
    }

    private GradientDrawable roundedTop(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        float r = dp(10);
        gd.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return gd;
    }

    private GradientDrawable roundedBottom(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        float r = dp(10);
        gd.setCornerRadii(new float[]{0, 0, 0, 0, r, r, r, r});
        return gd;
    }

    private GradientDrawable plainRect(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        return gd;
    }

    private GradientDrawable bottomRowDrawable(int color, boolean rightRounded) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        float r = dp(10);
        // order: top-left x,y, top-right x,y, bottom-right x,y, bottom-left x,y
        float br = rightRounded ? r : 0;
        gd.setCornerRadii(new float[]{0, 0, 0, 0, br, br, r, r});
        return gd;
    }

    private View divider(int color) {
        View v = new View(ctx);
        v.setBackgroundColor(color);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(1));
        v.setLayoutParams(lp);
        return v;
    }

    public void setOnSearchInteractionListener(Runnable listener) {
        this.searchInteractionListener = listener;
        searchBar.setOnSearchInteractionListener(listener);
    }

    public View getSearchBarView() {
        return searchBar;
    }

    public void resetSearch() {
        if (allHeaders != null) {
            renderRows(allHeaders, allRowEntries);
        }
        searchBar.clear();
    }
}