package com.thelinkphone.app.utils;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.graphics.Typeface;

public class SearchHighlightUtils {

    public static SpannableString highlight(String source, String query) {
        SpannableString spannable = new SpannableString(source == null ? "" : source);
        if (source == null || query == null || query.trim().isEmpty()) {
            return spannable;
        }
        String lowerSource = source.toLowerCase();
        String lowerQuery = query.trim().toLowerCase();
        int start = lowerSource.indexOf(lowerQuery);
        if (start >= 0) {
            int end = start + lowerQuery.length();
            spannable.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#007AFF")), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }
}