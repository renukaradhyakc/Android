package com.thelinkphone.app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;

import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.interfaces.LogInCallback;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;
import com.thelinkphone.app.model.LoginResponse;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private ApiService apiService;
    private static final String TAG = "LoginActivity";

    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_NAME = "app_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String DOMAIN_KEY = "auth_domain";
    private static final String PHONE_KEY = "auth_phone";
    private static final String EMAIL_KEY = "user_email";
    private static final String PASSWORD_KEY = "user_password";
    private static final String ENTITLEMENT_ID = "CallALink Premium";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        apiService = ApiClient.getClient().create(ApiService.class);
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        Call<LoginResponse> call = apiService.login(email, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.getToken() != null) {
                        // Login successful, proceed with WebView loading
                     //   loadWebView(loginResponse.getToken());
                        saveToken(loginResponse.getToken(),loginResponse.getDomain_url(), loginResponse.getPhone_number());
                        saveUserCredentials(email, password);
                        Log.d(TAG, "User logged in successfully - Email: " + email.substring(0, Math.min(3, email.length())) + "***");
                        onLoginSuccess(email);
                        Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();


                    } else {
                        Toast.makeText(LoginActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                    }
                } else if (response.code() == 422) {
                    // Handle 422 response
                    try {
                        String errorBody = response.errorBody().string();
                        JsonObject jsonObject = JsonParser.parseString(errorBody).getAsJsonObject();
                        String errorMessage = jsonObject.get("message").getAsString();
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(LoginActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToken(String token, String domainUrl, String phoneNumber) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.putString(DOMAIN_KEY, domainUrl);
        editor.putString(PHONE_KEY, phoneNumber);
        editor.apply();
    }

    private void saveUserCredentials(String email, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(EMAIL_KEY, email);
        editor.putString(PASSWORD_KEY, password);
        editor.apply();
        Log.d(TAG, "User credentials saved for API use");
    }

    private void onLoginSuccess(String email) {
        Purchases.getSharedInstance().logIn(email, new LogInCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo, boolean created) {
                Log.d(TAG, "RevenueCat identified user: " + email);
                Log.d(TAG, "User created in RevenueCat: " + created);
                Log.d(TAG, "Active entitlements: " + customerInfo.getEntitlements().getAll().keySet().toString());

                com.revenuecat.purchases.EntitlementInfo entitlement =
                        customerInfo.getEntitlements().get(ENTITLEMENT_ID);

                boolean isSubscribed = entitlement != null && entitlement.isActive();

                if (isSubscribed) {
                    Log.d(TAG, "User has active subscription, going to main activity");
                    goToMainActivity();
                } else {
                    Log.d(TAG, "User does not have active subscription, showing paywall");
                    showPaywall();
                }
            }

            @Override
            public void onError(@NonNull com.revenuecat.purchases.PurchasesError error) {
                Log.e(TAG, "RevenueCat identify error: " + error.getMessage());
                Toast.makeText(LoginActivity.this,
                        "Error checking subscription status",
                        Toast.LENGTH_SHORT).show();

                // Fallback: show paywall on error
                showPaywall();
            }
        });
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, ActivityHome.class);
        startActivity(intent);
        finish();
    }

    private void showPaywall() {
        Intent intent = new Intent(LoginActivity.this, ActivityPaywall.class);
        intent.putExtra("ENTRY_SOURCE", "LOGIN");
        startActivity(intent);
        finish();
    }

    @SuppressWarnings("MissingSuperCall")
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
