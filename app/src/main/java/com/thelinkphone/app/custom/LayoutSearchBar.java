package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class LayoutSearchBar extends LinearLayout {

    public interface OnQueryChangeListener {
        void onQueryChanged(String query);
    }

    private final Context ctx;
    private final boolean theme;
    private EditText etSearch;
    private OnQueryChangeListener listener;

    public LayoutSearchBar(Context context, boolean theme) {
        super(context);
        this.ctx = context;
        this.theme = theme;
        build();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }

    private void build() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(8), 0, dp(8), 0);

        int pillColor = Color.WHITE;
        GradientDrawable pillBg = new GradientDrawable();
        pillBg.setColor(pillColor);
        pillBg.setCornerRadius(dp(17));
        setBackground(pillBg);

        int iconTint = theme ? Color.parseColor("#8A8A8E") : Color.parseColor("#B8B8B8");

        ImageView icon = new ImageView(ctx);
        icon.setImageResource(android.R.drawable.ic_menu_search);
        icon.setColorFilter(iconTint);
        LayoutParams iconParams = new LayoutParams(dp(14), dp(14));
        iconParams.setMarginEnd(dp(6));
        addView(icon, iconParams);

        etSearch = new EditText(ctx);
        etSearch.setHint("Search");
        etSearch.setSingleLine(true);
        etSearch.setBackground(null);
        etSearch.setPadding(0, 0, 0, 0);
        etSearch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f);
        etSearch.setTextColor(theme ? Color.BLACK : Color.WHITE);
        etSearch.setHintTextColor(iconTint);
        etSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        etSearch.setInputType(InputType.TYPE_CLASS_TEXT);
        LayoutParams etParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
        etParams.weight = 1;
        addView(etSearch, etParams);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                if (listener != null) listener.onQueryChanged(s.toString());
            }
        });
    }

    public void setOnQueryChangeListener(OnQueryChangeListener listener) {
        this.listener = listener;
    }

    public String getQuery() {
        return etSearch.getText().toString();
    }

    public void clear() {
        etSearch.setText("");
    }

    public void setOnSearchInteractionListener(Runnable onInteraction) {
        etSearch.setOnClickListener(v -> onInteraction.run());
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) onInteraction.run();
        });
    }

    public boolean isSearchActive() {
        return etSearch.hasFocus();
    }
}