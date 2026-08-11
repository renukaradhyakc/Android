package com.thelinkphone.app.utils;

import java.util.List;
import java.util.Locale;

public class TableSearchUtils {

    public static boolean matches(List<String> cells, String query) {
        if (cells == null || query == null) return false;
        String trimmed = query.trim();
        if (trimmed.isEmpty()) return false;

        String[] tokens = trimmed.toLowerCase(Locale.US).split("\\s+");

        for (String token : tokens) {
            if (!rowContainsToken(cells, token)) {
                return false;
            }
        }
        return true;
    }

    private static boolean rowContainsToken(List<String> cells, String token) {
        for (String cell : cells) {
            if (cell != null && cell.toLowerCase(Locale.US).contains(token)) {
                return true;
            }
        }
        return false;
    }
}