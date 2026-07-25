package com.thelinkphone.app.repository;

import android.content.Context;
import android.util.Log;

import com.thelinkphone.app.model.ContactLookupRequest;
import com.thelinkphone.app.model.ContactLookupResponse;
import com.thelinkphone.app.model.ContactLookupResult;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactLookupRepository {

    private static final int BATCH_SIZE = 1000;
    private static final String SHARED_PREFS_NAME = "app_prefs";
    private static final String TOKEN_KEY = "auth_token";

    public interface LookupCompletedListener {
        void onLookupCompleted(Set<String> updatedNumbers);
    }

    public static void resolve(Context context, List<String> numbers, LookupCompletedListener listener) {
        Set<String> unique = new HashSet<>(numbers);
        List<String> missing = new ArrayList<>();

        for (String number : unique) {
            if (ContactLookupCache.get(context, number) == null) {
                missing.add(number);
            }
        }

        Log.d("CONTACT_LOOKUP_REPO", "total=" + unique.size() + " missing=" + missing.size());

        if (missing.isEmpty()) {
            listener.onLookupCompleted(new HashSet<>());
            return;
        }

        String token = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
                .getString(TOKEN_KEY, "");

        if (token.isEmpty()) {
            Log.w("CONTACT_LOOKUP_REPO", "no auth token — skipping lookup");
            listener.onLookupCompleted(new HashSet<>());
            return;
        }

        fetchInBatches(context, missing, token, listener);
    }

    private static void fetchInBatches(Context context, List<String> missing, String token, LookupCompletedListener listener) {
        List<List<String>> chunks = partition(missing, BATCH_SIZE);
        AtomicInteger remaining = new AtomicInteger(chunks.size());
        Set<String> updatedNumbers = java.util.Collections.synchronizedSet(new HashSet<>());
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        for (List<String> chunk : chunks) {
            apiService.lookupContacts("Bearer " + token, new ContactLookupRequest(chunk))
            .enqueue(new Callback<ContactLookupResponse>() {
                @Override
                public void onResponse(Call<ContactLookupResponse> call, Response<ContactLookupResponse> response) {
                    if (response.isSuccessful() && response.body() != null
                            && response.body().getResults() != null) {
                        Map<String, ContactLookupResult> results = response.body().getResults();
                        for (String requestedNumber : chunk) {
                            if (!results.containsKey(requestedNumber)) {
                                ContactLookupResult unresolved = new ContactLookupResult();
                                unresolved.setUser(false);
                                results.put(requestedNumber, unresolved);
                            }
                        }
                        ContactLookupCache.putAllInMemory(context, results);
                        updatedNumbers.addAll(results.keySet());
                    } else {
                        Log.e("CONTACT_LOOKUP_REPO", "lookup failed, code=" + response.code());
                    }
                    finishIfDone();
                }

                @Override
                public void onFailure(Call<ContactLookupResponse> call, Throwable t) {
                    Log.e("CONTACT_LOOKUP_REPO", "lookup network error", t);
                    finishIfDone();
                }

                private void finishIfDone() {
                    if (remaining.decrementAndGet() == 0) {
                        ContactLookupCache.persist(context);
                        listener.onLookupCompleted(updatedNumbers);
                    }
                }
            });
        }
    }

    private static List<List<String>> partition(List<String> list, int size) {
        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            chunks.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return chunks;
    }
}