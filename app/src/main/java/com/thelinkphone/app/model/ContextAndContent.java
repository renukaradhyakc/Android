package com.thelinkphone.app.model;

public class ContextAndContent {

    private int id;
    private String title;
    private String imageUrl;  // Drawable resource ID
    private String link;   // URL link

    public ContextAndContent(int id, String title, String imageRes, String link) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageRes;
        this.link = link;
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
