package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.thelinkphone.app.model.LoginResponse;


public class SessionManager {

    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "app_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PASSWORD = "user_password";
    private static final String KEY_DOMAIN = "auth_domain";
    private static final String KEY_PHONE = "auth_phone";
    private static volatile SessionManager instance;
    private volatile SessionData sessionData;
    private final Object lock = new Object();
    private final SharedPreferences prefs;
    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        loadSession();
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    private void loadSession() {
        synchronized (lock) {
            String token = prefs.getString(KEY_TOKEN, null);
            String email = prefs.getString(KEY_EMAIL, null);
            String password = prefs.getString(KEY_PASSWORD, null);
            String domain = prefs.getString(KEY_DOMAIN, null);
            String phone = prefs.getString(KEY_PHONE, null);

            sessionData = new SessionData(token, email, password, domain, phone);

            Log.d(TAG, "Session loaded - Token: " + (token != null ? "present" : "null") +
                    ", Email: " + (email != null ? "present" : "null"));
        }
    }

    public void saveSession(LoginResponse response, String email, String password) {
        synchronized (lock) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_TOKEN, response.getToken());
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_PASSWORD, password);
            editor.putString(KEY_DOMAIN, response.getDomain_url());
            editor.putString(KEY_PHONE, response.getPhone_number());
            editor.apply();

            sessionData = new SessionData(
                    response.getToken(),
                    email,
                    password,
                    response.getDomain_url(),
                    response.getPhone_number()
            );

            Log.d(TAG, "Session saved successfully");
        }
    }

    public void updateToken(String newToken) {
        synchronized (lock) {
            prefs.edit().putString(KEY_TOKEN, newToken).apply();
            sessionData = new SessionData(
                    newToken,
                    sessionData.email,
                    sessionData.password,
                    sessionData.domain,
                    sessionData.phone
            );
            Log.d(TAG, "Token updated");
        }
    }

    public boolean isLoggedIn() {
        synchronized (lock) {
            return sessionData.token != null &&
                    sessionData.email != null &&
                    !sessionData.email.trim().isEmpty();
        }
    }

    public SessionData getSession() {
        synchronized (lock) {
            return sessionData;
        }
    }

    public String getToken() {
        synchronized (lock) {
            return sessionData.token;
        }
    }

    public String getEmail() {
        synchronized (lock) {
            return sessionData.email;
        }
    }

    public String getPhone() {
        synchronized (lock) {
            return sessionData.phone;
        }
    }

    public String getDomain() {
        synchronized (lock) {
            return sessionData.domain;
        }
    }

    public void clearSession() {
        synchronized (lock) {
            prefs.edit().clear().apply();
            sessionData = new SessionData(null, null, null, null, null);
            Log.d(TAG, "Session cleared");
        }
    }

    public void reloadSession() {
        Log.d(TAG, "Reloading session from disk");
        loadSession();
    }

    public static class SessionData {
        public final String token;
        public final String email;
        public final String password;
        public final String domain;
        public final String phone;

        SessionData(String token, String email, String password, String domain, String phone) {
            this.token = token;
            this.email = email;
            this.password = password;
            this.domain = domain;
            this.phone = phone;
        }

        public boolean isValid() {
            return token != null && email != null && !email.trim().isEmpty();
        }

        @Override
        public String toString() {
            return "SessionData{" +
                    "token=" + (token != null ? "***" : "null") +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", domain='" + domain + '\'' +
                    '}';
        }
    }
}