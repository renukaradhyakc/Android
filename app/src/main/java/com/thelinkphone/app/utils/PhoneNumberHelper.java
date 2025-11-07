package com.thelinkphone.app.utils;

import android.telephony.PhoneNumberUtils;
import android.util.Log;


public class PhoneNumberHelper {
    private static final String KOREA_ISO_COUNTRY_CODE = "KR";
    private static final String LOG_TAG = "PhoneNumberHelper";

    public static boolean isUriNumber(String str) {
        return str != null && (str.contains("@") || str.contains("%40"));
    }

    public static String normalizeNumber(String str) {
        StringBuilder sb = new StringBuilder();
        int length = str.length();
        for (int i = 0; i < length; i++) {
            char charAt = str.charAt(i);
            int digit = Character.digit(charAt, 10);
            if (digit != -1) {
                sb.append(digit);
            } else if (i == 0 && charAt == '+') {
                sb.append(charAt);
            } else if ((charAt >= 'a' && charAt <= 'z') || (charAt >= 'A' && charAt <= 'Z')) {
                return normalizeNumber(PhoneNumberUtils.convertKeypadLettersToDigits(str));
            }
        }
        return sb.toString();
    }

    public static String getUsernameFromUriNumber(String str) {
        int indexOf = str.indexOf(64);
        if (indexOf < 0) {
            indexOf = str.indexOf("%40");
        }
        if (indexOf < 0) {
            String str2 = LOG_TAG;
            Log.w(str2, "getUsernameFromUriNumber: no delimiter found in SIP addr '" + str + "'");
            return str;
        }
        return str.substring(0, indexOf);
    }
}
