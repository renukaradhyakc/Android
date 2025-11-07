package com.thelinkphone.app.fragment;

import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemRecentGroup;


public interface ContactResult {
    void onAddNewContact(ItemRecentGroup itemRecentGroup, ItemContact itemContact);

    void onBack();

    void onContactChange();

    void onFavoritesChange();
}
