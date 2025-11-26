package com.thelinkphone.app.model;

public class ContextAndContent {

    private String callId;
    private String callerNumber;
    private String receiverNumber;
    private long timestamp;
    private boolean active;
    private int id;
    private String title;
    private String imageUrl;
    private String link;

    public ContextAndContent() {}

    public ContextAndContent(String callId, String callerNumber, String receiverNumber, long timestamp,
                     boolean active, int id, String title, String imageUrl, String link) {
        this.callId = callId;
        this.callerNumber = callerNumber;
        this.receiverNumber = receiverNumber;
        this.timestamp = timestamp;
        this.active = active;
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
        this.link = link;
    }

    public String getCallId() {
        return callId;
    }
    public String getCallerNumber() {
        return callerNumber;
    }
    public String getReceiverNumber() {
        return receiverNumber;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public boolean isActive() {
        return active;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getLink() {
        return link;
    }


    public void setCallId(String callId) {
        this.callId = callId;
    }
    public void setCallerNumber(String callerNumber) {
        this.callerNumber = callerNumber;
    }
    public void setReceiverNumber(String receiverNumber) {
        this.receiverNumber = receiverNumber;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setImageUrl(String imageRes) {
        this.imageUrl = imageRes;
    }
    public void setLink(String link) {
        this.link = link;
    }
}
