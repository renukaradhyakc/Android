package com.thelinkphone.app.utils;

import android.content.Context;
import com.thelinkphone.app.utils.CallBlockReason;
import com.thelinkphone.app.item.ItemBlockReason;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CallBlockReasonResolver {

    private static final long MATCH_WINDOW_MS = 15_000;
    private final HashMap<String, List<ItemBlockReason>> indexByNumber;
    private CallBlockReasonResolver(HashMap<String, List<ItemBlockReason>> indexByNumber) {
        this.indexByNumber = indexByNumber;
    }

    public static CallBlockReasonResolver load(Context context) {
        ArrayList<ItemBlockReason> records = MyShare.getBlockReasons(context);
        HashMap<String, List<ItemBlockReason>> index = new HashMap<>();
        for (ItemBlockReason r : records) {
            String key = normalize(r.getNumber());
            List<ItemBlockReason> bucket = index.get(key);
            if (bucket == null) {
                bucket = new ArrayList<>();
                index.put(key, bucket);
            }
            bucket.add(r);
        }
        return new CallBlockReasonResolver(index);
    }

    public int getReason(String number, long callTime) {
        if (number == null || number.isEmpty()) return CallBlockReason.NONE;
        List<ItemBlockReason> bucket = indexByNumber.get(normalize(number));
        if (bucket == null) return CallBlockReason.NONE;
        for (ItemBlockReason r : bucket) {
            if (Math.abs(r.getTimestamp() - callTime) <= MATCH_WINDOW_MS) {
                return r.getReason();
            }
        }
        return CallBlockReason.NONE;
    }
    
    private static String normalize(String rawNumber) {
        return ReadContact.normalizeNumber(rawNumber);
    }
}