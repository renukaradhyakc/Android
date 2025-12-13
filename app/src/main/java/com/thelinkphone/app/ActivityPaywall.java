package com.thelinkphone.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Offerings;
import com.revenuecat.purchases.Package;
import com.revenuecat.purchases.PurchaseParams;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.interfaces.LogInCallback;
import com.revenuecat.purchases.interfaces.PurchaseCallback;
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback;
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback;
import com.revenuecat.purchases.models.StoreProduct;
import com.revenuecat.purchases.models.StoreTransaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityPaywall extends AppCompatActivity {

    private MaterialButton btnContinue;

    private Map<String, Package> planPackageMap = new HashMap<>();
    private Package selectedPackage = null;

    private TextView tvAnnualName, tvAnnualPrice, discountPercentage, pricePerMonth;
    private TextView tvHalfYearlyName, tvHalfYearlyPrice;
    private TextView tvQuarterlyName, tvQuarterlyPrice;
    private TextView tvMonthlyName, tvMonthlyPrice;
    private TextView tvPrivacy, tvTerms;

    private Map<String, RadioButton> radioButtonMap = new HashMap<>();
    private Map<String, MaterialCardView> planCardMap = new HashMap<>();
    private RadioButton rbAnnual, rbHalfYearly, rbQuarterly, rbMonthly;

    private static final String TAG = "ActivityPaywall";
    private static final String ENTITLEMENT_ID = "CallALink Premium";
    private static final String SHARED_PREFS_NAME = "app_prefs";
    private static final String EMAIL_KEY = "user_email";

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paywall);

        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);

        initializeRevenueCatUser();

        btnContinue = findViewById(R.id.btnContinue);
        btnContinue.setEnabled(false); // Disable until packages loaded

        // Plan TextViews
        tvAnnualName = findViewById(R.id.cardAnnual).findViewById(R.id.tvTitleAnnual);
        tvAnnualPrice = findViewById(R.id.cardAnnual).findViewById(R.id.tvPriceAnnual);
        discountPercentage = findViewById(R.id.cardAnnual).findViewById(R.id.discount);
        pricePerMonth = findViewById(R.id.cardAnnual).findViewById(R.id.price_per_month);

        tvHalfYearlyName = findViewById(R.id.cardHalfYearly).findViewById(R.id.tvTitleHalfYearly);
        tvHalfYearlyPrice = findViewById(R.id.cardHalfYearly).findViewById(R.id.tvPriceHalfYearly);

        tvQuarterlyName = findViewById(R.id.cardQuarterly).findViewById(R.id.tvTitleQuarterly);
        tvQuarterlyPrice = findViewById(R.id.cardQuarterly).findViewById(R.id.tvPriceQuarterly);

        tvMonthlyName = findViewById(R.id.cardMonthly).findViewById(R.id.tvTitleMonthly);
        tvMonthlyPrice = findViewById(R.id.cardMonthly).findViewById(R.id.tvPriceMonthly);

        rbAnnual = findViewById(R.id.rbAnnual);
        rbHalfYearly = findViewById(R.id.rbHalfYearly);
        rbQuarterly = findViewById(R.id.rbQuarterly);
        rbMonthly = findViewById(R.id.rbMonthly);

        radioButtonMap.put("annual", rbAnnual);
        radioButtonMap.put("half_yearly", rbHalfYearly);
        radioButtonMap.put("quarterly", rbQuarterly);
        radioButtonMap.put("monthly", rbMonthly);

        planCardMap.put("annual", findViewById(R.id.cardAnnual));
        planCardMap.put("half_yearly", findViewById(R.id.cardHalfYearly));
        planCardMap.put("quarterly", findViewById(R.id.cardQuarterly));
        planCardMap.put("monthly", findViewById(R.id.cardMonthly));

        rbAnnual.setOnClickListener(v -> updateRadioButtons("annual"));
        rbHalfYearly.setOnClickListener(v -> updateRadioButtons("half_yearly"));
        rbQuarterly.setOnClickListener(v -> updateRadioButtons("quarterly"));
        rbMonthly.setOnClickListener(v -> updateRadioButtons("monthly"));

        planCardMap.get("annual").setOnClickListener(v -> updateRadioButtons("annual"));
        planCardMap.get("half_yearly").setOnClickListener(v -> updateRadioButtons("half_yearly"));
        planCardMap.get("quarterly").setOnClickListener(v -> updateRadioButtons("quarterly"));
        planCardMap.get("monthly").setOnClickListener(v -> updateRadioButtons("monthly"));

        loadOfferings();

        btnContinue.setOnClickListener(v -> purchaseSelectedPackage());
        findViewById(R.id.tvRestore).setOnClickListener(v -> restorePurchases());

        tvPrivacy = findViewById(R.id.tvPrivacy);
        tvTerms = findViewById(R.id.tvTerms);

        makeTextViewClickable(tvPrivacy, "Privacy", "https://www.termsfeed.com/live/dc6768be-8856-4124-a689-3aac885707ae");
        makeTextViewClickable(tvTerms, "Terms", "https://www.callalink.com/terms");
    }

    private void initializeRevenueCatUser() {
        String email = sharedPreferences.getString(EMAIL_KEY, null);

        if (email != null && !email.isEmpty()) {
            // Check if user is already logged in to RevenueCat
            String currentUserId = Purchases.getSharedInstance().getAppUserID();

            if (!email.equals(currentUserId)) {
                // Log in the user with their email
                Purchases.getSharedInstance().logIn(email, new LogInCallback() {
                    @Override
                    public void onReceived(@NonNull CustomerInfo customerInfo, boolean created) {
                        Log.d(TAG, "RevenueCat user initialized: " + email);
                    }

                    @Override
                    public void onError(@NonNull com.revenuecat.purchases.PurchasesError error) {
                        Log.e(TAG, "RevenueCat initialization error: " + error.getMessage());
                    }
                });
            }
        }
    }

    private void checkExistingSubscription() {
        Purchases.getSharedInstance().getCustomerInfo(new ReceiveCustomerInfoCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {
                com.revenuecat.purchases.EntitlementInfo entitlement =
                        customerInfo.getEntitlements().get(ENTITLEMENT_ID);

                if (entitlement != null && entitlement.isActive()) {
                    Log.d(TAG, "User already has active subscription, redirecting to home");
                    goToMainActivity();
                }
            }

            @Override
            public void onError(@NonNull com.revenuecat.purchases.PurchasesError error) {
                Log.e(TAG, "Error checking subscription: " + error.getMessage());
            }
        });
    }

    private void loadOfferings() {
        btnContinue.setEnabled(false); // Disable until packages are loaded

        Purchases.getSharedInstance().getOfferings(new ReceiveOfferingsCallback() {
            @Override
            public void onReceived(@NonNull Offerings offerings) {
                if (offerings.getCurrent() != null) {
                    List<Package> packages = offerings.getCurrent().getAvailablePackages();

                    Log.d("TAG", "Number of packages fetched: " + packages.size());

                    for (Package pkg : packages) {
                        StoreProduct product = pkg.getProduct();
                        String identifier = pkg.getIdentifier();

                        // Log everything for debugging
                        Log.d("TAG", "---- Package Info ----");
                        Log.d("TAG", "Package Identifier: " + identifier);
                        Log.d("TAG", "Package Type: " + pkg.getPackageType());
                        Log.d("TAG", "Product Name: " + product.getName());
                        Log.d("TAG", "Product Title: " + product.getTitle());
                        Log.d("TAG", "Product Description: " + product.getDescription());
                        Log.d("TAG", "Price (micros): " + product.getPrice().getAmountMicros());
                        Log.d("TAG", "Price (formatted): " + product.getPrice().getFormatted());
                        Log.d("TAG", "Currency Code: " + product.getPrice().getCurrencyCode());
                        Log.d("TAG", "--------------------");

                        String key = null;

                        // Map RevenueCat identifiers to our plan keys dynamically
                        if (identifier.equals("$rc_monthly")) {
                            key = "monthly";
                            tvMonthlyName.setText("Monthly Premium");
                            tvMonthlyPrice.setText(product.getPrice().getFormatted());
                        } else if (identifier.equals("$rc_three_month")) {
                            key = "quarterly";
                            tvQuarterlyName.setText("Quarterly Premium");
                            tvQuarterlyPrice.setText(product.getPrice().getFormatted());
                        } else if (identifier.equals("$rc_six_month")){
                            key = "half_yearly";
                            tvHalfYearlyName.setText("Half-Yearly Premium");
                            tvHalfYearlyPrice.setText(product.getPrice().getFormatted());
                        } else if (identifier.equals("$rc_annual")) {
                            key = "annual";
                            tvAnnualName.setText("Annual Premium");
                            tvAnnualPrice.setText(product.getPrice().getFormatted());
                            setPlanMonthlyPrice(product, discountPercentage, pricePerMonth, 12);
                        }

                        if (key != null) {
                            planPackageMap.put(key, pkg);
                            Log.d("TAG", "Mapped package to key: " + key);
                        }
                    }

                    // Set default selection AFTER packages loaded
                    if (!planPackageMap.isEmpty()) {
                        updateRadioButtons("annual"); // default
                        btnContinue.setEnabled(true);  // enable subscribe button
                    } else {
                        Log.e("TAG", "No packages loaded from RevenueCat!");
                    }
                } else {
                    Log.e("TAG", "No current offerings available");
                }
            }

            @Override
            public void onError(@NonNull com.revenuecat.purchases.PurchasesError error) {
                Log.e("TAG", "Error fetching offerings: " + error.getMessage());
                Toast.makeText(ActivityPaywall.this, "Failed to load packages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void purchaseSelectedPackage() {
        if (selectedPackage == null) {
            Log.e("TAG", "selectedPackage is NULL");
            Toast.makeText(this, "Please select a plan", Toast.LENGTH_SHORT).show();
            return;
        }

        PurchaseParams params = new PurchaseParams.Builder(this, selectedPackage).build();
        Purchases.getSharedInstance().purchase(params, new PurchaseCallback() {
            @Override
            public void onCompleted(@NonNull StoreTransaction transaction,
                                    @NonNull CustomerInfo customerInfo) {

                com.revenuecat.purchases.EntitlementInfo entitlement = customerInfo.getEntitlements().get(ENTITLEMENT_ID);
                Log.d(TAG, "Purchase completed successfully");
                Log.d(TAG, "Active entitlements: " + customerInfo.getEntitlements().getAll().keySet().toString());

                if (entitlement != null && entitlement.isActive()) {
                    unlockPremiumContent();
                    finish();
                } else {
                    Log.e(TAG, "Entitlement not active after purchase");
                    Toast.makeText(ActivityPaywall.this,
                            "Purchase completed but entitlement not active",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull com.revenuecat.purchases.PurchasesError error, boolean userCancelled) {
                Toast.makeText(ActivityPaywall.this,
                        userCancelled ? "Purchase cancelled" : "Purchase failed",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void restorePurchases() {
        Log.d(TAG, "Restore purchases initiated");

        Toast.makeText(this, "Restoring purchases...", Toast.LENGTH_SHORT).show();

        Purchases.getSharedInstance().restorePurchases(new ReceiveCustomerInfoCallback() {
            @Override
            public void onReceived(@NonNull CustomerInfo customerInfo) {
                Log.d(TAG, "Restore purchases completed");
                Log.d(TAG, "Active subscriptions: " + customerInfo.getActiveSubscriptions());

                com.revenuecat.purchases.EntitlementInfo entitlement =
                        customerInfo.getEntitlements().get(ENTITLEMENT_ID);

                if (entitlement != null && entitlement.isActive()) {
                    Log.d(TAG, "Active subscription found during restore");
                    unlockPremiumContent();

                    Toast.makeText(ActivityPaywall.this,
                            "Subscription restored successfully!",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "No active subscription found during restore");
                    Toast.makeText(ActivityPaywall.this,
                            "No active subscriptions found to restore",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(@NonNull com.revenuecat.purchases.PurchasesError error) {
                Log.e(TAG, "Restore error: " + error.getMessage());
                Log.e(TAG, "Error code: " + error.getCode());

                Toast.makeText(ActivityPaywall.this,
                        "Failed to restore purchases. Please try again.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setPlanMonthlyPrice(StoreProduct product, TextView discount, TextView pricePerMonth, int months) {
        double totalPrice = product.getPrice().getAmountMicros() / 1_000_000.0;
        double monthlyPrice = totalPrice / months;

        if (pricePerMonth != null) {
            pricePerMonth.setText(String.format("$%.2f/mo", monthlyPrice));
        }

        if (discount != null) {
            double regularPrice = months; // Example: assume $1 per month normal price
            double discountPercent = ((regularPrice - totalPrice) / regularPrice) * 100;
            discount.setText(String.format("%.0f%%", discountPercent));
        }
    }

    private void updateRadioButtons(String selectedKey) {
        for (String key : radioButtonMap.keySet()) {
            RadioButton rb = radioButtonMap.get(key);
            MaterialCardView card = planCardMap.get(key);
            if (rb != null && card != null) {
                boolean isSelected = key.equals(selectedKey);
                rb.setChecked(isSelected);
                rb.setButtonTintList(ContextCompat.getColorStateList(this,
                        isSelected ? R.color.primary_purple : R.color.radiobutton_unselected));

                card.setStrokeWidth(isSelected ? 2 : 0);
                card.setStrokeColor(ContextCompat.getColor(this,
                        isSelected ? R.color.primary_purple : R.color.selected_plan_background));
            }
        }

        selectedPackage = planPackageMap.get(selectedKey);
    }

    private void unlockPremiumContent() {
        Log.d("TAG", "Premium content unlocked!");
        Toast.makeText(this, "Premium content unlocked!", Toast.LENGTH_SHORT).show();
        // TODO: Save entitlement state, navigate or unlock features
    }

    private void goToMainActivity() {
        Intent intent = new Intent(ActivityPaywall.this, ActivityHome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void makeTextViewClickable(TextView textView, String text, String url) {
        SpannableString spannable = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // Open the URL
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                widget.getContext().startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(ContextCompat.getColor(textView.getContext(), R.color.paywall_text_secondary));
                ds.setUnderlineText(true);
            }
        };

        spannable.setSpan(clickableSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }


}
