package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemFavorites;
import com.thelinkphone.app.item.ItemNote;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;


public class MyShare {
    private static SharedPreferences share(Context context) {
        return context.getSharedPreferences("preferences", 0);
    }

    public static int getSizeNotification(Context context) {
        return share(context).getInt("size_notification", 0);
    }

    public static int getSizeNavigation(Context context) {
        return share(context).getInt("size_navigation", 0);
    }

    public static void putSizeNotification(Context context, int i) {
        share(context).edit().putInt("size_notification", i).apply();
    }

    public static void putSizeNavigation(Context context, int i) {
        share(context).edit().putInt("size_navigation", i).apply();
    }

    public static void applyPolicy(Context context) {
        share(context).edit().putBoolean("apply_policy", true).apply();
    }

    public static boolean isApplyPolicy(Context context) {
        return share(context).getBoolean("apply_policy", false);
    }

    public static String getPhoto(Context context) {
        return share(context).getString("photo", "");
    }

    public static void putPhoto(Context context, String str) {
        share(context).edit().putString("photo", str).apply();
    }

    public static void putStyle(Context context, int i) {
        share(context).edit().putInt("style", i).apply();
    }

    public static int getStyle(Context context) {
        return share(context).getInt("style", 0);
    }

    public static void putLayout(Context context, int i) {
        share(context).edit().putInt("layout_pos", i).apply();
    }

    public static int getLayout(Context context) {
        return share(context).getInt("layout_pos", 2);
    }

    public static void putTheme(Context context, boolean z) {
        share(context).edit().putBoolean("dark", z).apply();
    }

    public static boolean getTheme(Context context) {
        return share(context).getBoolean("dark", true);
    }

    public static void putSoundPad(Context context, int i) {
        share(context).edit().putInt("sound_pad", i).apply();
    }

    public static int getSoundPad(Context context) {
        return share(context).getInt("sound_pad", 0);
    }

    public static boolean isRate(Context context) {
        return share(context).getBoolean("is_rate_app", false);
    }

    public static void rated(Context context) {
        share(context).edit().putBoolean("is_rate_app", true).apply();
    }

    public static void putPosSim(Context context, int i) {
        share(context).edit().putInt("pos_sim", i).apply();
    }

    public static int getPosSim(Context context) {
        return share(context).getInt("pos_sim", 0);
    }

    public static void putBlockNumber(Context context, ArrayList<ItemContact> arrayList) {
        share(context).edit().putString("arr_block", new Gson().toJson(arrayList)).apply();
    }

    public static ArrayList<ItemContact> getArrBlock(Context context) {
        String string = share(context).getString("arr_block", "");
        if (!string.isEmpty()) {
            ArrayList<ItemContact> arrayList = (ArrayList) new Gson().fromJson(string, new TypeToken<ArrayList<ItemContact>>() {
            }.getType());
            if (arrayList != null) {
                return arrayList;
            }
        }
        return new ArrayList<>();
    }

    public static void putArrNote(Context context, ArrayList<ItemNote> arrayList) {
        share(context).edit().putString("arr_note", new Gson().toJson(arrayList)).apply();
    }

    public static ArrayList<ItemNote> getArrNote(Context context) {
        String string = share(context).getString("arr_note", "");
        if (!string.isEmpty()) {
            ArrayList<ItemNote> arrayList = (ArrayList) new Gson().fromJson(string, new TypeToken<ArrayList<ItemNote>>() {
            }.getType());
            if (arrayList != null) {
                return arrayList;
            }
        }
        return new ArrayList<>();
    }

    public static void putFav(Context context, ArrayList<ItemFavorites> arrayList) {
        share(context).edit().putString("arr_fav_contact", new Gson().toJson(arrayList)).apply();
    }

    public static ArrayList<ItemFavorites> getFav(Context context) {
        String string = share(context).getString("arr_fav_contact", "");
        if (!string.isEmpty()) {
            ArrayList<ItemFavorites> arrayList = (ArrayList) new Gson().fromJson(string, new TypeToken<ArrayList<ItemFavorites>>() {
            }.getType());
            if (arrayList != null) {
                return arrayList;
            }
        }
        return new ArrayList<>();
    }

    // Call settings constants
    public static final int CALL_SETTING_UNRESTRICTED = 0;
    public static final int CALL_SETTING_PHONELINK_SCHEDULED = 1;
    private static final String PREF_CALL_INFO = "CALL_INFO";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_SCAN_TAB_MODE = "scanner_bill_mode";

    public static void putCallSetting(Context context, int callSetting) {
        share(context).edit().putInt("call_setting", callSetting).apply();
    }

    public static int getCallSetting(Context context) {
        return share(context).getInt("call_setting", CALL_SETTING_UNRESTRICTED);
    }

    public static boolean isCallSettingUnrestricted(Context context) {
        return getCallSetting(context) == CALL_SETTING_UNRESTRICTED;
    }

    public static boolean isCallSettingPhonelinkScheduled(Context context) {
        return getCallSetting(context) == CALL_SETTING_PHONELINK_SCHEDULED;
    }

    public static String getCallSettingName(Context context) {
        int setting = getCallSetting(context);
        return setting == CALL_SETTING_UNRESTRICTED ? "Unrestricted" : "Phonelink Scheduled";
    }

    public static void resetCallSettings(Context context) {
        share(context).edit().putInt("call_setting", CALL_SETTING_PHONELINK_SCHEDULED).apply();
    }

    public static void logCallSetting(Context context, String tag) {
        String currentSetting = getCallSettingName(context);
        boolean isUnrestricted = isCallSettingUnrestricted(context);
        android.util.Log.d(tag, "Call Setting: " + currentSetting +
            " (Delete call logs: " + (!isUnrestricted) + ")");
    }

    // User credentials management
    public static void clearUserCredentials(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("user_email");
        editor.remove("user_password");
        editor.remove("auth_token");
        editor.remove("auth_domain");
        editor.remove("auth_phone");
        editor.apply();
    }

    public static boolean isUserLoggedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("auth_token", "");
        return !token.isEmpty();
    }

    public static void saveCallInfo(Context context, String name) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_CALL_INFO, Context.MODE_PRIVATE);
        prefs.edit()
                .putString("CALLER_NAME", name)
                .apply();
    }

    public static String getCallerName(Context context) {
        return context.getSharedPreferences(PREF_CALL_INFO, Context.MODE_PRIVATE)
                .getString("CALLER_NAME", "Unknown");
    }

    public static void clearCallInfo(Context context) {
        context.getSharedPreferences(PREF_CALL_INFO, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    public static void putUserEmail(Context context, String email) {
        share(context).edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public static String getUserEmail(Context context) {
        return share(context).getString(KEY_USER_EMAIL, null);
    }

    public static void putScannerMode(Context context, boolean billMode) {
        share(context).edit().putBoolean(KEY_SCAN_TAB_MODE, billMode).apply();
    }

    public static boolean isBillMode(Context context) {
        return share(context).getBoolean(KEY_SCAN_TAB_MODE, false);
    }
}
