package com.thelinkphone.app.utils;

public class CallOutcome {
    public static final int MISSED = 0;
    public static final int BLOCKED = 1;
    public static final int OUTSIDE_SCHEDULE = 2;
    public static final int OTHER = 3; // incoming, outgoing, cancelled, voicemail — never streaked

    public static int resolve(int type, String number, long time, CallBlockReasonResolver resolver) {
        if (type == 3) return MISSED;
        if (type == 6) {
            int reason = resolver != null ? resolver.getReason(number, time) : CallBlockReason.NONE;
            if (reason == CallBlockReason.OUTSIDE_SCHEDULE || reason == CallBlockReason.API_FAILURE) {
                return OUTSIDE_SCHEDULE;
            }
            return BLOCKED;
        }
        return OTHER;
    }
}