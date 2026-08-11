package com.thelinkphone.app.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeFormatUtils {

    private static final Pattern TIME_PATTERN = Pattern.compile(
            "^\\s*(\\d{1,2}):(\\d{2})(?::(\\d{2}))?\\s*(AM|PM)?\\s*$",
            Pattern.CASE_INSENSITIVE);
    public static Calendar parseTimeOnly(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) return null;

        Matcher m = TIME_PATTERN.matcher(trimmed);
        if (!m.matches()) return null;

        int hour = Integer.parseInt(m.group(1));
        int minute = Integer.parseInt(m.group(2));
        int second = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;
        String meridiem = m.group(4);

        if (minute > 59 || second > 59) return null;

        int hour24;

        if (meridiem != null && hour >= 1 && hour <= 12) {
            boolean isPm = meridiem.equalsIgnoreCase("PM");
            hour24 = isPm ? (hour % 12) + 12 : (hour % 12);
        } else {
            if (hour < 0 || hour > 23) return null;
            hour24 = hour;
        }

        Calendar result = Calendar.getInstance();
        result.set(Calendar.HOUR_OF_DAY, hour24);
        result.set(Calendar.MINUTE, minute);
        result.set(Calendar.SECOND, second);
        result.set(Calendar.MILLISECOND, 0);
        return result;
    }

    public static String toDisplayTime(String raw) {
        Calendar parsed = parseTimeOnly(raw);
        if (parsed == null) return raw;
        SimpleDateFormat out = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return out.format(parsed.getTime());
    }

    public static boolean isBeforeNow(String rawTime) {
        Calendar parsed = parseTimeOnly(rawTime);
        if (parsed == null) return false;
        return parsed.before(Calendar.getInstance());
    }

    private TimeFormatUtils() {
    }

    public static Calendar combineDateAndTime(String dateStr, String timeStr) {
        Calendar timeCal = parseTimeOnly(timeStr);
        if (timeCal == null || dateStr == null) return null;

        try {
            SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = dateFmt.parse(dateStr.trim());
            Calendar combined = Calendar.getInstance();
            combined.setTime(date);
            combined.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
            combined.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
            combined.set(Calendar.SECOND, 0);
            combined.set(Calendar.MILLISECOND, 0);
            return combined;
        } catch (ParseException e) {
            return null;
        }
    }
}