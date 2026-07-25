package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class ContactLookupResponse {
    @SerializedName("results")
    private Map<String, ContactLookupResult> results;

    public Map<String, ContactLookupResult> getResults() {
        return results;
    }
}