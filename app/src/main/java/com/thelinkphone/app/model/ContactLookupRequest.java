package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ContactLookupRequest {
    @SerializedName("numbers")
    private List<String> numbers;

    public ContactLookupRequest(List<String> numbers) {
        this.numbers = numbers;
    }
}