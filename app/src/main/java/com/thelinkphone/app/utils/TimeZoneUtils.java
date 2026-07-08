package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class TimeZoneUtils {

    private static final String PREF_NAME = "app_prefs";
    private static final String TIMEZONE_KEY = "auth_timezone";
    private static final String TIMEZONE_LIST_KEY = "timezone_list";

    public static String getUserTimezone(Context context) {

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        String authTimezone = prefs.getString(TIMEZONE_KEY, null);
        String json = prefs.getString(TIMEZONE_LIST_KEY, null);

        if (authTimezone == null || json == null) {
            return "";
        }

        try {
            Type type = new TypeToken<List<String>>() {}.getType();
            List<String> timezones = new Gson().fromJson(json, type);

            int index = Integer.parseInt(authTimezone);

            if (index >= 0 && index < timezones.size()) {
                return timezones.get(index);
            }

        } catch (Exception e) {
            Log.e("TimeZoneUtils", "Failed to load timezone", e);
        }

        return "";
    }
}