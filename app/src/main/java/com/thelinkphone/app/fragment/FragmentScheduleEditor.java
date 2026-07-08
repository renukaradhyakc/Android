package com.thelinkphone.app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.DropDown;
import com.thelinkphone.app.item.ItemTimeSlot;
import com.thelinkphone.app.item.ItemWeekDaySchedule;
import com.thelinkphone.app.custom.LayoutWeekDayRow;
import com.thelinkphone.app.dialog.DialogCopyToDays;
import com.thelinkphone.app.model.PhoneScheduleResponse;
import com.thelinkphone.app.model.Schedule;
import com.thelinkphone.app.model.ScheduleListResponse;
import com.thelinkphone.app.model.ScheduleResponse;
import com.thelinkphone.app.model.ScheduleSlot;
import com.thelinkphone.app.repository.PhoneScheduleRepository;
import com.thelinkphone.app.repository.ScheduleRepository;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.TimeFormatUtils;
import com.thelinkphone.app.utils.TimeZoneUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentScheduleEditor extends Fragment {

    private static final String ARG_PHONE = "phone";
    private String phoneNumber;
    private ArrayList<ItemWeekDaySchedule> weekSchedule;
    private ArrayList<LayoutWeekDayRow> dayRows;
    private LinearLayout customSection;
    private LinearLayout existingSection;
    private DropDown dropDownSchedules;
    private ArrayList<Schedule> scheduleOptions = new ArrayList<>();
    private Integer selectedExistingScheduleId = null;
    private boolean isExistingScheduleMode = false;
    private ArrayList<ItemWeekDaySchedule> existingWeekSchedule;
    private ArrayList<LayoutWeekDayRow> existingDayRows;

    public static FragmentScheduleEditor newInstance(String phone) {
        FragmentScheduleEditor f = new FragmentScheduleEditor();
        Bundle b = new Bundle();
        b.putString(ARG_PHONE, phone);
        f.setArguments(b);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Context context = inflater.getContext();
        phoneNumber = getArguments() != null ? getArguments().getString(ARG_PHONE) : null;
        boolean theme = MyShare.getTheme(context);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(theme ? Color.parseColor("#F2F2F7") : Color.parseColor("#2C2C2C"));

        ScrollView scrollView = new ScrollView(context);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = OtherUtils.getWidthScreen(context) / 25;
        content.setPadding(pad, pad, pad, pad);
        scrollView.addView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView tvQuestion = new TextView(context);
        tvQuestion.setText("How do you want to set the schedule for this number?");
        tvQuestion.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvQuestion.setTypeface(tvQuestion.getTypeface(), Typeface.BOLD);
        tvQuestion.setTextColor(theme ? Color.BLACK : Color.WHITE);
        LinearLayout.LayoutParams qParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        qParams.bottomMargin = pad;
        content.addView(tvQuestion, qParams);

        LinearLayout optionExisting = buildCheckboxOption(context, theme, pad,
                "Use an existing schedule", false);
        LinearLayout optionCustom   = buildCheckboxOption(context, theme, pad,
                "Set Custom Hours", true);

        LinearLayout optionsRow = new LinearLayout(context);
        optionsRow.setOrientation(LinearLayout.HORIZONTAL);
        optionsRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout.LayoutParams optionExistingParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        optionExistingParams.setMarginEnd(pad / 2);
        LinearLayout.LayoutParams optionCustomParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        optionCustomParams.setMarginStart(pad / 2);

        optionsRow.addView(optionExisting, optionExistingParams);
        optionsRow.addView(optionCustom, optionCustomParams);

        LinearLayout.LayoutParams optionsRowParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        optionsRowParams.bottomMargin = pad;

        content.addView(optionsRow, optionsRowParams);

        View cbExisting = optionExisting.getChildAt(0);
        View cbCustom   = optionCustom.getChildAt(0);

        optionExisting.setOnClickListener(v -> {
            isExistingScheduleMode = true;
            cbExisting.setBackgroundResource(R.drawable.schedule_checkbox_checked_bg);
            cbCustom.setBackgroundResource(R.drawable.schedule_checkbox_unchecked_bg);
            ((FrameLayout) cbExisting).getChildAt(0).setVisibility(View.VISIBLE);
            ((FrameLayout) cbCustom).getChildAt(0).setVisibility(View.GONE);
            customSection.setVisibility(View.GONE);
            existingSection.setVisibility(View.VISIBLE);
            if (scheduleOptions.isEmpty()) loadExistingSchedules(context);
        });

        optionCustom.setOnClickListener(v -> {
            isExistingScheduleMode = false;
            cbCustom.setBackgroundResource(R.drawable.schedule_checkbox_checked_bg);
            cbExisting.setBackgroundResource(R.drawable.schedule_checkbox_unchecked_bg);
            ((FrameLayout) cbCustom).getChildAt(0).setVisibility(View.VISIBLE);
            ((FrameLayout) cbExisting).getChildAt(0).setVisibility(View.GONE);
            customSection.setVisibility(View.VISIBLE);
            existingSection.setVisibility(View.GONE);
        });

        // ---- Existing-schedule section (hidden by default) ----
        existingSection = new LinearLayout(context);
        existingSection.setOrientation(LinearLayout.VERTICAL);
        existingSection.setVisibility(View.GONE);
        content.addView(existingSection, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvWhich = new TextView(context);
        tvWhich.setText("Which schedule do you want to use?");
        tvWhich.setTextColor(theme ? Color.BLACK : Color.WHITE);
        tvWhich.setTypeface(tvWhich.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams whichParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        whichParams.bottomMargin = dp(context, 8);
        existingSection.addView(tvWhich, whichParams);

        dropDownSchedules = new DropDown(context, theme);
        dropDownSchedules.setOnItemSelectedListener((position, label) -> {
            if (position < 0 || position >= scheduleOptions.size()) return;
            selectedExistingScheduleId = scheduleOptions.get(position).getId();
            loadScheduleSlotsPreview(context, selectedExistingScheduleId);
        });
        LinearLayout.LayoutParams dropdownParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dropdownParams.topMargin = pad / 2;
        dropdownParams.bottomMargin = dp(context, 16);
        existingSection.addView(dropDownSchedules, dropdownParams);

        LinearLayout tzRow = new LinearLayout(context);
        tzRow.setOrientation(LinearLayout.HORIZONTAL);
        tzRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams tzRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tzRowParams.topMargin = dp(context, 12);
        tzRowParams.bottomMargin = pad;
        existingSection.addView(tzRow, tzRowParams);

        TextView tvGlobeIcon = new TextView(context);
        tvGlobeIcon.setText("\uD83C\uDF10"); // 🌐
        tvGlobeIcon.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tzRow.addView(tvGlobeIcon, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvTimezoneValue = new TextView(context);
        tvTimezoneValue.setText(TimeZoneUtils.getUserTimezone(context));
        tvTimezoneValue.setTextColor(Color.parseColor("#6366F1"));
        tvTimezoneValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        LinearLayout.LayoutParams tzValueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tzValueParams.setMarginStart(dp(context, 5));
        tzRow.addView(tvTimezoneValue, tzValueParams);

        existingWeekSchedule = buildEmptyWeek();
        existingDayRows = new ArrayList<>();

        LinearLayout existingRowsContainer = new LinearLayout(context);
        existingRowsContainer.setOrientation(LinearLayout.VERTICAL);
        existingSection.addView(existingRowsContainer, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        for (ItemWeekDaySchedule day : existingWeekSchedule) {
            LayoutWeekDayRow row = new LayoutWeekDayRow(context, day, theme,false,
                    new LayoutWeekDayRow.WeekDayRowListener() {
                        @Override
                        public void onCopyRequested(ItemWeekDaySchedule sourceDay) {
                        }

                        @Override
                        public void onScheduleChanged() {
                        }
                    });
            existingDayRows.add(row);
            existingRowsContainer.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        customSection = new LinearLayout(context);
        customSection.setOrientation(LinearLayout.VERTICAL);
        content.addView(customSection, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvTimeZoneLabel = new TextView(context);
        tvTimeZoneLabel.setText("Time Zone:");
        tvTimeZoneLabel.setTextColor(theme ? Color.BLACK : Color.WHITE);
        tvTimeZoneLabel.setTypeface(tvTimeZoneLabel.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams tzLabelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tzLabelParams.bottomMargin = dp(context, 8);
        customSection.addView(tvTimeZoneLabel, tzLabelParams);

        DropDown dropdownTimeZone = new DropDown(context, theme);
        LinearLayout.LayoutParams tzDropdownParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tzDropdownParams.bottomMargin = pad;
        customSection.addView(dropdownTimeZone, tzDropdownParams);

        loadTimeZonesForDropdown(context, dropdownTimeZone);

        weekSchedule = buildEmptyWeek();
        dayRows = new ArrayList<>();

        for (ItemWeekDaySchedule day : weekSchedule) {
            LayoutWeekDayRow row = new LayoutWeekDayRow(context, day, theme,
                    new LayoutWeekDayRow.WeekDayRowListener() {
                        @Override
                        public void onCopyRequested(ItemWeekDaySchedule sourceDay) {
                            new DialogCopyToDays(context, sourceDay.dayOfWeek, theme, selectedDays -> {
                                for (int targetDayNum : selectedDays) {
                                    for (int i = 0; i < weekSchedule.size(); i++) {
                                        if (weekSchedule.get(i).dayOfWeek == targetDayNum) {
                                            dayRows.get(i).applyCopiedSlots(sourceDay.slots);
                                        }
                                    }
                                }
                            }).show();
                        }

                        @Override
                        public void onScheduleChanged() {
                            // hook for live validation later if needed
                        }
                    });
            dayRows.add(row);
            customSection.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        root.addView(scrollView, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        LinearLayout buttonRow = new LinearLayout(context);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);

        Button btnSave = new Button(context);
        btnSave.setText("Save");
        btnSave.setTextColor(Color.WHITE);
        btnSave.setAllCaps(false);
        btnSave.setBackgroundResource(R.drawable.btn_save_bg);
        btnSave.setOnClickListener(v -> onSaveClicked(context));

        Button btnDiscard = new Button(context);
        btnDiscard.setText("Discard");
        btnDiscard.setTextColor(Color.BLACK);
        btnDiscard.setAllCaps(false);
        btnDiscard.setBackgroundResource(R.drawable.btn_discard_bg);
        btnDiscard.setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        int btnHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                context.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, btnHeight, 1f);
        saveParams.setMarginEnd(pad / 2);
        buttonRow.addView(btnSave, saveParams);

        LinearLayout.LayoutParams discardParams = new LinearLayout.LayoutParams(0, btnHeight, 1f);
        discardParams.setMarginStart(pad / 2);
        buttonRow.addView(btnDiscard, discardParams);

        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(pad, pad / 2, pad, pad);
        root.addView(buttonRow, rowParams);

        return root;
    }

    private ArrayList<ItemWeekDaySchedule> buildEmptyWeek() {
        ArrayList<ItemWeekDaySchedule> list = new ArrayList<>();
        String[] labels = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};
        for (int i = 0; i < 7; i++) {
            list.add(new ItemWeekDaySchedule(i + 1, labels[i]));
        }
        return list;
    }

    private String buildSlotsJson() {
        JSONArray arr = new JSONArray();
        try {
            for (ItemWeekDaySchedule day : weekSchedule) {
                if (!day.available) continue;
                for (ItemTimeSlot slot : day.slots) {
                    JSONObject obj = new JSONObject();
                    obj.put("day_of_week", day.dayOfWeek);
                    obj.put("from_time", slot.fromTime);
                    obj.put("to_time", slot.toTime);
                    arr.put(obj);
                }
            }
        } catch (Exception e) {
            Log.e("ScheduleEditor", "Failed to build slots JSON", e);
        }
        return arr.toString();
    }

    private void onSaveClicked(Context context) {
        if (isExistingScheduleMode) {
            saveExistingSchedule(context);
        } else {
            saveCustomHoursSchedule(context); // your current saveSchedule() body, renamed
        }
    }

    // TODO: this needs your original custom-hours save body pasted back in here
    // (the POST call that sends buildSlotsJson() to assignCustom()). It wasn't
    // present in the file as given, so I've left it as a stub rather than guess
    // at the API shape.
    private void saveCustomHoursSchedule(Context context) {
        Log.w("ScheduleEditor", "saveCustomHoursSchedule() is a stub — restore original save logic here");
    }

    private void saveExistingSchedule(Context context) {
        if (phoneNumber == null || selectedExistingScheduleId == null) {
            Toast.makeText(context, "Select a schedule first", Toast.LENGTH_SHORT).show();
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String authToken = prefs.getString("auth_token", null);
        if (authToken == null) return;

        PhoneScheduleRepository repository = new PhoneScheduleRepository(
                ApiClient.getClient().create(ApiService.class), "Bearer " + authToken);

        repository.assignExisting(phoneNumber, selectedExistingScheduleId, new Callback<PhoneScheduleResponse>() {
            @Override
            public void onResponse(Call<PhoneScheduleResponse> call, Response<PhoneScheduleResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(context, "Schedule assigned", Toast.LENGTH_SHORT).show();
                    if (getActivity() != null) getActivity().onBackPressed();
                } else {
                    Toast.makeText(context, "Failed to assign schedule", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PhoneScheduleResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExistingSchedules(Context context) {
        Log.d("ScheduleAPI", "loadExistingSchedules called");

        ScheduleRepository repository = new ScheduleRepository(context);

        repository.getSchedules(new Callback<ScheduleListResponse>() {
            @Override
            public void onResponse(Call<ScheduleListResponse> call, Response<ScheduleListResponse> response) {
                Log.d("ScheduleAPI", "Response received");
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    scheduleOptions.clear();
                    scheduleOptions.addAll(response.body().getData());

                    String[] names = new String[scheduleOptions.size()];
                    for (int i = 0; i < scheduleOptions.size(); i++) {
                        names[i] = scheduleOptions.get(i).getScheduleName();
                    }

                    dropDownSchedules.setItems(names);

                } else {
                    Toast.makeText(context, "Failed to load schedules", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ScheduleListResponse> call,
                                  Throwable t) {

                if (!isAdded()) return;

                Toast.makeText(context,
                        "Network error",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadScheduleSlotsPreview(Context context, int scheduleId) {
        ScheduleRepository repository = new ScheduleRepository(context);

        repository.getScheduleDetails(scheduleId, new Callback<ScheduleResponse>() {
            @Override
            public void onResponse(Call<ScheduleResponse> call,
                                   Response<ScheduleResponse> response) {
                if (!isAdded()) return;

                if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                    Toast.makeText(context, "Unable to load schedule", Toast.LENGTH_SHORT).show();
                    return;
                }
                populateSchedulePreview(response.body().getData());
            }

            @Override
            public void onFailure(Call<ScheduleResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTimeZonesForDropdown(Context context, DropDown dropdownTimeZone) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String json = prefs.getString("timezone_list", "[]");
        Log.d("TIMEZONE", "Stored timezones = " + json);

        String[] timezones;
        try {Type type = new com.google.gson.reflect.TypeToken<ArrayList<String>>() {}.getType();

            ArrayList<String> timezoneList = new Gson().fromJson(json, type);

            if (timezoneList != null && !timezoneList.isEmpty()) {
                timezones = timezoneList.toArray(new String[0]);
            } else {
                timezones = new String[]{ "Asia/Kolkata", "America/New_York", "Europe/London" };
            }

        } catch (Exception e) {
            Log.e("TIMEZONE", "Failed to parse timezone list", e);
            timezones = new String[]{ "Asia/Kolkata", "America/New_York", "Europe/London" };
        }

        dropdownTimeZone.setItems(timezones);
        dropdownTimeZone.setSelectedPosition(160);
        dropdownTimeZone.setOnItemSelectedListener((position, label) -> {
            Log.d("ScheduleEditor", "Selected timezone: " + label);
        });
    }


    private void populateSchedulePreview(Schedule schedule) {
        for (LayoutWeekDayRow row : existingDayRows) {
            row.applySchedule(false, new ArrayList<>());
        }

        if (schedule == null || schedule.getSlots() == null || schedule.getSlots().isEmpty()) {
            return;
        }

        Map<Integer, List<ScheduleSlot>> byDay = new TreeMap<>();
        for (ScheduleSlot slot : schedule.getSlots()) {
            byDay.computeIfAbsent(slot.getDayOfWeek(), k -> new ArrayList<>()).add(slot);
        }

        for (Map.Entry<Integer, List<ScheduleSlot>> entry : byDay.entrySet()) {
            int dayOfWeek = entry.getKey(); // 1=Mon ... 7=Sun
            List<ScheduleSlot> daySlots = entry.getValue();

            daySlots.sort((a, b) -> {
                Calendar ca = TimeFormatUtils.parseTimeOnly(a.getFromTime());
                Calendar cb = TimeFormatUtils.parseTimeOnly(b.getFromTime());
                if (ca == null || cb == null) return 0;
                return ca.compareTo(cb);
            });

            ArrayList<ItemTimeSlot> itemSlots = new ArrayList<>();
            for (ScheduleSlot slot : daySlots) {
                itemSlots.add(new ItemTimeSlot(slot.getFromTime(), slot.getToTime()));
            }

            if (dayOfWeek >= 1 && dayOfWeek <= 7) {
                existingDayRows.get(dayOfWeek - 1).applySchedule(true, itemSlots);
            }
        }
    }

    private int dp(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics());
    }

    private LinearLayout buildCheckboxOption(Context context, boolean theme,
                                             int pad, String label, boolean startChecked) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int vPad = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
        row.setPadding(0, vPad, 0, vPad);

        int cbSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 22, context.getResources().getDisplayMetrics());

        FrameLayout cbFrame = new android.widget.FrameLayout(context);
        cbFrame.setBackgroundResource(startChecked
                ? R.drawable.schedule_checkbox_checked_bg
                : R.drawable.schedule_checkbox_unchecked_bg);

        ImageView tick = new android.widget.ImageView(context);
        tick.setImageResource(R.drawable.ic_check_white);
        tick.setVisibility(startChecked ? View.VISIBLE : View.GONE);
        int tickPad = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, context.getResources().getDisplayMetrics());
        tick.setPadding(tickPad, tickPad, tickPad, tickPad);
        cbFrame.addView(tick, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));

        LinearLayout.LayoutParams cbParams = new LinearLayout.LayoutParams(cbSize, cbSize);
        cbParams.setMarginEnd(pad / 2);
        row.addView(cbFrame, cbParams);

        TextView tv = new TextView(context);
        tv.setText(label);
        tv.setTextColor(theme ? Color.parseColor("#1C1C1E") : Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        row.addView(tv, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        row.setOnClickListener(v -> {
            boolean nowChecked = !cbFrame.isSelected();
            tick.setVisibility(nowChecked ? View.VISIBLE : View.GONE);
        });

        cbFrame.setSelected(startChecked);

        return row;
    }
}