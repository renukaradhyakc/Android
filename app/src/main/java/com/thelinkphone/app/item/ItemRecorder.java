package com.thelinkphone.app.item;


public class ItemRecorder {
    private boolean choose = false;
    private String data;
    private String name;
    private String num;
    private long size;
    private int status;
    private long time;
    private String uri;

    public ItemRecorder(String str, String str2, String str3, String str4, long j, long j2, int i) {
        this.name = str2;
        this.data = str;
        this.num = str3;
        this.uri = str4;
        this.size = j;
        this.time = j2;
        this.status = i;
    }

    public boolean isChoose() {
        return this.choose;
    }

    public void setChoose(boolean z) {
        this.choose = z;
    }

    public String getData() {
        return this.data;
    }

    public String getName() {
        return this.name;
    }

    public String getNum() {
        return this.num;
    }

    public String getUri() {
        return this.uri;
    }

    public long getSize() {
        return this.size;
    }

    public long getTime() {
        return this.time;
    }

    public int getStatus() {
        return this.status;
    }
}
