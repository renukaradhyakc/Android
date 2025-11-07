package com.thelinkphone.app.model;

import java.util.List;

public class EventResponse {
    private String token;
    private List<Event> events;

    // Getters
    public String getToken() {
        return token;
    }

    public List<Event> getEvents() {
        return events;
    }
}

