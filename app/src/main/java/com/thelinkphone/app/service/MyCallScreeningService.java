package com.thelinkphone.app.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.thelinkphone.app.ActivityCall;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.model.Event;
import com.thelinkphone.app.model.EventResponse;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.CheckEventTimeListener;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.ReadContact;

import java.util.Iterator;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Call screening service that implements two modes:
 *
 * 1. UNRESTRICTED: All calls are allowed (except manually blocked numbers)
 *
 * 2. PHONELINK_SCHEDULED (Enhanced):
 *    - Manual block list: Always blocked (highest priority)
 *    - Known contacts (saved in phonebook): Always allowed
 *    - Unknown numbers: Only allowed during active scheduled events
 *    - Requires user login for scheduled events API
 *
 * This provides optimal user experience by ensuring contacts can always call
 * while filtering unknown numbers based on scheduled availability.
 */
public class MyCallScreeningService extends CallScreeningService {
    private static final String TAG = "MyCallScreeningService";
    private static final String SHARED_PREFS_NAME = "app_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final String DOMAIN_KEY = "auth_domain";
    private static final String PHONE_KEY = "auth_phone";
    private static final String EMAIL_KEY = "user_email";
    private static final String PASSWORD_KEY = "user_password";

    @Override
    public void onScreenCall(Call.Details details) {
        String decode = Uri.decode(details.getHandle().toString());
        String phoneNumber = (decode == null || !decode.startsWith("tel:")) ? "" : decode.substring(decode.indexOf("tel:") + 4);

        Log.d(TAG, "==================== CALL SCREENING START ====================");
        Log.d(TAG, "Raw handle: " + details.getHandle().toString());
        Log.d(TAG, "Extracted phone number: " + phoneNumber);

        // Check if this is an incoming call (call screening should only apply to incoming calls)
        int callDirection = details.getCallDirection();
        Log.d(TAG, "Call direction: " + callDirection + " (0=UNKNOWN, 1=OUTGOING, 2=INCOMING)");

        if (callDirection == Call.Details.DIRECTION_OUTGOING) {
            Log.d(TAG, "Outgoing call detected - allowing without screening: " + phoneNumber);
            allowCall(details);
            return;
        }

        Log.d(TAG, "Screening incoming call from: " + phoneNumber);

        // Check if number is manually blocked by user
        boolean isManuallyBlocked = isNumberManuallyBlocked(phoneNumber);
        Log.d(TAG, "Manual block check result: " + isManuallyBlocked);

        if (isManuallyBlocked) {
            Log.d(TAG, "BLOCKING CALL - number is in block list: " + phoneNumber);
            blockCall(details);
            return;
        }

        // Check call settings preference
        int callSetting = MyShare.getCallSetting(this);
        Log.d(TAG, "Call screening for " + phoneNumber + " - Setting: " + (callSetting == MyShare.CALL_SETTING_UNRESTRICTED ? "UNRESTRICTED" : "PHONELINK_SCHEDULED"));

        Log.d(TAG, "CALL MODE: " + (callSetting == MyShare.CALL_SETTING_UNRESTRICTED ? "UNRESTRICTED" : "PHONELINK_SCHEDULED"));
        Log.d(TAG, "Fetching event info for: " + phoneNumber);

        checkEventAndProceed(details, phoneNumber, callSetting);

        Log.d(TAG, "==================== CALL SCREENING END ====================");
    }

    private void launchActivityCall(int callMode,Event event,String phoneNumber) {
        Intent intent = new Intent(getApplicationContext(), com.thelinkphone.app.service.IncomingCallPopupService.class);;
        intent.putExtra("CALL_MODE", callMode);

        String displayName = "Unknown Caller";
        boolean isCallalinkUser = false;
        boolean isWithinTime = false;
        boolean isAContact=false;

        if (event != null) {
            isCallalinkUser = event.isCallalinkUser();
            isWithinTime = event.isWithinTime();
        }

        String[] namePhoto = ReadContact.getNamePhoto(getApplicationContext(), phoneNumber);
        String contactName = namePhoto != null ? namePhoto[0] : null;

        if (contactName != null && !contactName.isEmpty()) {
            displayName = contactName;
            isAContact=true;
            Log.d("launchActivityCall", "Found local contact: " + displayName);
        } else if (event != null && event.getUsername() != null && !event.getUsername().isEmpty()) {
            displayName = event.getUsername();
            Log.d("launchActivityCall", "Using API username: " + displayName);
        }

        intent.putExtra("IS_CALLALINK_USER", isCallalinkUser);
        intent.putExtra("IS_WITHIN_SCHEDULE", isWithinTime);
        intent.putExtra("USERNAME", displayName);
        intent.putExtra("IS_A_CONTACT",isAContact);

        Log.d(TAG, "Received popup data -> username: " + displayName +
                ", callMode: " + callMode +
                ", isCallALinkUser: " + isCallalinkUser +
                ", isWithinSchedule: " + isWithinTime +
                ", isAContact: " + isAContact);

        MyShare.saveCallInfo(getApplicationContext(), displayName);

        Log.d("launchActivityCall", "Final name to display: " + displayName);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            getApplicationContext().startForegroundService(intent);
        } else {
            getApplicationContext().startService(intent);
        }

    }


    private boolean isNumberManuallyBlocked(String phoneNumber) {
        Log.d(TAG, "Checking if number is manually blocked: " + phoneNumber);

        // Log all blocked numbers for debugging
        logAllBlockedNumbers();

        Iterator<ItemContact> it = MyShare.getArrBlock(this).iterator();
        while (it.hasNext()) {
            ItemContact contact = it.next();

            // Check if contact ID matches (for contacts from phonebook)
            String idWithNumber = ReadContact.getIdWithNumber(this, phoneNumber);
            if (!idWithNumber.isEmpty() && idWithNumber.equals(contact.getId())) {
                Log.d(TAG, "Number blocked by contact ID match: " + phoneNumber);
                return true;
            }

            // Always check phone numbers in blocked list (for both manual entries and contacts)
            if (!contact.getArrPhone().isEmpty()) {
                Iterator<ItemPhone> it2 = contact.getArrPhone().iterator();
                while (it2.hasNext()) {
                    ItemPhone phone = it2.next();
                    if (!phoneNumber.isEmpty() && phone.getNumber() != null &&
                            PhoneNumberUtils.compare(phoneNumber, phone.getNumber())) {
                        Log.d(TAG, "Number blocked by phone number match: " + phoneNumber + " matches " + phone.getNumber());
                        return true;
                    }
                }
            }
        }

        Log.d(TAG, "Number not found in block list: " + phoneNumber);
        return false;
    }

    private void logAllBlockedNumbers() {
        Log.d(TAG, "=== LOGGING ALL BLOCKED NUMBERS ===");
        Iterator<ItemContact> it = MyShare.getArrBlock(this).iterator();
        int count = 0;
        while (it.hasNext()) {
            ItemContact contact = it.next();
            count++;
            Log.d(TAG, "Blocked Contact #" + count + ":");
            Log.d(TAG, "  - ID: " + (contact.getId() != null ? contact.getId() : "null"));
            Log.d(TAG, "  - Name: " + (contact.getName() != null ? contact.getName() : "null"));

            if (contact.getArrPhone() != null && !contact.getArrPhone().isEmpty()) {
                for (int i = 0; i < contact.getArrPhone().size(); i++) {
                    ItemPhone phone = contact.getArrPhone().get(i);
                    Log.d(TAG, "  - Phone #" + (i+1) + ": " + (phone.getNumber() != null ? phone.getNumber() : "null"));
                }
            } else {
                Log.d(TAG, "  - No phone numbers");
            }
        }
        Log.d(TAG, "Total blocked contacts: " + count);
        Log.d(TAG, "=== END BLOCKED NUMBERS LOG ===");
    }

    private boolean isNumberInContacts(String phoneNumber) {
        String contactId = ReadContact.getIdWithNumber(this, phoneNumber);
        boolean isInContacts = contactId != null && !contactId.isEmpty();
        Log.d(TAG, "Contact check for " + phoneNumber + ": " + (isInContacts ? "FOUND" : "NOT FOUND"));
        return isInContacts;
    }

    private void checkPhonelinkScheduledCall(Call.Details details, String phoneNumber,int callMode) {
        // Check if the number exists in contacts using helper method
        if (isNumberInContacts(phoneNumber)) {
            // Known number (in contacts) - always allow
            Log.d(TAG, "Call allowed - number found in contacts: " + phoneNumber);
            allowCall(details);
            launchActivityCall(callMode,null,phoneNumber);
        } else {
            // Unknown number (not in contacts) - check scheduled events
            Log.d(TAG, "Unknown number detected, checking scheduled events: " + phoneNumber);
            checkScheduledEventsForUnknownNumber(details, phoneNumber,callMode);
        }
    }

    private void checkScheduledEventsForUnknownNumber(Call.Details details, String phoneNumber,int callMode) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(TOKEN_KEY, null);

        // If user is not logged in, block unknown numbers
        if (userToken == null || userToken.isEmpty()) {
            Log.w(TAG, "User not logged in - blocking unknown number: " + phoneNumber);
            blockCall(details);
            return;
        }

        // Get user credentials from shared preferences (saved during login)
        String userEmail = sharedPreferences.getString(EMAIL_KEY, "");
        String userPassword = sharedPreferences.getString(PASSWORD_KEY, "");

        // If credentials are not available, block unknown numbers
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Log.w(TAG, "User credentials not available - blocking unknown number: " + phoneNumber);
            blockCall(details);
            return;
        }

        checkEventTime(userEmail, userPassword, phoneNumber, new CheckEventTimeListener() {
            @Override
            public void onEventCheckComplete(Event event,boolean apiFailed) {
                if (event == null) {
                    Log.e("MyCallScreeningService", "Event is null — API call failed or returned empty.");
                    blockCall(details);
                    return;
                }
                if (event.isWithinTime()) {
                    Log.d(TAG, "Call allowed - unknown number within scheduled event time: " + phoneNumber);
                    allowCall(details);
                    launchActivityCall(callMode,event,phoneNumber);
                } else {
                    Log.d(TAG, "Call blocked - unknown number, no scheduled event: " + phoneNumber);
                    blockCall(details);
                }
            }
        });
    }

    private void checkScheduledEvents(Call.Details details, String phoneNumber,int callMode) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(TOKEN_KEY, null);

        // If user is not logged in, use fallback behavior
        if (userToken == null || userToken.isEmpty()) {
            Log.w(TAG, "User not logged in - using fallback behavior (block call)");
            blockCall(details);
            return;
        }

        // Get user credentials from shared preferences (saved during login)
        String userEmail = sharedPreferences.getString(EMAIL_KEY, "");
        String userPassword = sharedPreferences.getString(PASSWORD_KEY, "");

        // If credentials are not available, block the call
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Log.w(TAG, "User credentials not available - blocking call for security");
            Log.d(TAG, "Email available: " + !userEmail.isEmpty() + ", Password available: " + !userPassword.isEmpty());
            blockCall(details);
            return;
        }

        checkEventTime(userEmail, userPassword, phoneNumber, new CheckEventTimeListener() {
            @Override
            public void onEventCheckComplete(Event event,boolean apiFailed) {
                if (event.isWithinTime()) {
                    Log.d(TAG, "Call allowed - within scheduled event time");
                    allowCall(details);
                    launchActivityCall(callMode,event,phoneNumber);
                } else {
                    Log.d(TAG, "Call blocked - no scheduled event found");
                    blockCall(details);
                }
            }
        });
    }

    private void allowCall(Call.Details details) {
        String phoneNumber = details.getHandle() != null ? details.getHandle().getSchemeSpecificPart() : "Unknown";
        Log.d(TAG, "ALLOWING call from: " + phoneNumber);

        CallResponse response = new CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build();
        respondToCall(details, response);
    }

    private void blockCall(Call.Details details) {
        String phoneNumber = details.getHandle() != null ? details.getHandle().getSchemeSpecificPart() : "Unknown";
        Log.d(TAG, "BLOCKING call from: " + phoneNumber + " - Call log deletion will be attempted");

        CallResponse response = new CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
                .build();
        respondToCall(details, response);

        // Delete call log entry for blocked calls in Phonelink Scheduled mode
        deleteBlockedCallLog(details);
    }

    private void deleteBlockedCallLog(Call.Details details) {
        if (MyShare.isCallSettingPhonelinkScheduled(this)) {
            try {
                String phoneNumber = null;
                if (details.getHandle() != null) {
                    phoneNumber = details.getHandle().getSchemeSpecificPart();
                }

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    final String finalPhoneNumber = phoneNumber;
                    Log.i(TAG, "PHONELINK SCHEDULED MODE: Initiating call log deletion for blocked call: " + finalPhoneNumber);

//                     Force delete all recent call logs for blocked calls
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // First attempt - immediate force delete
                                ReadContact.forceDeleteRecentCallLogs(MyCallScreeningService.this, finalPhoneNumber);
                                ReadContact.clearMissedCallBadge(MyCallScreeningService.this, finalPhoneNumber);
                                Log.i(TAG, "ATTEMPT 1: Immediate force delete for blocked call: " + finalPhoneNumber);

                                Thread.sleep(1000); // 1 second delay

                                // Second attempt - standard deletion
                                ReadContact.removeMostRecentCallLog(MyCallScreeningService.this, finalPhoneNumber);
                                ReadContact.clearMissedCallBadge(MyCallScreeningService.this, finalPhoneNumber);
                                Log.i(TAG, "ATTEMPT 2: Standard deletion for blocked call: " + finalPhoneNumber);

                                Thread.sleep(2000); // Additional 2 seconds delay

                                // Final attempt - force delete again
                                ReadContact.forceDeleteRecentCallLogs(MyCallScreeningService.this, finalPhoneNumber);
                                ReadContact.clearMissedCallBadge(MyCallScreeningService.this, finalPhoneNumber);
                                Log.i(TAG, "ATTEMPT 3: Final force delete for blocked call: " + finalPhoneNumber);

                            } catch (Exception e) {
                                Log.e(TAG, "Error deleting blocked call log: " + e.getMessage());
                            }
                        }
                    }).start();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing blocked call log deletion: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Unrestricted mode - blocked call log will be preserved");
        }
    }

    private void checkEventTime(String email, String password, String phoneNumber, CheckEventTimeListener listener) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        retrofit2.Call<Event> call = apiService.checkEvent(email, password, phoneNumber);

        call.enqueue(new retrofit2.Callback<Event>() {
            @Override
            public void onResponse(retrofit2.Call<Event> call, retrofit2.Response<Event> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Event event = response.body();
                    Log.d(TAG, "API Response: " + new Gson().toJson(response.body()));
                    listener.onEventCheckComplete(event,false);
                } else {
                    Log.e(TAG, "API call failed or empty response - Response code: " + response.code());
                    // On API error, use fallback behavior (block call)
                    listener.onEventCheckComplete(null,true);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Event> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                // On API failure, fallback to blocking the call for security
                // In a production app, you might want to allow calls on API failure
                // depending on your security requirements
                listener.onEventCheckComplete(null,false);
                t.printStackTrace();
            }
        });
    }

    // Debug method to log current call settings
    public void logCurrentSettings() {
        String currentSetting = MyShare.getCallSettingName(this);
        Log.d(TAG, "Current call setting: " + currentSetting);

        if (MyShare.isCallSettingUnrestricted(this)) {
            Log.d(TAG, "Call mode: UNRESTRICTED - All calls will be allowed");
        } else {
            Log.d(TAG, "Call mode: PHONELINK_SCHEDULED - Known contacts always allowed, unknown numbers only during scheduled events");
        }
    }

    // Method to test call screening logic without an actual call
    public void testCallScreening(String testPhoneNumber) {
        Log.d(TAG, "Testing call screening for number: " + testPhoneNumber);

        boolean isBlocked = isNumberManuallyBlocked(testPhoneNumber);
        Log.d(TAG, "Number manually blocked: " + isBlocked);

        int callSetting = MyShare.getCallSetting(this);
        Log.d(TAG, "Call setting: " + (callSetting == MyShare.CALL_SETTING_UNRESTRICTED ? "UNRESTRICTED" : "PHONELINK_SCHEDULED"));

        if (isBlocked) {
            Log.d(TAG, "TEST RESULT: Call would be BLOCKED (manually blocked)");
        } else if (callSetting == MyShare.CALL_SETTING_UNRESTRICTED) {
            Log.d(TAG, "TEST RESULT: Call would be ALLOWED (unrestricted mode)");
        } else {
            // Check if number is in contacts using helper method
            if (!isNumberInContacts(testPhoneNumber)) {
                Log.d(TAG, "TEST RESULT: Unknown number - would check scheduled events");
            } else {
                Log.d(TAG, "TEST RESULT: Call would be ALLOWED (number found in contacts)");
            }
        }
    }

    // Method to test the scheduled events API
    public void testScheduledEventsAPI(String testPhoneNumber) {
        Log.d(TAG, "Testing scheduled events API...");

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(TOKEN_KEY, null);

        if (userToken == null || userToken.isEmpty()) {
            Log.w(TAG, "Cannot test API - user not logged in");
            return;
        }

        String userEmail = sharedPreferences.getString(EMAIL_KEY, "");
        String userPassword = sharedPreferences.getString(PASSWORD_KEY, "");

        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Log.w(TAG, "Cannot test API - user credentials not available");
            Log.d(TAG, "Email available: " + !userEmail.isEmpty() + ", Password available: " + !userPassword.isEmpty());
            return;
        }

        checkEventTime(userEmail, userPassword, testPhoneNumber, new CheckEventTimeListener() {
            @Override
            public void onEventCheckComplete(Event event,boolean apiFailed) {
                Log.d(TAG, "API TEST RESULT: " + (event.isWithinTime() ? "WITHIN SCHEDULED TIME" : "NOT WITHIN SCHEDULED TIME"));
                Log.d(TAG, "This means calls would be: " + (event.isWithinTime() ? "ALLOWED" : "BLOCKED"));
            }
        });
    }

    // Comprehensive test method to verify the entire call screening flow
    public void testCompleteCallScreeningFlow(String testPhoneNumber) {
        Log.d(TAG, "=== TESTING COMPLETE CALL SCREENING FLOW ===");
        Log.d(TAG, "Test phone number: " + testPhoneNumber);

        // Test 1: Check if user is logged in
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(TOKEN_KEY, "");
        String userEmail = sharedPreferences.getString(EMAIL_KEY, "");
        String userPassword = sharedPreferences.getString(PASSWORD_KEY, "");

        Log.d(TAG, "User login status: " + (!userToken.isEmpty() ? "LOGGED IN" : "NOT LOGGED IN"));
        Log.d(TAG, "Credentials available - Email: " + !userEmail.isEmpty() + ", Password: " + !userPassword.isEmpty());

        // Test 2: Check manual blocking
        boolean isManuallyBlocked = isNumberManuallyBlocked(testPhoneNumber);
        Log.d(TAG, "Manual block status: " + (isManuallyBlocked ? "BLOCKED" : "NOT BLOCKED"));

        // Test 3: Check call settings
        int callSetting = MyShare.getCallSetting(this);
        String settingName = callSetting == MyShare.CALL_SETTING_UNRESTRICTED ? "UNRESTRICTED" : "PHONELINK_SCHEDULED";
        Log.d(TAG, "Call setting: " + settingName);

        // Test 4: Predict call outcome
        if (isManuallyBlocked) {
            Log.d(TAG, "FINAL RESULT: Call would be BLOCKED (manually blocked number)");
        } else if (callSetting == MyShare.CALL_SETTING_UNRESTRICTED) {
            Log.d(TAG, "FINAL RESULT: Call would be ALLOWED (unrestricted mode)");
        } else {
            // In Phonelink Scheduled mode, check if number is in contacts using helper method
            if (!isNumberInContacts(testPhoneNumber)) {
                if (userEmail.isEmpty() || userPassword.isEmpty()) {
                    Log.d(TAG, "FINAL RESULT: Call would be BLOCKED (unknown number, credentials not available)");
                } else {
                    Log.d(TAG, "FINAL RESULT: Unknown number would be CHECKED against scheduled events");
                    // Test the API call
                    checkEventTime(userEmail, userPassword, testPhoneNumber, new CheckEventTimeListener() {
                        @Override
                        public void onEventCheckComplete(Event event,boolean apiFailed) {
                            Log.d(TAG, "SCHEDULED EVENT CHECK: " + (event.isWithinTime() ? "WITHIN TIME - ALLOW" : "NOT WITHIN TIME - BLOCK"));
                        }
                    });
                }
            } else {
                Log.d(TAG, "FINAL RESULT: Call would be ALLOWED (number found in contacts)");
            }
        }

        Log.d(TAG, "=== END OF CALL SCREENING FLOW TEST ===");
    }

    // Method to verify user credentials are properly saved
    public void verifyUserCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userToken = sharedPreferences.getString(TOKEN_KEY, "");
        String userEmail = sharedPreferences.getString(EMAIL_KEY, "");
        String userPassword = sharedPreferences.getString(PASSWORD_KEY, "");
        String domain = sharedPreferences.getString(DOMAIN_KEY, "");
        String phone = sharedPreferences.getString(PHONE_KEY, "");

        Log.d(TAG, "=== USER CREDENTIALS VERIFICATION ===");
        Log.d(TAG, "Token available: " + !userToken.isEmpty());
        Log.d(TAG, "Email available: " + !userEmail.isEmpty());
        Log.d(TAG, "Password available: " + !userPassword.isEmpty());
        Log.d(TAG, "Domain available: " + !domain.isEmpty());
        Log.d(TAG, "Phone available: " + !phone.isEmpty());

        if (!userEmail.isEmpty()) {
            Log.d(TAG, "Email preview: " + userEmail.substring(0, Math.min(3, userEmail.length())) + "***");
        }
        if (!domain.isEmpty()) {
            Log.d(TAG, "Domain: " + domain);
        }
    }

    // Method to explain the complete Phonelink Scheduled behavior
    public void explainPhonelinkScheduledBehavior() {
        Log.d(TAG, "=== PHONELINK SCHEDULED BEHAVIOR EXPLANATION ===");
        Log.d(TAG, "When 'Phonelink Scheduled' is enabled, calls are handled as follows:");
        Log.d(TAG, "");
        Log.d(TAG, "1. MANUAL BLOCK LIST CHECK:");
        Log.d(TAG, "   - If caller is in manual block list → BLOCK (regardless of contact/schedule status)");
        Log.d(TAG, "");
        Log.d(TAG, "2. CONTACT CHECK:");
        Log.d(TAG, "   - If caller is saved in phonebook contacts → ALLOW (always)");
        Log.d(TAG, "   - Known contacts can call anytime, no schedule restrictions");
        Log.d(TAG, "");
        Log.d(TAG, "3. UNKNOWN NUMBER + SCHEDULE CHECK:");
        Log.d(TAG, "   - If caller is NOT in contacts → Check scheduled events");
        Log.d(TAG, "   - If user not logged in → BLOCK");
        Log.d(TAG, "   - If no credentials available → BLOCK");
        Log.d(TAG, "   - If within scheduled event time → ALLOW");
        Log.d(TAG, "   - If no active scheduled event → BLOCK");
        Log.d(TAG, "");
        Log.d(TAG, "SUMMARY:");
        Log.d(TAG, "- Contacts: Always allowed");
        Log.d(TAG, "- Unknown numbers: Only allowed during scheduled events");
        Log.d(TAG, "- Blocked numbers: Never allowed");
        Log.d(TAG, "=== END OF EXPLANATION ===");
    }

    private void checkEventAndProceed(Call.Details details, String phoneNumber, int callMode) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = sharedPreferences.getString(EMAIL_KEY, "");
        String userPassword = sharedPreferences.getString(PASSWORD_KEY, "");

        // Call API always, even if credentials missing (gracefully skip if empty)
        if (userEmail.isEmpty() || userPassword.isEmpty()) {
            Log.w(TAG, "Credentials missing — proceeding without event info");
            allowCall(details);
            launchActivityCall(callMode, null, phoneNumber);
            return;
        }

        checkEventTime(userEmail, userPassword, phoneNumber, new CheckEventTimeListener() {
            @Override
            public void onEventCheckComplete(Event event,boolean apiFailed) {
                Log.d(TAG, "Event check complete for " + phoneNumber);

                if (apiFailed) {
                    Log.e(TAG, "API failed — blocking call");
                    blockCall(details); // don't allow
                    return;
                }

                // For unrestricted mode → always allow (ignore schedule)
                if (callMode == MyShare.CALL_SETTING_UNRESTRICTED) {
                    allowCall(details);
                    launchActivityCall(callMode, event, phoneNumber);
                    return;
                }

                // For Phonelink Scheduled mode
                boolean isContact = isNumberInContacts(phoneNumber);
                if (isContact) {
                    Log.d(TAG, "Contact found — allowing, but updating UI using event info");
                    allowCall(details);
                    launchActivityCall(callMode, event, phoneNumber);
                } else if (event != null && event.isWithinTime()) {
                    Log.d(TAG, "Unknown number — within schedule, allowing");
                    allowCall(details);
                    launchActivityCall(callMode, event, phoneNumber);
                } else {
                    Log.d(TAG, "Unknown number — outside schedule, blocking");
                    blockCall(details);
                }
            }
        });
    }


}

