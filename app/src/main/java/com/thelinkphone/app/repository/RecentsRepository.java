package com.thelinkphone.app.repository;

import android.util.Log;

import com.thelinkphone.app.item.ItemRecentGroup;

import java.util.ArrayList;

public class RecentsRepository {

    private static ArrayList<ItemRecentGroup> cache;
    private static long lastLoadedTime;

    public static ArrayList<ItemRecentGroup> getCache() {
        return cache;
    }

    public static void setCache(ArrayList<ItemRecentGroup> data) {
        Log.d("RECENTS_CACHE", "setCache() size=" + data.size());
        cache = data;
        lastLoadedTime = System.currentTimeMillis();
    }

    public static boolean hasCache() {

        boolean result = cache != null && !cache.isEmpty();
        Log.d("RECENTS_CACHE", "hasCache() = " + result);
        return result;
    }

    public static long getLastLoadedTime() {
        return lastLoadedTime;
    }

    public static void clear() {
        cache = null;
        lastLoadedTime = 0;
    }
}