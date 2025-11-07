package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.model.Event;
import com.thelinkphone.app.model.EventResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Utility class to manage caller identification and display names
 * Handles contact lookup, scheduled event username retrieval, and fallback display
 */
public class CallerInfoManager {
    private static final String TAG = "CallerInfoManager";
    private static final String SHARED_PREFS_NAME = "app_prefs";
    private static final String EMAIL_KEY = "user_email";
    private static final String PASSWORD_KEY = "user_password";
    private static final String DEFAULT_CALLER_NAME = "TheLinkPhone";

    // Temporary storage for LinkPhone usernames during call initiation
    private static final String LINKPHONE_USER_KEY = "linkphone_temp_username";
    private static final String LINKPHONE_PHONE_KEY = "linkphone_temp_phone";

    public interface CallerInfoCallback {
        void onCallerInfoRetrieved(CallerInfo callerInfo);
    }

    public static class CallerInfo {
        private final String displayName;
        private final CallerType type;

        public CallerInfo(String displayName, CallerType type) {
            this.displayName = displayName != null ? displayName : DEFAULT_CALLER_NAME;
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public CallerType getType() {
            return type;
        }

        public boolean isContact() {
            return type == CallerType.CONTACT;
        }

        public boolean isScheduledUser() {
            return type == CallerType.SCHEDULED_USER;
        }

        public boolean isUnknown() {
            return type == CallerType.UNKNOWN;
        }
    }

    public enum CallerType {
        CONTACT,        // Number saved in contacts
        SCHEDULED_USER, // Unknown number with username from scheduled event
        UNKNOWN         // Unknown number without any additional info
    }

    /**
     * Get caller information with priority order:
     * 1. Contact name if number is saved in contacts
     * 2. Stored LinkPhone username from deep link initiation
     * 3. Username from scheduled event if unknown number during active event
     * 4. Default "TheLinkPhone" for unknown numbers
     */
    public static void getCallerInfo(Context context, String phoneNumber, CallerInfoCallback callback) {
        Log.d(TAG, "Getting caller info for: " + phoneNumber);

        // First check if number is in contacts
        ItemContact contact = ReadContact.getContactWithNumber(context, phoneNumber);
        if (contact != null && contact.getName() != null && !contact.getName().isEmpty()) {
            Log.d(TAG, "Found contact: " + contact.getName());
            CallerInfo callerInfo = new CallerInfo(
                contact.getName(),
                CallerType.CONTACT
            );
            callback.onCallerInfoRetrieved(callerInfo);
            return;
        }

        // Check for stored LinkPhone username (from deep link)
        String storedUsername = getStoredLinkPhoneUsername(context, phoneNumber);
        if (storedUsername != null) {
            Log.d(TAG, "Found stored LinkPhone username: " + storedUsername);
            CallerInfo callerInfo = new CallerInfo(
                storedUsername,
                CallerType.SCHEDULED_USER
            );
            callback.onCallerInfoRetrieved(callerInfo);
            return;
        }

        // If not in contacts and no stored username, check if we're in scheduled event mode
        Log.d(TAG, "Number not in contacts, checking scheduled events");
        checkScheduledEventUsername(context, phoneNumber, callback);
    }

    /**
     * Check if there's an active scheduled event and try to get username
     */
    private static void checkScheduledEventUsername(Context context, String phoneNumber, CallerInfoCallback callback) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(EMAIL_KEY, "");
        String userPassword = sharedPreferences.getString(PASSWORD_KEY, "");

        // If credentials not available, use default
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Log.d(TAG, "No user credentials available, using default caller name");
            CallerInfo callerInfo = new CallerInfo(DEFAULT_CALLER_NAME, CallerType.UNKNOWN);
            callback.onCallerInfoRetrieved(callerInfo);
            return;
        }

        // Check current scheduled events
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        String currentTime = timeFormat.format(new Date());

        Log.d(TAG, "Checking scheduled events for date: " + currentDate + ", time: " + currentTime);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<EventResponse> call = apiService.getEventData(userEmail, userPassword, currentDate, currentTime);

        call.enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EventResponse eventResponse = response.body();

                    // Look for active events
                    for (Event event : eventResponse.getEvents()) {
                        if (event.isWithinTime()) {
                            Log.d(TAG, "Found active scheduled event");

                            // Check if event has username
                            String username = event.getUsername();
                            String displayName;

                            if (username != null && !username.trim().isEmpty()) {
                                displayName = username.trim();
                                Log.d(TAG, "Using username from scheduled event: " + displayName);
                            } else {
                                displayName = "Scheduled Caller";
                                Log.d(TAG, "No username in scheduled event, using default");
                            }

                            CallerInfo callerInfo = new CallerInfo(
                                displayName,
                                CallerType.SCHEDULED_USER
                            );
                            callback.onCallerInfoRetrieved(callerInfo);
                            return;
                        }
                    }
                }

                // No active event found, use default
                Log.d(TAG, "No active scheduled event found, using default");
                CallerInfo callerInfo = new CallerInfo(DEFAULT_CALLER_NAME, CallerType.UNKNOWN);
                callback.onCallerInfoRetrieved(callerInfo);
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                Log.e(TAG, "Failed to get scheduled events: " + t.getMessage());
                // On API failure, use default
                CallerInfo callerInfo = new CallerInfo(DEFAULT_CALLER_NAME, CallerType.UNKNOWN);
                callback.onCallerInfoRetrieved(callerInfo);
            }
        });
    }

    /**
     * Get caller info synchronously for immediate display
     * Checks contacts first, then stored LinkPhone usernames
     * Returns null if no info available
     */
    public static CallerInfo getContactInfoSync(Context context, String phoneNumber) {
        try {
            // First check contacts
            ItemContact contact = ReadContact.getContactWithNumber(context, phoneNumber);
            if (contact != null && contact.getName() != null && !contact.getName().isEmpty()) {
                return new CallerInfo(
                    contact.getName(),
                    CallerType.CONTACT
                );
            }

            // Then check for stored LinkPhone username
            String storedUsername = getStoredLinkPhoneUsername(context, phoneNumber);
            if (storedUsername != null) {
                return new CallerInfo(
                    storedUsername,
                    CallerType.SCHEDULED_USER
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting caller info: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get default caller info (fallback)
     */
    public static CallerInfo getDefaultCallerInfo() {
        return new CallerInfo(DEFAULT_CALLER_NAME, CallerType.UNKNOWN);
    }

    /**
     * Format display name with appropriate styling based on caller type
     */
    public static String formatDisplayName(CallerInfo callerInfo) {
        String name = callerInfo.getDisplayName();

        switch (callerInfo.getType()) {
            case CONTACT:
                // Just return the contact name as is
                return name;

            case SCHEDULED_USER:
                // Format LinkPhone username with proper capitalization
                return formatLinkPhoneUsername(name);

            case UNKNOWN:
            default:
                // Add phone icon for branding
                return "📱 " + name;
        }
    }

    /**
     * Get appropriate text color based on caller type
     */
    public static int getTextColor(CallerInfo callerInfo) {
        switch (callerInfo.getType()) {
            case CONTACT:
                return android.graphics.Color.WHITE;
            case SCHEDULED_USER:
                return android.graphics.Color.parseColor("#E3F2FD"); // Light blue
            case UNKNOWN:
            default:
                return android.graphics.Color.WHITE;
        }
    }

    /**
     * Store LinkPhone username temporarily when initiating a call
     * This allows us to display the username during the call instead of just "TheLinkPhone"
     */
    public static void storeLinkPhoneUsername(Context context, String phoneNumber, String username) {
        Log.d(TAG, "Storing LinkPhone username: " + username + " for phone: " + phoneNumber);
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString(LINKPHONE_USER_KEY, username)
                .putString(LINKPHONE_PHONE_KEY, phoneNumber)
                .apply();
    }

    /**
     * Check if there's a stored LinkPhone username for this phone number
     */
    public static String getStoredLinkPhoneUsername(Context context, String phoneNumber) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String storedPhone = prefs.getString(LINKPHONE_PHONE_KEY, "");
        String storedUsername = prefs.getString(LINKPHONE_USER_KEY, "");

        if (!storedPhone.isEmpty() && !storedUsername.isEmpty() && storedPhone.equals(phoneNumber)) {
            Log.d(TAG, "Found stored LinkPhone username: " + storedUsername + " for phone: " + phoneNumber);
            return storedUsername;
        }

        return null;
    }

    /**
     * Clear stored LinkPhone username after call ends
     */
    public static void clearStoredLinkPhoneUsername(Context context) {
        Log.d(TAG, "Clearing stored LinkPhone username");
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .remove(LINKPHONE_USER_KEY)
                .remove(LINKPHONE_PHONE_KEY)
                .apply();
    }

    /**
     * Format LinkPhone username with proper capitalization
     * Handles camelCase names and capitalizes first letter
     */
    private static String formatLinkPhoneUsername(String username) {
        if (username == null || username.isEmpty()) {
            return username;
        }

        // Handle single character usernames
        if (username.length() == 1) {
            return username.toUpperCase();
        }

        Log.d(TAG, "Formatting username: " + username);

        // If username is already in camelCase or PascalCase, leave it as is
        if (hasCapitalLetters(username)) {
            // Just ensure first letter is capitalized
            String formatted = Character.toUpperCase(username.charAt(0)) + username.substring(1);
            Log.d(TAG, "Username has capitals, formatted: " + formatted);
            return formatted;
        }

        // If all lowercase, capitalize first letter
        String formatted = Character.toUpperCase(username.charAt(0)) + username.substring(1).toLowerCase();
        Log.d(TAG, "Username was lowercase, formatted: " + formatted);
        return formatted;
    }

    /**
     * Check if string contains any capital letters (indicating camelCase/PascalCase)
     */
    private static boolean hasCapitalLetters(String str) {
        for (int i = 1; i < str.length(); i++) {
            if (Character.isUpperCase(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
