package com.thelinkphone.app.utils;

public final class CallBlockReason {
    private CallBlockReason() {}
    public static final int NONE = 0;
    public static final int OUTSIDE_SCHEDULE = 1;
    public static final int API_FAILURE = 2;
    // MANUAL not stored — no history needed, generic "Call is blocked" text is already accurate
}