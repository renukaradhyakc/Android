package com.thelinkphone.app.utils;

import com.thelinkphone.app.item.ItemRecent;
import com.thelinkphone.app.item.ItemRecentGroup;

import java.util.ArrayList;

public class RecentSearchUtils {

    public static boolean matches(ItemRecentGroup group, String query) {
        if (group == null || query == null) return false;
        String trimmed = query.trim();
        if (trimmed.isEmpty()) return false;

        if (group.name != null && group.name.toLowerCase().contains(trimmed.toLowerCase())) {
            return true;
        }

        String queryDigits = digitsOnly(trimmed);
        if (!queryDigits.isEmpty() && group.arrRecent != null) {
            for (ItemRecent recent : group.arrRecent) {
                if (recent.number != null && digitsOnly(recent.number).contains(queryDigits)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String digitsOnly(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) sb.append(c);
        }
        return sb.toString();
    }

    public static String displayLabel(ItemRecentGroup group) {
        if (group.name != null && !group.name.isEmpty()) return group.name;
        if (group.arrRecent != null && !group.arrRecent.isEmpty()) return group.arrRecent.get(0).number;
        return "";
    }

    public static ArrayList<ItemRecentGroup> filterMissedGroups(ArrayList<ItemRecentGroup> source) {
        ArrayList<ItemRecentGroup> result = new ArrayList<>();
        if (source == null) return result;
        for (ItemRecentGroup group : source) {
            boolean hasMissed = false;
            if (group.arrRecent != null) {
                for (com.thelinkphone.app.item.ItemRecent recent : group.arrRecent) {
                    if (recent.type == 3) {
                        hasMissed = true;
                        break;
                    }
                }
            }
            if (hasMissed) result.add(group);
        }
        return result;
    }
}