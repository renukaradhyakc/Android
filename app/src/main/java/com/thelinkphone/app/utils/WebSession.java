package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.webkit.CookieManager;

import com.thelinkphone.app.model.WebviewTicketResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public final class WebSession {

    public interface Listener {
        void onDone(boolean ok);
    }

    private static final String TAG = "WebSession";
    private static final String PREFS = "app_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final long REUSE_MS = 5 * 60 * 1000L;
    private static final long RECOVER_GAP_MS = 30 * 1000L;
    private static final long JUST_REFRESHED_MS = 10 * 1000L;

    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final List<Listener> WAITING = new ArrayList<>();
    private static final Map<String, Long> LAST_RECOVER = new HashMap<>();

    private static okhttp3.OkHttpClient bridgeClient;
    private static boolean running = false;
    private static String lastAuth = null;
    private static long lastOkAt = 0;

    private WebSession() {
    }

    public static void ensure(Context context, boolean force, Listener listener) {
        final String auth = authHeader(context.getApplicationContext());

        if (auth == null) {
            Log.d(TAG, "no login token, nothing to bridge");
            listener.onDone(false);
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (!force && auth.equals(lastAuth) && lastOkAt != 0 && now - lastOkAt < REUSE_MS) {
            listener.onDone(true);
            return;
        }

        WAITING.add(listener);
        if (running) {
            return;
        }
        running = true;
        requestTicket(auth);
    }

    public static void recover(Context context, Listener listener) {
        boolean justRefreshed = lastOkAt != 0
                && SystemClock.elapsedRealtime() - lastOkAt < JUST_REFRESHED_MS;
        ensure(context, !justRefreshed, listener);
    }

    public static void reset() {
        lastAuth = null;
        lastOkAt = 0;
        CookieManager cm = CookieManager.getInstance();
        cm.removeAllCookies(null);
        cm.flush();
    }

    public static boolean isLoginUrl(String url) {
        if (url == null) return false;
        Uri u = Uri.parse(url);
        String ourHost = Uri.parse(MyConst.WEB_BASE_URL).getHost();
        if (ourHost == null || !ourHost.equalsIgnoreCase(u.getHost())) return false;
        String path = u.getPath();
        return path != null && path.replaceAll("/+$", "").endsWith("/login");
    }

    public static boolean allowRecover(String key) {
        long now = SystemClock.elapsedRealtime();
        Long last = LAST_RECOVER.get(key);
        if (last != null && now - last < RECOVER_GAP_MS) {
            return false;
        }
        LAST_RECOVER.put(key, now);
        return true;
    }

    private static void requestTicket(final String auth) {
        ApiClient.getClient().create(ApiService.class)
                .getWebviewTicket(auth)
                .enqueue(new Callback<WebviewTicketResponse>() {
                    @Override
                    public void onResponse(Call<WebviewTicketResponse> call, Response<WebviewTicketResponse> response) {
                        String ticket = null;
                        if (response.isSuccessful() && response.body() != null
                                && response.body().getUrl() != null) {
                            ticket = Uri.parse(response.body().getUrl()).getQueryParameter("ticket");
                        }
                        if (ticket == null || ticket.isEmpty()) {
                            Log.w(TAG, "ticket refused, HTTP " + response.code());
                            postFinish(auth, false, null);
                            return;
                        }
                        Log.d(TAG, "ticket received");
                        consumeTicket(auth, ticket);
                    }

                    @Override
                    public void onFailure(Call<WebviewTicketResponse> call, Throwable t) {
                        Log.w(TAG, "ticket request failed: " + t.getMessage());
                        postFinish(auth, false, null);
                    }
                });
    }

    private static void consumeTicket(final String auth, String ticket) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(MyConst.WEB_BASE_URL + "native-bridge?ticket=" + Uri.encode(ticket) + "&to=%2Fdashboard")
                .header("Accept", "text/html")
                .get()
                .build();

        bridgeClient().newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.w(TAG, "bridge call failed: " + e.getMessage());
                postFinish(auth, false, null);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                int code = response.code();
                List<String> cookies = response.headers("Set-Cookie");
                response.close();
                if (code == 302 && !cookies.isEmpty()) {
                    postFinish(auth, true, cookies);
                } else {
                    Log.w(TAG, "bridge refused, HTTP " + code);
                    postFinish(auth, false, null);
                }
            }
        });
    }

    private static void postFinish(final String auth, final boolean ok, final List<String> cookies) {
        MAIN.post(() -> {
            if (ok && cookies != null) {
                CookieManager cm = CookieManager.getInstance();
                cm.setAcceptCookie(true);
                for (String c : cookies) {
                    cm.setCookie(MyConst.WEB_BASE_URL, c);
                }
                cm.flush();
                lastAuth = auth;
                lastOkAt = SystemClock.elapsedRealtime();
                Log.d(TAG, "web session ready, cookies set: " + cookies.size());
            }
            running = false;
            List<Listener> copy = new ArrayList<>(WAITING);
            WAITING.clear();
            for (Listener l : copy) {
                l.onDone(ok);
            }
        });
    }

    private static okhttp3.OkHttpClient bridgeClient() {
        if (bridgeClient == null) {
            okhttp3.Call.Factory factory = ApiClient.getClient().callFactory();
            okhttp3.OkHttpClient base = (factory instanceof okhttp3.OkHttpClient)
                    ? (okhttp3.OkHttpClient) factory
                    : new okhttp3.OkHttpClient();
            okhttp3.OkHttpClient.Builder b = base.newBuilder();
            b.interceptors().clear();
            bridgeClient = b.followRedirects(false).followSslRedirects(false).build();
        }
        return bridgeClient;
    }

    private static String authHeader(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        token = token.trim();
        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    public static boolean isStale(Context context) {
        String auth = authHeader(context.getApplicationContext());
        return auth != null && !auth.equals(lastAuth);
    }
}