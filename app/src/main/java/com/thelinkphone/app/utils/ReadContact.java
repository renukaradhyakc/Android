package com.thelinkphone.app.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.item.ItemRecent;
import com.thelinkphone.app.item.ItemRecentGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;


public class ReadContact {
    @SuppressLint("Range")
    public static ItemContact getContact(Context context, String str) {
        ContentResolver contentResolver = context.getContentResolver();
        Cursor query = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{"display_name", "_id", "photo_uri"}, "_id = ?", new String[]{str}, null);
        if ((query != null ? query.getCount() : 0) <= 0 || !query.moveToNext()) {
            if (query != null) {
                query.close();
                return null;
            }
            return null;
        }
        return new ItemContact(str, query.getString(query.getColumnIndex("display_name")), query.getString(query.getColumnIndex("photo_uri")), getPhone(contentResolver, str));
    }

    public static ItemContact getContactWithNumber(Context context, String str) {
        String idWithNumber = getIdWithNumber(context, str);
        if (idWithNumber == null || idWithNumber.isEmpty()) {
            return null;
        }
        return getContact(context, idWithNumber);
    }
    @SuppressLint("Range")
    public static ArrayList<ItemContact> getAllContact(Context context) {
        ArrayList<ItemContact> arrayList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        try {
            Cursor query = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{"display_name", "_id", "photo_uri"}, null, null, null);
            if ((query != null ? query.getCount() : 0) > 0) {
                while (query.moveToNext()) {
                   String string = query.getString(query.getColumnIndex("display_name"));
                    String string2 = query.getString(query.getColumnIndex("_id"));
                    String string3 = query.getString(query.getColumnIndex("photo_uri"));
                    if (string != null && !string.isEmpty()) {
                        arrayList.add(new ItemContact(string2, string, string3, getPhone(contentResolver, string2)));
                    }
                }
            }
            if (query != null) {
                query.close();
            }
        } catch (SecurityException unused) {
        }
        Collections.sort(arrayList, ReadContact$$ExternalSyntheticLambda2.INSTANCE);
        return arrayList;
    }
    @SuppressLint("Range")
    public static ArrayList<ItemPhone> getPhone(ContentResolver contentResolver, String str) {
        ArrayList<ItemPhone> arrayList = new ArrayList<>();
        Cursor query = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{"data1", "data2"}, "contact_id = ?", new String[]{str}, null);
        if (query != null) {
            while (query.moveToNext()) {
                arrayList.add(new ItemPhone(query.getString(query.getColumnIndex("data1")), query.getInt(query.getColumnIndex("data2"))));
            }
            query.close();
        }
        return arrayList;
    }

    public static String[] getNamePhoto(Context context, String str) {
        String[] strArr = new String[2];
        try {
            if (!str.isEmpty()) {
                Cursor query = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(str)), new String[]{"display_name", "photo_uri"}, null, null, null);
                if (query != null) {
                    while (query.moveToNext()) {
                        strArr[0] = query.getString(0);
                        strArr[1] = query.getString(1);
                    }
                    query.close();
                }
            }
        } catch (Exception unused) {
        }
        if (strArr[0] == null) {
            strArr[0] = "";
        }
        if (strArr[1] == null) {
            strArr[1] = "";
        }
        return strArr;
    }

    public static String getIdWithNumber(Context context, String str) {
        String str2 = "";
        try {
            Cursor query = context.getContentResolver().query(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(str)), new String[]{"_id"}, null, null, null);
            if (query != null) {
                while (query.moveToNext()) {
                    str2 = query.getString(query.getColumnIndexOrThrow("_id"));
                }
                query.close();
            }
        } catch (SecurityException unused) {
        }
        return str2;
    }
    @SuppressLint("Range")
    public static ArrayList<ItemRecentGroup> getAllRecents(Context context) {
        long start = System.currentTimeMillis();
        android.util.Log.d("RECENTS_PERF", "getAllRecents START");
        int count = 0;
        boolean z;
        ItemRecent itemRecent;
        Locale locale;
        long j;
        ItemRecent itemRecent2;
        ArrayList<ItemRecentGroup> arrayList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        if (context.checkSelfPermission("android.permission.READ_CALL_LOG") != PackageManager.PERMISSION_GRANTED) {
            return arrayList;
        }
        long queryStart = System.currentTimeMillis();
        Cursor query = contentResolver.query(CallLog.Calls.CONTENT_URI, new String[]{"_id", "number", "subscription_id", "duration", "date", "countryiso", "type", "photo_uri", "name", "numberlabel"}, null, null, "date DESC");
        android.util.Log.d("RECENTS_PERF", "query created in " + (System.currentTimeMillis() - queryStart) + " ms");
        int i = 0;
        if (query != null) {
            try {
                count = query.getCount();
            } catch (SecurityException unused) {
            }
        } else {
            count = 0;
        }
        android.util.Log.d("RECENTS_PERF", "call log count = " + count);
        if (count > 0) {
            Locale locale2 = context.getResources().getConfiguration().locale;
            Calendar calendar = Calendar.getInstance();
            long loopStart = System.currentTimeMillis();
            while (query.moveToNext()) {
               String string = query.getString(query.getColumnIndex("_id"));
                String string2 = query.getString(query.getColumnIndex("number"));
                String string3 = query.getString(query.getColumnIndex("subscription_id"));
                long j2 = query.getLong(query.getColumnIndex("duration"));
                long j3 = query.getLong(query.getColumnIndex("date"));
                String string4 = query.getString(query.getColumnIndex("countryiso"));
                if (string4 != null && !string4.isEmpty()) {
                    string4 = locale2.getDisplayCountry(new Locale(string4));
                }
                int i2 = query.getInt(query.getColumnIndex("type"));
                String string5 = query.getString(query.getColumnIndex("photo_uri"));
                String string6 = query.getString(query.getColumnIndex("name"));
                int i3 = i2;
                long j4 = j3;
                ItemRecent itemRecent3 = new ItemRecent(string, string2, string3, j2, j3, string4, i3, query.getString(query.getColumnIndex("numberlabel")));
                Iterator<ItemRecentGroup> it = arrayList.iterator();
                while (true) {
                    z = true;
                    if (!it.hasNext()) {
                        itemRecent = itemRecent3;
                        locale = locale2;
                        break;
                    }
                    ItemRecentGroup next = it.next();
                    ItemRecent itemRecent4 = next.arrRecent.get(i);
                    int i4 = i3;
                    if (itemRecent4.type != i4) {
                        if (itemRecent4.type != 3 && i4 != 3) {
                        }
                        itemRecent2 = itemRecent3;
                        locale = locale2;
                        j = j4;
                        itemRecent3 = itemRecent2;
                        i3 = i4;
                        j4 = j;
                        locale2 = locale;
                        i = 0;
                    }
                    if (itemRecent4.number.equals(string2)) {
                        j = j4;
                        calendar.setTimeInMillis(j);
                        int i5 = calendar.get(1);
                        int i6 = calendar.get(6);
                        locale = locale2;
                        calendar.setTimeInMillis(itemRecent4.time);
                        if (i5 == calendar.get(1) && i6 == calendar.get(6)) {
                            itemRecent = itemRecent3;
                            next.addRecent(itemRecent);
                            z = false;
                            break;
                        }
                        itemRecent2 = itemRecent3;
                        itemRecent3 = itemRecent2;
                        i3 = i4;
                        j4 = j;
                        locale2 = locale;
                        i = 0;
                    }
                    itemRecent2 = itemRecent3;
                    locale = locale2;
                    j = j4;
                    itemRecent3 = itemRecent2;
                    i3 = i4;
                    j4 = j;
                    locale2 = locale;
                    i = 0;
                }
                if (z) {
                    arrayList.add(new ItemRecentGroup(itemRecent, string6, string5));
                }
                locale2 = locale;
                i = 0;
            }
            query.close();
            android.util.Log.d("RECENTS_PERF", "loop finished in " + (System.currentTimeMillis() - loopStart) + " ms");
            android.util.Log.d("RECENTS_PERF", "TOTAL = " + (System.currentTimeMillis() - start) + " ms");
            android.util.Log.d("RECENTS_PERF", "count = " + count);
        }
        return arrayList;
    }

    public static void removeRecents(final Context context, final String[] strArr) {
        new Thread(new Runnable() {
            @Override
            public final void run() {
                ReadContact.lambda$removeRecents$1(context, strArr);
            }
        }).start();
    }


    public static  void lambda$removeRecents$1(Context context, String[] strArr) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.delete(CallLog.Calls.CONTENT_URI, "_id in (" + TextUtils.join(",", strArr) + ")", null);
        } catch (Exception unused) {
        }
    }

    public static void removeAllRecents(final Context context) {
        new Thread(new Runnable() {
            @Override
            public final void run() {
                context.getContentResolver().delete(CallLog.Calls.CONTENT_URI, null, null);
            }
        }).start();
    }

    public static void removeMostRecentCallLog(final Context context, final String phoneNumber) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ContentResolver contentResolver = context.getContentResolver();
                    // Query for the most recent call log entry for this phone number
                    Cursor cursor = contentResolver.query(
                        CallLog.Calls.CONTENT_URI,
                        new String[]{"_id"},
                        "number = ?",
                        new String[]{phoneNumber},
                        "date DESC LIMIT 1"
                    );

                    if (cursor != null && cursor.moveToFirst()) {
                        String callId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
                        // Delete this specific call log entry
                        contentResolver.delete(
                            CallLog.Calls.CONTENT_URI,
                            "_id = ?",
                            new String[]{callId}
                        );
                    }

                    if (cursor != null) {
                        cursor.close();
                    }

                    // Clear missed call notifications, count, and badge
                    clearMissedCallBadge(context, phoneNumber);

                } catch (Exception e) {
                    // Handle any exceptions silently
                }
            }
        }).start();
    }

    public static void clearMissedCallNotifications(Context context, String phoneNumber) {
        try {
            android.util.Log.d("ReadContact", "Clearing missed call notifications for: " + phoneNumber);

            // Clear missed call notifications using NotificationManager
            android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                // Cancel all notifications with the "missed call" tag or category
                notificationManager.cancel("missed_call", phoneNumber.hashCode());
                notificationManager.cancel("call", phoneNumber.hashCode());

                // Cancel notifications by common missed call IDs
                for (int i = 1; i <= 100; i++) {
                    notificationManager.cancel(i);
                }
            }

            // Use TelecomManager to clear missed call count more effectively
            clearMissedCallCountWithTelecom(context, phoneNumber);

            // Clear the missed call count by marking calls as "read"
            clearMissedCallCount(context, phoneNumber);

        } catch (Exception e) {
            android.util.Log.e("ReadContact", "Error clearing missed call notifications: " + e.getMessage());
        }
    }

    private static void clearMissedCallCount(Context context, String phoneNumber) {
        try {
            ContentResolver contentResolver = context.getContentResolver();

            // Update all missed calls for this number to mark them as "seen"
            android.content.ContentValues values = new android.content.ContentValues();
            values.put("new", 0); // Mark as not new/seen
            values.put("is_read", 1); // Mark as read

            contentResolver.update(
                CallLog.Calls.CONTENT_URI,
                values,
                "number = ? AND type = ?",
                new String[]{phoneNumber, String.valueOf(CallLog.Calls.MISSED_TYPE)}
            );

            android.util.Log.d("ReadContact", "Cleared missed call count for: " + phoneNumber);

        } catch (Exception e) {
            android.util.Log.e("ReadContact", "Error clearing missed call count: " + e.getMessage());
        }
    }

    private static void clearMissedCallCountWithTelecom(Context context, String phoneNumber) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.telecom.TelecomManager telecomManager =
                    (android.telecom.TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                if (telecomManager != null && context.getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
                    // As default dialer, we can manipulate call log more effectively
                    android.util.Log.d("ReadContact", "Using TelecomManager to clear missed call count for: " + phoneNumber);

                    // Clear missed call count by updating call log entries
                    ContentResolver contentResolver = context.getContentResolver();
                    android.content.ContentValues values = new android.content.ContentValues();
                    values.put(CallLog.Calls.NEW, 0);
                    values.put(CallLog.Calls.IS_READ, 1);

                    int updatedRows = contentResolver.update(
                        CallLog.Calls.CONTENT_URI,
                        values,
                        CallLog.Calls.NUMBER + " = ? AND " + CallLog.Calls.TYPE + " = ? AND " + CallLog.Calls.NEW + " = 1",
                        new String[]{phoneNumber, String.valueOf(CallLog.Calls.MISSED_TYPE)}
                    );

                    android.util.Log.d("ReadContact", "Updated " + updatedRows + " missed call entries for: " + phoneNumber);
                }
            }
        } catch (Exception e) {
            android.util.Log.e("ReadContact", "Error using TelecomManager to clear missed calls: " + e.getMessage());
        }
    }

    public static void clearAllMissedCallNotifications(final Context context) {
        // Simplified version to prevent ANR
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    android.util.Log.d("ReadContact", "Clearing all missed call notifications");

                    // Quick notification clear
                    android.app.NotificationManager notificationManager =
                        (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    if (notificationManager != null) {
                        notificationManager.cancel("missed_call", 0);
                    }

                    // Simple missed call update
                    ContentResolver contentResolver = context.getContentResolver();
                    android.content.ContentValues values = new android.content.ContentValues();
                    values.put(CallLog.Calls.NEW, 0);

                    contentResolver.update(
                        CallLog.Calls.CONTENT_URI,
                        values,
                        CallLog.Calls.TYPE + " = ? AND " + CallLog.Calls.NEW + " = 1",
                        new String[]{String.valueOf(CallLog.Calls.MISSED_TYPE)}
                    );

                } catch (Exception e) {
                    android.util.Log.e("ReadContact", "Error clearing all missed call notifications: " + e.getMessage());
                }
            }
        }).start();
    }

    public static void clearMissedCallBadge(final Context context, final String phoneNumber) {
        // Already running in background thread from CallService - no need for new thread
        try {
            android.util.Log.d("ReadContact", "Clearing missed call badge for: " + phoneNumber);

            // Use only the most effective method to avoid ANR
            clearSystemMissedCallBadge(context, phoneNumber);

            // Quick notification clear without heavy operations
            android.app.NotificationManager notificationManager =
                (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel("missed_call", phoneNumber.hashCode());
                notificationManager.cancel(phoneNumber.hashCode());
            }

        } catch (Exception e) {
            android.util.Log.e("ReadContact", "Error clearing missed call badge: " + e.getMessage());
        }
    }

    private static void clearSystemMissedCallBadge(Context context, String phoneNumber) {
        try {
            // Simplified and faster approach
            ContentResolver contentResolver = context.getContentResolver();
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(CallLog.Calls.NEW, 0);
            values.put(CallLog.Calls.IS_READ, 1);

            // Simple exact match to avoid heavy LIKE operations
            int updated = contentResolver.update(
                CallLog.Calls.CONTENT_URI,
                values,
                CallLog.Calls.NUMBER + " = ? AND " + CallLog.Calls.NEW + " = 1",
                new String[]{phoneNumber}
            );

            android.util.Log.d("ReadContact", "Updated " + updated + " missed call entries for: " + phoneNumber);

        } catch (Exception e) {
            android.util.Log.e("ReadContact", "Error clearing system missed call badge: " + e.getMessage());
        }
    }

    // Removed to prevent ANR - functionality moved to clearSystemMissedCallBadge

    // Removed to prevent ANR - basic notification clearing moved to clearMissedCallBadge

    public static void forceDeleteRecentCallLogs(final Context context, final String phoneNumber) {
        // Force delete recent call logs for blocked calls - runs in background
        try {
            android.util.Log.d("ReadContact", "Force deleting recent call logs for: " + phoneNumber);

            ContentResolver contentResolver = context.getContentResolver();

            // Delete all call log entries for this number from the last 5 minutes
            long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);

            int deletedRows = contentResolver.delete(
                CallLog.Calls.CONTENT_URI,
                CallLog.Calls.NUMBER + " = ? AND " + CallLog.Calls.DATE + " > ?",
                new String[]{phoneNumber, String.valueOf(fiveMinutesAgo)}
            );

            android.util.Log.d("ReadContact", "Force deleted " + deletedRows + " recent call log entries for: " + phoneNumber);

            // Also clear any missed call badges
            if (deletedRows > 0) {
                clearSystemMissedCallBadge(context, phoneNumber);
            }

        } catch (Exception e) {
            android.util.Log.e("ReadContact", "Error force deleting recent call logs: " + e.getMessage());
        }
    }
}
