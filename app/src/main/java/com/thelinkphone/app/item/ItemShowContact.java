package com.thelinkphone.app.item;


public class ItemShowContact {
    public String alphaB;
    public ItemContact itemContact;

    public ItemShowContact(String str) {
        this.alphaB = str;
    }

    public ItemShowContact(ItemContact itemContact) {
        this.itemContact = itemContact;
    }
}
