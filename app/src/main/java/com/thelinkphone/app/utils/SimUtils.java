package com.thelinkphone.app.utils;

import android.content.Context;
import android.net.Uri;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import androidx.core.app.ActivityCompat;
import com.thelinkphone.app.item.ItemSimInfo;
import java.util.ArrayList;


public class SimUtils {
    public static boolean areMultipleSIMsAvailable(Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context, "android.permission.READ_PHONE_STATE") != 0) {
                return false;
            }
            return ((TelecomManager) context.getSystemService(Context.TELECOM_SERVICE)).getCallCapablePhoneAccounts().size() > 1;
        } catch (Exception unused) {
            return false;
        }
    }

    public static ArrayList<ItemSimInfo> getAvailableSIMCardLabels(Context context) {
        ArrayList<ItemSimInfo> arrayList = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, "android.permission.READ_PHONE_STATE") != 0) {
            return arrayList;
        }
        try {
            TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
            int i = 0;
            for (PhoneAccountHandle phoneAccountHandle : telecomManager.getCallCapablePhoneAccounts()) {
                PhoneAccount phoneAccount = telecomManager.getPhoneAccount(phoneAccountHandle);
                String charSequence = phoneAccount.getLabel().toString();
                String uri = phoneAccount.getAddress().toString();
                if (uri.startsWith("tel:") && !uri.substring(uri.indexOf("tel:")).isEmpty()) {
                    uri = Uri.decode(uri.substring(uri.indexOf("tel:")));
                }
                i++;
                arrayList.add(new ItemSimInfo(i, phoneAccount.getAccountHandle(), charSequence, uri.substring(uri.indexOf("tel:") + 4)));
            }
        } catch (Exception unused) {
        }
        return arrayList;
    }
}
