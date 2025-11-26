package com.thelinkphone.app;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.thelinkphone.app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Compact bottom panel: ChipGroup + input + send flight icon.
 * Usage:
 *   LinkChipPanel panel = new LinkChipPanel(context);
 *   panel.setOnSendListener(links -> { ... });
 */
public class LinkChipPanel extends FrameLayout {

    private final ChipGroup chipGroup;
    private final EditText input;
    private final ImageButton btnSend;
    private final List<String> links = new ArrayList<>();

    // simple URL-ish pattern (not exhaustive). Use stricter validation if needed.
    private static final Pattern URL_LIKE = Pattern.compile(
            "(https?://)?(www\\.)?\\S+\\.\\S{2,}.*",
            Pattern.CASE_INSENSITIVE
    );

    public interface OnSendListener {
        void onSend(@NonNull List<String> links);
    }

    private OnSendListener onSendListener;

    public LinkChipPanel(@NonNull Context context) {
        super(context);

        // container sizing & padding
        int pad = dp(12);
        setPadding(pad, pad, pad, pad);
        setBackgroundResource(R.drawable.panel_background_rounded); // create drawable (see below)
        setClipToPadding(false);

        // vertical stack: chips (horizontal scroll) + input row
        LinearLayout vertical = new LinearLayout(context);
        vertical.setOrientation(LinearLayout.VERTICAL);
        LayoutParams vv = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(vertical, vv);

        // Scrollable chip row (wrap)
        ScrollView scroll = new ScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        LayoutParams scLp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        vertical.addView(scroll, scLp);

        chipGroup = new ChipGroup(context);
        chipGroup.setSingleLine(false);
        chipGroup.setChipSpacingHorizontal(dp(8));
        chipGroup.setChipSpacingVertical(dp(8));
        chipGroup.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        scroll.addView(chipGroup);

        // input + send icon row
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        row.setPadding(0, dp(8), 0, 0);
        vertical.addView(row);

        input = new EditText(context);
        input.setHint("Paste a link and press Enter");
        input.setSingleLine(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setInputType(EditorInfo.TYPE_TEXT_VARIATION_URI);
        input.setBackgroundResource(R.drawable.input_rounded); // create drawable (see below)
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setLayoutParams(new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        // limit length to avoid overflow
        input.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(1024) });
        row.addView(input);

        btnSend = new ImageButton(context);
        btnSend.setImageResource(R.drawable.ic_send_flight); // add icon in res/drawable
        btnSend.setBackgroundResource(R.drawable.circle_button); // circular background drawable (see below)
        int btnSize = dp(44);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(btnSize, btnSize);
        btnLp.leftMargin = dp(8);
        btnSend.setLayoutParams(btnLp);
        btnSend.setScaleType(ImageButton.ScaleType.CENTER_INSIDE);
        row.addView(btnSend);

        // behavior: Enter key -> create chip
        input.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String text = input.getText().toString().trim();
                addLinkIfValid(text, true);
                handled = true;
            }
            return handled;
        });

        // also handle paste/space -> convert automatically when user types whitespace at end
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.endsWith(" ") || str.endsWith("\n")) {
                    String candidate = str.trim();
                    if (!candidate.isEmpty()) addLinkIfValid(candidate, true);
                }
            }
        });

        btnSend.setOnClickListener(v -> {
            if (links.isEmpty()) {
                // if input has content, convert first
                String t = input.getText().toString().trim();
                if (!t.isEmpty()) {
                    addLinkIfValid(t, true);
                } else {
                    Toast.makeText(getContext(), "Add at least one link", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (onSendListener != null) {
                onSendListener.onSend(new ArrayList<>(links));
            }
        });
    }

    private void addLinkIfValid(String text, boolean clearInput) {
        if (TextUtils.isEmpty(text)) return;
        // Normalize: add http if missing (optional)
        String normalized = text;
        if (!text.startsWith("http://") && !text.startsWith("https://") && URL_LIKE.matcher(text).matches()) {
            normalized = "https://" + text;
        }

        // Basic URL-like check
        if (!URL_LIKE.matcher(normalized).matches()) {
            Toast.makeText(getContext(), "Not a valid link", Toast.LENGTH_SHORT).show();
            return;
        }

        if (links.contains(normalized)) {
            Toast.makeText(getContext(), "Link already added", Toast.LENGTH_SHORT).show();
            if (clearInput) input.setText("");
            return;
        }

        links.add(normalized);
        final Chip chip = new Chip(getContext());
        chip.setText(shorten(normalized));
        chip.setCloseIconVisible(true);
        chip.setClickable(true);
        chip.setCheckable(false);
        chip.setChipBackgroundColorResource(R.color.chip_bg); // provide color in colors.xml
        chip.setTextColor(Color.WHITE);
        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            links.remove(normalized);
        });

        chipGroup.addView(chip);
        if (clearInput) input.setText("");
    }

    private String shorten(String url) {
        if (url.length() <= 36) return url;
        return url.substring(0, 30) + "..." + url.substring(url.length()-6);
    }

    private int dp(int v){ return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics()); }

    public void setOnSendListener(OnSendListener l) { this.onSendListener = l; }

    public List<String> getLinks() { return new ArrayList<>(links); }

    public void clearAll() {
        links.clear();
        chipGroup.removeAllViews();
        input.setText("");
    }
}
