package com.thelinkphone.app.item;

public class ItemScheduleOption {
    public int id;
    public String name;

    public ItemScheduleOption(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name; // so Spinner's default adapter shows the name
    }
}