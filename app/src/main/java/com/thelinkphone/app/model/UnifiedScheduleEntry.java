package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UnifiedScheduleEntry {

    public static final String TYPE_PHONE = "phone";
    public static final String TYPE_EVENT = "event";

    public static final String DIRECTION_GIVEN_BY_ME = "given_by_me";
    public static final String DIRECTION_GIVEN_TO_ME = "given_to_me";

    @SerializedName("type")
    private String type;

    @SerializedName("id")
    private int id;

    @SerializedName("direction")
    private String direction;

    @SerializedName("other_party")
    private PartyPayload otherParty;

    @SerializedName("description")
    private String description;

    @SerializedName("cancel_reason")
    private String cancelReason;

    // ---- phone-only fields (null on event items) ----

    @SerializedName("uses_pre-existing_schedule")
    private Boolean usesPreExistingSchedule;

    @SerializedName("schedule_name")
    private String scheduleName;

    @SerializedName("times")
    private List<ScheduleSlot> times;

    // ---- event-only fields (null on phone items) ----

    @SerializedName("event_id")
    private Integer eventId;

    @SerializedName("event_name")
    private String eventName;

    @SerializedName("date")
    private String date;

    @SerializedName("slot_time")
    private String slotTime;

    @SerializedName("status")
    private String status;

    public String getType() {
        return type;
    }

    public boolean isPhone() {
        return TYPE_PHONE.equals(type);
    }

    public boolean isEvent() {
        return TYPE_EVENT.equals(type);
    }

    public int getId() {
        return id;
    }

    public String getDirection() {
        return direction;
    }

    public PartyPayload getOtherParty() {
        return otherParty;
    }

    public String getDescription() {
        return description;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public boolean usesPreExistingSchedule() {
        return usesPreExistingSchedule != null && usesPreExistingSchedule;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public List<ScheduleSlot> getTimes() {
        return times;
    }

    public Integer getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getDate() {
        return date;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public String getStatus() {
        return status;
    }
}