package com.thelinkphone.app.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.model.Event;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.CallBlockReason;
import com.thelinkphone.app.utils.CheckEventTimeListener;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.ReadContact;

import java.util.Iterator;

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

    @Override
    public void onScreenCall(Call.Details details) {
        String decode = Uri.decode(details.getHandle().toString());
        String phoneNumber = (decode == null || !decode.startsWith("tel:")) ? "" : decode.substring(decode.indexOf("tel:") + 4);

        Log.d(TAG, "==================== CALL SCREENING START ====================");
        Log.d(TAG, "Raw handle: " + details.getHandle().toString());
        Log.d(TAG, "Extracted phone number: " + phoneNumber);

        // Check if this is an incoming call (call screening should only apply to incoming calls)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && details.getCallDirection() == Call.Details.DIRECTION_OUTGOING) {
            int callDirection = details.getCallDirection();
            Log.d(TAG, "Call direction: " + callDirection + " (0=UNKNOWN, 1=OUTGOING, 2=INCOMING)");

            if (callDirection == Call.Details.DIRECTION_OUTGOING) {
                Log.d(TAG, "Outgoing call detected - allowing without screening: " + phoneNumber);
                allowCall(details);
                return;
            }
        }

        Log.d(TAG, "Screening incoming call from: " + phoneNumber);

        // Check call settings preference
        int callSetting = MyShare.getCallSetting(this);
        Log.d(TAG, "Call screening for " + phoneNumber + " - Setting: " + (callSetting == MyShare.CALL_SETTING_UNRESTRICTED ? "UNRESTRICTED" : "PHONELINK_SCHEDULED"));

        Log.d(TAG, "CALL MODE: " + (callSetting == MyShare.CALL_SETTING_UNRESTRICTED ? "UNRESTRICTED" : "PHONELINK_SCHEDULED"));
        Log.d(TAG, "Fetching event info for: " + phoneNumber);

        final boolean isManuallyBlocked = isNumberManuallyBlocked(phoneNumber);
        Log.d(TAG, "Manual block check result: " + isManuallyBlocked);

        checkEventAndProceed(details, phoneNumber, callSetting, isManuallyBlocked);

        Log.d(TAG, "==================== CALL SCREENING END ====================");
    }

    private void launchActivityCall(int callMode,Event event,String phoneNumber) {
        Intent intent = new Intent(getApplicationContext(), com.thelinkphone.app.service.IncomingCallPopupService.class);
        intent.putExtra("CALL_MODE", callMode);

        String displayName = "Unknown Caller";
        boolean isCallalinkUser = false;
        boolean isWithinTime = false;
        boolean isAContact=false;

        if (event != null) {
            isCallalinkUser = event.isCallalinkUser();
            isWithinTime = event.isWithinTime();
        }

        displayName=getDisplayName(phoneNumber,event);

        if (isNumberInContacts(phoneNumber)) {
            isAContact=true;
            Log.d(TAG, "Found local contact: " + displayName);
        } else {
            Log.d(TAG, "Not found in local contact: " + displayName);
        }

        intent.putExtra("IS_CALLALINK_USER", isCallalinkUser);
        intent.putExtra("IS_WITHIN_SCHEDULE", isWithinTime);
        intent.putExtra("USERNAME", displayName);
        intent.putExtra("IS_A_CONTACT",isAContact);

        Log.d(TAG, "Launching incoming call popup ->username: " + displayName +
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

    private void launchBlockedPopup(int callMode,Event event,String phoneNumber,boolean isManuallyBlocked) {

        String displayName = "Unknown Caller";
        displayName=getDisplayName(phoneNumber,event);

        Log.d(TAG, "Launching blocked popup -> username: " + displayName +
                ", callMode: " + callMode +
                ", isManuallyBlocked: " + isManuallyBlocked);

        MyShare.saveCallInfo(getApplicationContext(), displayName);
        BlockedPopupService.showPopup(getApplicationContext(), callMode, displayName, isManuallyBlocked,phoneNumber);
    }

    private boolean isNumberManuallyBlocked(String phoneNumber) {
        Log.d(TAG, "Checking if number is manually blocked: " + phoneNumber);

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

    private boolean isNumberInContacts(String phoneNumber) {
        String contactId = ReadContact.getIdWithNumber(this, phoneNumber);
        boolean isInContacts = contactId != null && !contactId.isEmpty();
        Log.d(TAG, "Contact check for " + phoneNumber + ": " + (isInContacts ? "FOUND" : "NOT FOUND"));
        return isInContacts;
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
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build();
        respondToCall(details, response);

        // Delete call log entry for blocked calls in Phonelink Scheduled mode
//        deleteBlockedCallLog(details);
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

    private void checkEventTime(String token, String phoneNumber, CheckEventTimeListener listener) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        retrofit2.Call<Event> call = apiService.checkEvent("Bearer " + token, phoneNumber);

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
                listener.onEventCheckComplete(null,false);
                t.printStackTrace();
            }
        });
    }

    private void checkEventAndProceed(Call.Details details, String phoneNumber, int callMode,boolean isManuallyBlocked) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(TOKEN_KEY, "");

        // Call API always, even if credentials missing (gracefully skip if empty)
        if (token.isEmpty()) {
            Log.w(TAG, "Auth token missing");
            allowCall(details);
            launchActivityCall(callMode, null, phoneNumber);
            return;
        }

        checkEventTime(token, phoneNumber, new CheckEventTimeListener() {
            @Override
            public void onEventCheckComplete(Event event,boolean apiFailed) {
                Log.d(TAG, "Event check complete for " + phoneNumber);

                if (isManuallyBlocked) {
                    Log.d(TAG, "BLOCKING CALL - number is in manual block list: " + phoneNumber);
                    blockCall(details);
                    launchBlockedPopup(callMode, event, phoneNumber, true);
                    return;

                }

                if (apiFailed) {
                    Log.e(TAG, "API failed for non-blocked call");
                    if (callMode == MyShare.CALL_SETTING_UNRESTRICTED) {
                        Log.d(TAG, "Unrestricted mode - allowing call despite API failure");
                        allowCall(details);
                        launchActivityCall(callMode, null, phoneNumber);

                    } else {
                        boolean isContact = isNumberInContacts(phoneNumber);
                        if (isContact) {
                            Log.d(TAG, "Known contact - allowing despite API failure");
                            allowCall(details);
                            launchActivityCall(callMode, null, phoneNumber);

                        } else {
                            Log.d(TAG, "Unknown number + API failure - blocking for safety");
                            MyShare.addBlockReason(MyCallScreeningService.this, phoneNumber, CallBlockReason.API_FAILURE);
                            blockCall(details);
                            launchBlockedPopup(callMode, null, phoneNumber, false);
                        }
                    }
                    return;
                }

                // For unrestricted mode → always allow (ignore schedule)
                if (callMode == MyShare.CALL_SETTING_UNRESTRICTED) {
                    Log.d(TAG, "Unrestricted mode - allowing call");
                    allowCall(details);
                    launchActivityCall(callMode, event, phoneNumber);
                    return;
                }

                // For Phonelink Scheduled mode
//                boolean isContact = isNumberInContacts(phoneNumber);
//                if (isContact) {
//                    Log.d(TAG, "Contact found — allowing, but updating UI using event info");
//                    allowCall(details);
//                    launchActivityCall(callMode, event, phoneNumber);
//                } else
                if (event != null && event.isWithinTime()) {
                    Log.d(TAG, "Unknown number — within schedule, allowing");
                    allowCall(details);
                    launchActivityCall(callMode, event, phoneNumber);
                } else {
                    Log.d(TAG, "Unknown number — outside schedule, blocking");
                    MyShare.addBlockReason(MyCallScreeningService.this, phoneNumber, CallBlockReason.OUTSIDE_SCHEDULE);
                    blockCall(details);
                    launchBlockedPopup(callMode,event,phoneNumber,isManuallyBlocked);
                }
            }
        });
    }

    public void logCurrentSettings() {
        String currentSetting = MyShare.getCallSettingName(this);
        Log.d(TAG, "Current call setting: " + currentSetting);
        if (MyShare.isCallSettingUnrestricted(this)) {
            Log.d(TAG, "Call mode: UNRESTRICTED - All calls will be allowed");
        } else {
            Log.d(TAG, "Call mode: PHONELINK_SCHEDULED - Known contacts always allowed, unknown numbers only during scheduled events");
        }
    }

    private String getDisplayName(String phoneNumber, Event event) {
        String[] namePhoto = ReadContact.getNamePhoto(getApplicationContext(), phoneNumber);
        if (namePhoto != null && !namePhoto[0].isEmpty()) return namePhoto[0];
        if (event != null && event.getUsername() != null && !event.getUsername().isEmpty()) return event.getUsername();
        return "Unknown Caller";
    }
}

