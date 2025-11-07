package com.thelinkphone.app.utils;

import com.thelinkphone.app.model.Event;

public interface CheckEventTimeListener {
    void onEventCheckComplete(Event event,boolean apiFailed);
}

