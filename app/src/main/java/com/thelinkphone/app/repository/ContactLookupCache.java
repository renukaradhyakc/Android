package com.thelinkphone.app.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thelinkphone.app.model.ContactLookupResult;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ContactLookupCache {

    private static final String PREF_NAME = "contact_lookup_cache";
    private static final String KEY_DATA = "data";

    // Asymmetric on purpose: positive entries are stable (people rarely leave
    // CallaLink); negative entries flip whenever someone new signs up, so they
    // need to go stale sooner.
    private static final long POSITIVE_TTL_MS = TimeUnit.HOURS.toMillis(24);
    private static final long NEGATIVE_TTL_MS = TimeUnit.HOURS.toMillis(6);

    private static Map<String, ContactLookupResult> cache;
    private static boolean loaded = false;

    private static synchronized void ensureLoaded(Context context) {
        if (loaded) return;

        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_DATA, null);

        if (json != null) {
            Type type = new TypeToken<HashMap<String, ContactLookupResult>>() {}.getType();
            cache = new Gson().fromJson(json, type);
        }
        if (cache == null) cache = new HashMap<>();
        loaded = true;

        Log.d("CONTACT_LOOKUP_CACHE", "loaded size=" + cache.size());
    }

    /** Returns null on cache miss OR expired entry — caller treats both identically. */
    public static synchronized ContactLookupResult get(Context context, String normalizedNumber) {
        ensureLoaded(context);

        ContactLookupResult entry = cache.get(normalizedNumber);
        if (entry == null) return null;

        long ttl = entry.isUser() ? POSITIVE_TTL_MS : NEGATIVE_TTL_MS;
        if (System.currentTimeMillis() - entry.getCheckedAt() > ttl) {
            return null;
        }
        return entry;
    }

    /** Memory update only — call persist() once, after all batches finish. */
    public static synchronized void putAllInMemory(Context context, Map<String, ContactLookupResult> results) {
        ensureLoaded(context);

        long now = System.currentTimeMillis();
        for (Map.Entry<String, ContactLookupResult> entry : results.entrySet()) {
            entry.getValue().setCheckedAt(now);
            cache.put(entry.getKey(), entry.getValue());
        }
    }

    /** Single disk write — call after all batches complete, success or failure. */
    public static synchronized void persist(Context context) {
        ensureLoaded(context);
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DATA, new Gson().toJson(cache)).apply();

        Log.d("CONTACT_LOOKUP_CACHE", "persisted size=" + cache.size());
    }

    public static synchronized void clearCache(Context context) {
        cache = new HashMap<>();
        loaded = true;
        persist(context);
    }
}