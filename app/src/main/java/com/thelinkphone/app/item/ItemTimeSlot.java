package com.thelinkphone.app.item;

public class ItemTimeSlot {
    public String id; // local UI id (UUID), not a backend id until saved
    public String fromTime; // "07:00 AM"
    public String toTime;   // "09:00 PM"

    public ItemTimeSlot(String fromTime, String toTime) {
        this.id = java.util.UUID.randomUUID().toString();
        this.fromTime = fromTime;
        this.toTime = toTime;
    }
}