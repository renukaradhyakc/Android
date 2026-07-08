package com.thelinkphone.app.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Generates 15-minute interval time labels for the whole day,
 * e.g. "12:00 AM", "12:15 AM", ... "11:45 PM".
 * Equivalent of your web's getConstTimeArr() helper.
 */
public class TimeSlotUtils {

    public static List<String> getTimeOptions() {
        List<String> times = new ArrayList<>();
        SimpleDateFormat fmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);

        for (int i = 0; i < 96; i++) { // 24h * 4 (15-min steps)
            times.add(fmt.format(cal.getTime()));
            cal.add(Calendar.MINUTE, 15);
        }
        return times;
    }

    /** Index of a given time string in the generated list, or 0 if not found. */
    public static int indexOf(List<String> options, String time) {
        int idx = options.indexOf(time);
        return idx >= 0 ? idx : 0;
    }
}