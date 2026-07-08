package com.thelinkphone.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.thelinkphone.app.utils.OtherUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DialogCopyToDays extends Dialog {

    public interface CopyResult {
        void onDaysSelected(ArrayList<Integer> selectedDayNumbers);
    }

    private final Context ctx;
    private final int excludeDay; // don't offer copying onto the source day itself
    private final CopyResult result;

    public DialogCopyToDays(Context context, int excludeDay, boolean theme, CopyResult result) {
        super(context);
        this.ctx = context;
        this.excludeDay = excludeDay;
        this.result = result;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        build(theme);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }

    private void build(boolean theme) {
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(16), dp(20), dp(16));
        int bg = theme ? Color.WHITE : Color.parseColor("#2C2C2C");
        root.setBackground(OtherUtils.bgIcon(bg, dp(12)));

        Map<Integer, String> days = new LinkedHashMap<>();
        days.put(1, "Monday");
        days.put(2, "Tuesday");
        days.put(3, "Wednesday");
        days.put(4, "Thursday");
        days.put(5, "Friday");
        days.put(6, "Saturday");
        days.put(7, "Sunday");

        ArrayList<CheckBox> boxes = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : days.entrySet()) {
            if (entry.getKey() == excludeDay) continue;

            CheckBox cb = new CheckBox(ctx);
            cb.setText(entry.getValue());
            cb.setTag(entry.getKey());
            cb.setTextColor(theme ? Color.BLACK : Color.WHITE);
            cb.setPadding(0, dp(8), 0, dp(8));
            boxes.add(cb);
            root.addView(cb, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        Button btnCopy = new Button(ctx);
        btnCopy.setText("Copy");
        btnCopy.setTextColor(Color.WHITE);
        btnCopy.setBackground(OtherUtils.bgIcon(Color.parseColor("#007AFF"), dp(8)));
        btnCopy.setOnClickListener(v -> {
            ArrayList<Integer> selected = new ArrayList<>();
            for (CheckBox cb : boxes) {
                if (cb.isChecked()) selected.add((Integer) cb.getTag());
            }
            result.onDaysSelected(selected);
            dismiss();
        });
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
        btnParams.topMargin = dp(12);
        root.addView(btnCopy, btnParams);

        setContentView(root);
        Window window = getWindow();
        if (window != null) {
            window.setLayout(dp(280), LinearLayout.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.CENTER);
        }
    }
}