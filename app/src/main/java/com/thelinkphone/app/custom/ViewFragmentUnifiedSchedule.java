package com.thelinkphone.app.custom;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemEventScheduleRow;
import com.thelinkphone.app.item.ItemPartyDisplay;
import com.thelinkphone.app.item.ItemPhoneScheduleRow;
import com.thelinkphone.app.item.ItemScheduleEmptyState;
import com.thelinkphone.app.item.ItemUnifiedSchedule;
import com.thelinkphone.app.model.PartyPayload;
import com.thelinkphone.app.model.UnifiedScheduleData;
import com.thelinkphone.app.model.UnifiedScheduleEntry;
import com.thelinkphone.app.model.UnifiedScheduleResponse;
import com.thelinkphone.app.repository.ContactLookupRepository;
import com.thelinkphone.app.repository.UnifiedScheduleRepository;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.ScheduleAvailabilityUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewFragmentUnifiedSchedule extends RelativeLayout {

    private static final String DIRECTION_BY_ME = UnifiedScheduleEntry.DIRECTION_GIVEN_BY_ME;
    private static final String DIRECTION_TO_ME = UnifiedScheduleEntry.DIRECTION_GIVEN_TO_ME;

    private static final SimpleDateFormat FILTER_KEY_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private final Context ctx;
    private final boolean theme;
    private ScrollView scrollView;
    private CompactCalendarView calendarView;
    private LayoutScheduleTable eventTable;
    private LayoutScheduleTable phoneTable;
    private String currentDirection = DIRECTION_BY_ME;
    private UnifiedScheduleData lastData;
    private Calendar filterDate;
    private View pendingScrollTarget;
    private int closedWindowHeight = 0;
    private boolean keyboardOpen = false;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardListener;
    private View keyboardSpacer;
    private int visibleBottom = 0;
    private static final String CACHE_PREFS = "unified_schedule_cache";
    private static final String CACHE_KEY_DATA = "cached_data_json";
    private static final String CACHE_KEY_TS = "cached_timestamp";
    private static final long CACHE_TTL_MS = 5 * 60 * 1000;
    private static UnifiedScheduleData cachedData;
    private static long cacheTimestamp = 0;
    private static final String PREF_HINT_DISMISSED_BY_ME = "unified_schedule_hint_dismissed_by_me";
    private static final String PREF_HINT_DISMISSED_TO_ME = "unified_schedule_hint_dismissed_to_me";
    private LinearLayout hintBanner;
    private TextView tvHint;
    private SharedPreferences appPrefs;

    public ViewFragmentUnifiedSchedule(Context context) {
        super(context);
        this.ctx = context;
        this.theme = MyShare.getTheme(context);
        build();
    }

    public void resetSearches() {
        eventTable.resetSearch();
        phoneTable.resetSearch();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }

    private void build() {
        scrollView = new ScrollView(ctx);
        scrollView.setFillViewport(true);
        scrollView.setClipChildren(false);
        scrollView.setClipToPadding(false);
        addView(scrollView, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        LinearLayout container = new LinearLayout(ctx);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(dp(8), dp(12), dp(8), dp(12));
        container.setClipChildren(false);
        scrollView.addView(container, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        // ---- Title (matches Settings screen styling, centered) ----
        TextView tvTitle = new android.widget.TextView(ctx);
        tvTitle.setText("Schedules");
        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        tvTitle.setTypeface(tvTitle.getTypeface(), android.graphics.Typeface.BOLD);
        tvTitle.setTextColor(theme ? Color.parseColor("#1C1C1E") : Color.WHITE);
        tvTitle.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = dp(20);
        container.addView(tvTitle, titleParams);

        // ---- Direction toggle: same component/styling as everywhere else, just relabeled ----
        ViewModeRecent modeToggle = new ViewModeRecent(ctx, new int[]{R.string.given_by_me, R.string.given_to_me});
        modeToggle.setTabTextSize(3.3f);
        modeToggle.setModeResult(mode -> setDirection(mode == 0 ? DIRECTION_BY_ME : DIRECTION_TO_ME));
        LinearLayout.LayoutParams toggleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        toggleParams.bottomMargin = dp(12);
        container.addView(modeToggle, toggleParams);

        appPrefs = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        hintBanner = new LinearLayout(ctx);
        hintBanner.setOrientation(LinearLayout.HORIZONTAL);
        hintBanner.setGravity(Gravity.CENTER_VERTICAL);
        hintBanner.setPadding(dp(12), dp(10), dp(12), dp(10));

        GradientDrawable hintBg = new GradientDrawable();
        hintBg.setColor(theme ? Color.parseColor("#FBF3D9") : Color.parseColor("#3A3626"));
        hintBg.setCornerRadius(dp(10));
        hintBanner.setBackground(hintBg);

        tvHint = new TextView(ctx);
        tvHint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        tvHint.setGravity(Gravity.CENTER);
        tvHint.setTextColor(theme ? Color.parseColor("#3C3C43") : Color.parseColor("#C7C7CC"));
        LinearLayout.LayoutParams tvHintParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvHintParams.weight = 1;
        hintBanner.addView(tvHint, tvHintParams);

        ImageView ivClose = new ImageView(ctx);
        ivClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        ivClose.setColorFilter(theme ? Color.parseColor("#8A8A8E") : Color.parseColor("#B8B8B8"));
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(dp(18), dp(18));
        closeParams.setMarginStart(dp(10));
        ivClose.setOnClickListener(v -> {
            hintBanner.setVisibility(View.GONE);
            String key = DIRECTION_BY_ME.equals(currentDirection) ? PREF_HINT_DISMISSED_BY_ME : PREF_HINT_DISMISSED_TO_ME;
            appPrefs.edit().putBoolean(key, true).apply();
        });
        hintBanner.addView(ivClose, closeParams);

        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hintParams.bottomMargin = dp(12);
        container.addView(hintBanner, hintParams);

        updateHintForCurrentDirection(); // sets initial text + visibility based on the default tab

        // ---- Calendar ----
        calendarView = new CompactCalendarView(ctx, theme, CompactCalendarView.Sizing.fullWidth(ctx));
        calendarView.setPhoneDotColor("#0A84FF");
        calendarView.setEventDotColor("#FF9500");
        calendarView.setSelectedDateColor("#1C1C1E");
        calendarView.setOnDateSelectedListener(this::onCalendarDateSelected);
        int sideMargin = com.thelinkphone.app.utils.OtherUtils.getWidthScreen(ctx) / 90;
        LinearLayout.LayoutParams calParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        calParams.leftMargin = sideMargin;
        calParams.rightMargin = sideMargin;
        calParams.bottomMargin = dp(16);
        container.addView(calendarView, calParams);

        // ---- Event schedule table ----
        eventTable = new LayoutScheduleTable(ctx, theme, "Event Schedule", "#FF9500");
        LinearLayout.LayoutParams eventParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        eventParams.leftMargin = dp(8);
        eventParams.topMargin = dp(4);
        eventParams.bottomMargin = dp(16);
        container.addView(eventTable, eventParams);

        // ---- Phone schedule table ----
        phoneTable = new LayoutScheduleTable(ctx, theme, "Phone Schedule", "#0A84FF");
        LinearLayout.LayoutParams phoneParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        phoneParams.leftMargin = dp(8);
        phoneParams.topMargin = dp(4);
        phoneParams.bottomMargin = dp(16);
        container.addView(phoneTable, phoneParams);

        keyboardSpacer = new View(ctx);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0);
        container.addView(keyboardSpacer, spacerParams);

        eventTable.setOnSearchInteractionListener(() -> requestScrollTo(eventTable.getSearchBarView()));
        phoneTable.setOnSearchInteractionListener(() -> requestScrollTo(phoneTable.getSearchBarView()));
    }

    private void setDirection(String direction) {
        if (direction.equals(currentDirection)) return;
        currentDirection = direction;
        updateHintForCurrentDirection();
        renderFromCache();
    }

    private void onCalendarDateSelected(Calendar date) {
        filterDate = date;
        renderFromCache();
    }

    // ---- Data loading ----

    public void loadUnifiedSchedules() {
        loadUnifiedSchedules(false);
    }

    public void loadUnifiedSchedules(boolean forceRefresh) {
        if (cachedData == null) {
            restoreCacheFromDisk(); // survives app process death
        }

        boolean cacheValid = cachedData != null
                && (System.currentTimeMillis() - cacheTimestamp) < CACHE_TTL_MS;

        if (cachedData != null) {
            lastData = cachedData;
            renderFromCache();
        }

        if (cacheValid && !forceRefresh) {
            return;
        }

        SharedPreferences prefs = ctx.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        if (token == null) return;

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        UnifiedScheduleRepository repo = new UnifiedScheduleRepository(apiService, "Bearer " + token);

        repo.getUnifiedSchedules(new Callback<UnifiedScheduleResponse>() {
            @Override
            public void onResponse(Call<UnifiedScheduleResponse> call, Response<UnifiedScheduleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    lastData = response.body().getData();
                    cachedData = lastData;
                    cacheTimestamp = System.currentTimeMillis();
                    persistCacheToDisk();
                    renderFromCache();
                }
            }

            @Override
            public void onFailure(Call<UnifiedScheduleResponse> call, Throwable t) {
                // silent fail -- tables just stay empty; add a toast/log here if you want visibility
            }
        });
    }

    private void restoreCacheFromDisk() {
        SharedPreferences cachePrefs = ctx.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        String json = cachePrefs.getString(CACHE_KEY_DATA, null);
        if (json == null) return;
        try {
            cachedData = new com.google.gson.Gson().fromJson(json, UnifiedScheduleData.class);
            cacheTimestamp = cachePrefs.getLong(CACHE_KEY_TS, 0);
        } catch (Exception e) {
            cachedData = null; // corrupted cache -- ignore, will refetch
        }
    }

    private void persistCacheToDisk() {
        SharedPreferences cachePrefs = ctx.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        String json = new com.google.gson.Gson().toJson(cachedData);
        cachePrefs.edit()
                .putString(CACHE_KEY_DATA, json)
                .putLong(CACHE_KEY_TS, cacheTimestamp)
                .apply();
    }

    public static void invalidateCache(Context context) {
        cachedData = null;
        cacheTimestamp = 0;

        SharedPreferences cachePrefs = context.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        cachePrefs.edit()
                .remove(CACHE_KEY_DATA)
                .remove(CACHE_KEY_TS)
                .apply();
    }

    private void renderFromCache() {
        if (lastData == null) return;

        List<UnifiedScheduleEntry> entries = DIRECTION_BY_ME.equals(currentDirection)
                ? lastData.getGivenByMe()
                : lastData.getGivenToMe();

        ItemUnifiedSchedule item = ItemUnifiedSchedule.from(entries);

        calendarView.setScheduleDays(item.phoneDaysOfWeek);
        calendarView.setEventDates(item.eventDates);

        renderEventTable(item.eventEntries);
        renderPhoneTable(item.phoneEntries);
    }

    private void renderEventTable(List<UnifiedScheduleEntry> eventEntries) {
        Calendar targetDate = filterDate != null ? filterDate : Calendar.getInstance();
        String filterKey = FILTER_KEY_FMT.format(targetDate.getTime());

        ItemEventScheduleRow.Result result = ItemEventScheduleRow.from(eventEntries, targetDate, filterKey);

        if (result.rows.isEmpty()) {
            applyEmptyState(eventTable, result.emptyReason);
            return;
        }

        List<String> headers = Arrays.asList("Name", "Number", "Email", "Link", "Schedule", "Time", "Status");
        List<List<String>> rows = new ArrayList<>();
        Set<Integer> badgedRows = new HashSet<>();

        int idx=0;
        for (ItemEventScheduleRow r : result.rows) {
            PartyPayload party = r.entry.getOtherParty();
            if (party != null && party.isCallalinkUser()) badgedRows.add(idx);

            rows.add(Arrays.asList(
                    ItemPartyDisplay.name(ctx, party),
                    nullSafe(party != null ? party.getPhoneNumber() : null),
                    nullSafe(party != null ? party.getEmail() : null),
                    ItemPartyDisplay.link(party),
                    nullSafe(r.entry.getEventName()),
                    nullSafe(r.entry.getSlotTime()),
                    r.statusLabel()
            ));
            idx++;
        }
        eventTable.setData(headers, rows, badgedRows);
    }

    private void renderPhoneTable(List<UnifiedScheduleEntry> phoneEntries) {

        Calendar targetDate = filterDate != null ? filterDate : Calendar.getInstance();
        ItemPhoneScheduleRow.Result result = ItemPhoneScheduleRow.from(phoneEntries, targetDate);

        if (result.rows.isEmpty()) {
            applyEmptyState(phoneTable, result.emptyReason);
            return;
        }

        List<String> headers = Arrays.asList("Name", "Number", "Email", "Link", "Schedule", "Time", "Status");
        List<List<String>> rows = new ArrayList<>();
        Set<Integer> badgedRows = new HashSet<>();

        int idx = 0;
        for (ItemPhoneScheduleRow r : result.rows) {
            if (r.isCallalinkUser()) badgedRows.add(idx);
            rows.add(Arrays.asList(
                    r.nameLabel(ctx),
                    nullSafe(r.rawNumber()),
                    nullSafe(r.emailLabel()),
                    r.linkLabel(),
                    r.scheduleLabel(),
                    r.timeLabel(),
                    r.statusLabel()
            ));
            idx++;
        }
        phoneTable.setData(headers, rows, badgedRows);
    }

    private void applyEmptyState(LayoutScheduleTable table, ScheduleAvailabilityUtils.EmptyReason reason) {
        switch (reason) {
            case PAST_DATE:
                table.setEmptyState("Nothing to show here", "This date is in the past.");
                break;
            case TODAY_EXPIRED:
                table.setEmptyState("No more slots today", "All of today's time slots have already ended.");
                break;
            case NO_SCHEDULE:
            default:
                table.setEmptyState("No schedule available", "This day has no available time slots.");
                break;
        }
    }

    private String nullSafe(String s) {
        return s != null ? s : "-";
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        keyboardListener = () -> {
            android.graphics.Rect r = new android.graphics.Rect();
            getWindowVisibleDisplayFrame(r);
            int visibleHeight = r.height();
            visibleBottom = r.bottom;

            if (closedWindowHeight == 0) {
                closedWindowHeight = visibleHeight;
                return;
            }

            boolean nowOpen = (closedWindowHeight - visibleHeight) > dp(150);

            if (nowOpen && !keyboardOpen) {
                keyboardOpen = true;
                int keyboardHeight = closedWindowHeight - visibleHeight;
                setKeyboardSpacerHeight(keyboardHeight);
                if (pendingScrollTarget != null) {
                    scrollToKeepVisible(pendingScrollTarget);
                    pendingScrollTarget = null;
                }
            } else if (!nowOpen && keyboardOpen) {
                keyboardOpen = false;
                closedWindowHeight = visibleHeight;
                setKeyboardSpacerHeight(0);
            }
        };
        getViewTreeObserver().addOnGlobalLayoutListener(keyboardListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (keyboardListener != null) {
            getViewTreeObserver().removeOnGlobalLayoutListener(keyboardListener);
        }
        super.onDetachedFromWindow();
    }

    private void requestScrollTo(View target) {
        if (keyboardOpen) {
            scrollToKeepVisible(target);
        } else {
            pendingScrollTarget = target;
        }
    }

    private void scrollToKeepVisible(View target) {
        scrollView.post(() -> {
            int[] loc = new int[2];
            target.getLocationOnScreen(loc);
            int targetBottom = loc[1] + target.getHeight();

            int buffer = dp(200);
            int overlap = (targetBottom + buffer) - visibleBottom;

            android.util.Log.d("SCROLL_DEBUG", "targetBottom=" + targetBottom
                    + " visibleBottom=" + visibleBottom + " overlap=" + overlap);

            if (overlap > 0) {
                scrollView.smoothScrollBy(0, overlap);
            }
        });
    }

    private void setKeyboardSpacerHeight(int height) {
        if (keyboardSpacer == null) return;
        ViewGroup.LayoutParams lp = keyboardSpacer.getLayoutParams();
        if (lp.height != height) {
            lp.height = height;
            keyboardSpacer.setLayoutParams(lp);
        }
    }

    private void updateHintForCurrentDirection() {
        if (hintBanner == null) return;

        boolean isByMe = DIRECTION_BY_ME.equals(currentDirection);
        String key = isByMe ? PREF_HINT_DISMISSED_BY_ME : PREF_HINT_DISMISSED_TO_ME;
        boolean dismissed = appPrefs.getBoolean(key, false);

        if (dismissed) {
            hintBanner.setVisibility(View.GONE);
        } else {
            hintBanner.setVisibility(View.VISIBLE);
            tvHint.setText(isByMe ? R.string.unified_schedule_hint_by_me : R.string.unified_schedule_hint_to_me);
        }
    }
}