package com.thelinkphone.app.item;

import android.content.Context;

import com.thelinkphone.app.model.PartyPayload;
import com.thelinkphone.app.utils.MyConst;
import com.thelinkphone.app.utils.ReadContact;

public final class ItemPartyDisplay {

    public static String name(Context context, PartyPayload party) {
        if (party == null) return "-";

        if (party.getPhoneNumber() != null) {
            com.thelinkphone.app.item.ItemContact contact =
                    ReadContact.getContactWithNumber(context, party.getPhoneNumber());
            if (contact != null && contact.getName() != null && !contact.getName().trim().isEmpty()) {
                return contact.getName();
            }
        }

        if (party.getName() != null && !party.getName().trim().isEmpty()) {
            return party.getName();
        }

        return party.getPhoneNumber() != null ? party.getPhoneNumber() : "-";
    }

    public static String link(PartyPayload party) {
        if (party == null || !party.isCallalinkUser()) return "-";
        String domainUrl = party.getDomainUrl();
        if (domainUrl == null || domainUrl.isEmpty()) return "-";
        return MyConst.CALL_URL + domainUrl;
    }

    private ItemPartyDisplay() {}
}