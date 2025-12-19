package com.thelinkphone.app.utils;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;

import com.thelinkphone.app.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Calendar;

public class OtherUtils {
    public static boolean checkPer(Context context, String str) {
        return ActivityCompat.checkSelfPermission(context, str) == 0;
    }

    public static boolean checkPer(Activity activity) {
        TelecomManager telecomManager = (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
        return (telecomManager == null || activity.getPackageName().equals(telecomManager.getDefaultDialerPackage()))
                ? false
                : true;
    }

    public static boolean checkPermission(Context context) {
        String[] strArr = { "android.permission.READ_CONTACTS", "android.permission.CALL_PHONE",
                "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG",
                "android.permission.READ_PHONE_STATE" };
        for (int i = 0; i < 5; i++) {
            if (!checkPer(context, strArr[i])) {
                return false;
            }
        }
        return true;
    }

    public static void requestDefault(Activity activity, ActivityResultLauncher<Intent> activityResultLauncher) {
        TelecomManager telecomManager = (TelecomManager) activity.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager == null || activity.getPackageName().equals(telecomManager.getDefaultDialerPackage())) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager roleManager = (RoleManager) activity.getSystemService(RoleManager.class);
            if (roleManager.isRoleAvailable("android.app.role.DIALER")
                    && !roleManager.isRoleHeld("android.app.role.DIALER")) {
                activityResultLauncher.launch(roleManager.createRequestRoleIntent("android.app.role.DIALER"));
                return;
            }
        }
        try {
            Intent intent = new Intent("android.telecom.action.CHANGE_DEFAULT_DIALER");
            intent.putExtra("android.telecom.extra.CHANGE_DEFAULT_DIALER_PACKAGE_NAME", activity.getPackageName());
            activityResultLauncher.launch(intent);
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(activity, (int) R.string.error, Toast.LENGTH_SHORT).show();
            activity.finish();
        }
    }

    public static int getWidthScreen(Context context) {
        return Math.min(context.getResources().getDisplayMetrics().widthPixels,
                context.getResources().getDisplayMetrics().heightPixels);
    }

    public static int getHeightScreen(Context context) {
        return Math.max(context.getResources().getDisplayMetrics().widthPixels,
                context.getResources().getDisplayMetrics().heightPixels) + MyShare.getSizeNotification(context);
    }

    public static GradientDrawable bgMain(int i, float f) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(new float[] { f, f, f, f, 0.0f, 0.0f, 0.0f, 0.0f });
        gradientDrawable.setColor(i);
        return gradientDrawable;
    }

    public static GradientDrawable bgIcon(int i, float f) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(f);
        gradientDrawable.setColor(i);
        return gradientDrawable;
    }

    public static GradientDrawable bgOval(int i) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setColor(i);
        return gradientDrawable;
    }

    public static StateListDrawable selNum(String str, String str2) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.setExitFadeDuration(400);
        stateListDrawable.addState(new int[] { 16842919 }, bgOval(Color.parseColor(str2)));
        stateListDrawable.addState(new int[0], bgOval(Color.parseColor(str)));
        return stateListDrawable;
    }

    public static StateListDrawable selLayout(String str, String str2) {
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.setExitFadeDuration(400);
        stateListDrawable.addState(new int[] { 16842919 }, bgIcon(Color.parseColor(str2), 1000.0f));
        stateListDrawable.addState(new int[0], bgIcon(Color.parseColor(str), 1000.0f));
        return stateListDrawable;
    }

    public static void drawRectTextFit(Canvas canvas, Paint paint, String str, Rect rect) {
        drawRectTextFit(canvas, paint, Paint.Align.CENTER, str, rect);
    }

    public static void drawRectTextFit(Canvas canvas, Paint paint, Paint.Align align, String str, Rect rect) {
        paint.setTextSize(100.0f);
        paint.setTextAlign(align);
        Rect rect2 = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect2);
        paint.setTextSize(Math.min(((rect.width() * 0.95f) / rect2.width()) * 100.0f,
                ((rect.height() * 0.95f) / rect2.height()) * 100.0f));
        paint.getTextBounds(str, 0, str.length(), rect2);
        float height = rect.top + (rect.height() / 2.0f) + (rect2.height() / 2.0f);
        float f = rect.left;
        if (align == Paint.Align.CENTER) {
            f = rect.exactCenterX();
        }
        canvas.drawText(str, f, height, paint);
    }

    public static String getStore(Context context) {
        if (Build.VERSION.SDK_INT >= 29) {
            File externalFilesDir = context.getExternalFilesDir(null);
            return externalFilesDir != null ? externalFilesDir.getAbsolutePath() : "/storage/emulated/0";
        }
        return Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName();
    }

    public static String getPathTheme(Context context) {
        return getStore(context) + "/theme/";
    }

    public static String getPathImage(Context context) {
        return getStore(context) + "/image/";
    }

    public static String getPathFakeCall(Context context) {
        return getStore(context) + "/fakecall/";
    }

    public static String getPathSave(Context context) {
        return getStore(context) + "/audio";
    }

    public static String getTextInAssets(Context context, String str) {
        try {
            InputStream open = context.getAssets().open(str);
            byte[] bArr = new byte[open.available()];
            open.read(bArr);
            open.close();
            return new String(bArr);
        } catch (IOException unused) {
            return "";
        }
    }

    public static boolean checkCanDrawOtherApp(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public static String longToTime(Context context, long j) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(j);
        if (calendar2.get(1) == calendar.get(1)) {
            if (calendar2.get(6) == calendar.get(6)) {
                return setNum(calendar2.get(11)) + ":" + setNum(calendar2.get(12));
            } else if (calendar.get(6) - calendar2.get(6) == 1) {
                return context.getString(R.string.yesterday);
            } else {
                if (calendar.get(6) - calendar2.get(6) < 7) {
                    return dayToString(context, calendar2.get(7));
                }
                return setNum(calendar2.get(5)) + "/" + setNum(calendar2.get(2) + 1) + "/" + calendar2.get(1);
            }
        }
        return setNum(calendar2.get(5)) + "/" + setNum(calendar2.get(2) + 1) + "/" + calendar2.get(1);
    }

    public static String longToTimeTitle(Context context, long j) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(j);
        if (calendar2.get(1) == calendar.get(1)) {
            if (calendar2.get(6) == calendar.get(6)) {
                return context.getString(R.string.today);
            }
            if (calendar.get(6) - calendar2.get(6) == 1) {
                return context.getString(R.string.yesterday);
            }
            if (calendar.get(6) - calendar2.get(6) < 7) {
                return dayToString(context, calendar2.get(7));
            }
            return longToDate(context, calendar2);
        }
        return longToDate(context, calendar2);
    }

    private static String longToDate(Context context, Calendar calendar) {
        return getMonthFull(context, calendar.get(2)) + " " + calendar.get(5) + ", " + calendar.get(1);
    }

    public static String longToTime(long j) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(j);
        return setNum(calendar.get(11)) + ":" + setNum(calendar.get(12));
    }

    public static String getDur(Context context, long j) {
        int i = (int) (j / 3600);
        long j2 = j % 3600;
        int i2 = (int) (j2 / 60);
        int i3 = (int) (j2 % 60);
        StringBuilder sb = new StringBuilder();
        if (i > 0) {
            sb.append(i);
            sb.append(" ");
            sb.append(context.getString(R.string.hour));
            sb.append(" ");
        }
        if (i2 > 0) {
            sb.append(i2);
            sb.append(" ");
            sb.append(context.getString(R.string.minute));
            sb.append(" ");
        }
        if (i3 > 0) {
            sb.append(i3);
            sb.append(" ");
            sb.append(context.getString(R.string.seconds));
        }
        return sb.toString();
    }

    public static String longToDur(int i) {
        return setNum(i / 60) + ":" + setNum(i % 60);
    }

    private static String setNum(int i) {
        if (i < 10) {
            return "0" + i;
        }
        return "" + i;
    }

    private static String dayToString(Context context, int i) {
        switch (i) {
            case 2:
                return context.getResources().getString(R.string.monday);
            case 3:
                return context.getResources().getString(R.string.tuesday);
            case 4:
                return context.getResources().getString(R.string.wednesday);
            case 5:
                return context.getResources().getString(R.string.thursday);
            case 6:
                return context.getResources().getString(R.string.friday);
            case 7:
                return context.getResources().getString(R.string.saturday);
            default:
                return context.getResources().getString(R.string.sunday);
        }
    }

    public static String getMonthFull(Context context, int i) {
        switch (i) {
            case 0:
                return context.getString(R.string.full_january);
            case 1:
                return context.getString(R.string.full_february);
            case 2:
                return context.getString(R.string.full_march);
            case 3:
                return context.getString(R.string.full_april);
            case 4:
                return context.getString(R.string.full_may);
            case 5:
                return context.getString(R.string.full_june);
            case 6:
                return context.getString(R.string.full_july);
            case 7:
                return context.getString(R.string.full_august);
            case 8:
                return context.getString(R.string.full_september);
            case 9:
                return context.getString(R.string.full_october);
            case 10:
                return context.getString(R.string.full_november);
            default:
                return context.getString(R.string.full_december);
        }
    }

    public static String getReadableFileSize(long j) {
        double d = 0;
        if (j <= 0) {
            return "0";
        }
        try {
            int log10 = (int) (Math.log10(j) / Math.log10(1024.0d));
            return new DecimalFormat("#,##0.#").format(d / Math.pow(1024.0d, log10)) + " "
                    + new String[] { "B", "KB", "MB", "GB", "TB" }[log10];
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(-12434878);
        canvas.drawCircle(bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f, bitmap.getWidth() / 2.0f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return createBitmap;
    }

    public static void copyToClip(Context context, String str) {
        if (str == null || str.isEmpty()) {
            return;
        }
        vibrator(context);
        ((ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("number", str));
        Toast.makeText(context, (int) R.string.copy, Toast.LENGTH_SHORT).show();
    }

    public static void sendMessage(Context context, String str) {
        if (str == null || str.isEmpty()) {
            return;
        }
        String join = TextUtils.join(";", new String[] { str });
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.setData(Uri.fromParts("smsto", join, null));
        context.startActivity(intent);
    }

    public static void call(Context context, String str, PhoneAccountHandle phoneAccountHandle) {
        android.util.Log.d("OtherUtils", "Attempting to place call to: " + str);
        android.util.Log.d("OtherUtils",
                "PhoneAccountHandle: " + (phoneAccountHandle != null ? phoneAccountHandle.toString() : "null"));

        // Check if we have CALL_PHONE permission
        boolean hasCallPermission = context
                .checkSelfPermission("android.permission.CALL_PHONE") == PackageManager.PERMISSION_GRANTED;
        android.util.Log.d("OtherUtils", "Has CALL_PHONE permission: " + hasCallPermission);

        // Check if app is set as default dialer
        TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null) {
            String defaultDialer = telecomManager.getDefaultDialerPackage();
            String currentPackage = context.getPackageName();
            boolean isDefaultDialer = currentPackage.equals(defaultDialer);
            android.util.Log.d("OtherUtils", "Default dialer: " + defaultDialer);
            android.util.Log.d("OtherUtils", "Current package: " + currentPackage);
            android.util.Log.d("OtherUtils", "Is default dialer: " + isDefaultDialer);

            if (!isDefaultDialer) {
                android.util.Log.w("OtherUtils", "App is not set as default dialer - this may cause call failures");
                Toast.makeText(context, "Please set this app as your default dialer to make calls", Toast.LENGTH_LONG)
                        .show();
            }
        }

        Intent callIntentForEmergencyNumber;
        Uri callUri = getCallUri(str);
        android.util.Log.d("OtherUtils", "Call URI: " + callUri.toString());

        if (!hasCallPermission) {
            android.util.Log.d("OtherUtils", "No CALL_PHONE permission, using DIAL intent");
            callIntentForEmergencyNumber = getCallIntentForEmergencyNumber(callUri);
        } else {
            boolean isEmergency = PhoneNumberUtils.isEmergencyNumber(str);
            android.util.Log.d("OtherUtils", "Is emergency number: " + isEmergency);
            callIntentForEmergencyNumber = isEmergency ? getCallIntentForEmergencyNumber(callUri)
                    : getCallIntent(callUri);
        }

        android.util.Log.d("OtherUtils", "Call intent action: " + callIntentForEmergencyNumber.getAction());

        try {
            callIntentForEmergencyNumber.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            callIntentForEmergencyNumber.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (phoneAccountHandle != null) {
                callIntentForEmergencyNumber.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandle);
                android.util.Log.d("OtherUtils", "Added PhoneAccountHandle to intent");
            }

            if (telecomManager != null) {
                android.util.Log.d("OtherUtils", "Placing call via TelecomManager");
                telecomManager.placeCall(callIntentForEmergencyNumber.getData(),
                        callIntentForEmergencyNumber.getExtras());
                android.util.Log.d("OtherUtils", "Call placed successfully");
            } else {
                android.util.Log.e("OtherUtils", "TelecomManager is null");
                Toast.makeText(context, "Unable to access phone service", Toast.LENGTH_SHORT).show();
                return;
            }

            // Start the call activity
            context.startActivity(callIntentForEmergencyNumber);
            android.util.Log.d("OtherUtils", "Call activity started");

        } catch (SecurityException e) {
            android.util.Log.e("OtherUtils", "SecurityException placing call: " + e.getMessage());
            Toast.makeText(context, "Permission denied to place call", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("OtherUtils", "Exception placing call: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(context, (int) R.string.no_app_found, Toast.LENGTH_SHORT).show();
        }
    }

    private static Intent getCallIntentForEmergencyNumber(Uri uri) {
        return new Intent("android.intent.action.DIAL", uri);
    }

    public static Intent getCallIntent(Uri uri) {
        return new Intent("android.intent.action.CALL", uri);
    }

    public static Uri getCallUri(String str) {
        if (PhoneNumberHelper.isUriNumber(str)) {
            return Uri.fromParts("sip", str, null);
        }
        return Uri.fromParts("tel", str, null);
    }

    public static void anim(View view, int i, boolean z) {
        anim(view, AnimationUtils.loadAnimation(view.getContext(), i), z);
    }

    private static void anim(final View view, Animation animation, final boolean z) {
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation2) {
            }

            @Override
            public void onAnimationStart(Animation animation2) {
            }

            @Override
            public void onAnimationEnd(Animation animation2) {
                if (z) {
                    view.setVisibility(View.INVISIBLE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }
        });
        view.startAnimation(animation);
    }

    public static void deleteFile(final String str) {
        new Thread(new Runnable() {
            @Override
            public final void run() {
                OtherUtils.lambda$deleteFile$0(str);
            }
        }).start();
    }

    public static void lambda$deleteFile$0(String str) {
        File file = new File(str);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void deleteAllFileInFolder(final String str, final boolean z) {
        new Thread(new Runnable() {
            @Override
            public final void run() {
                OtherUtils.lambda$deleteAllFileInFolder$1(str, z);
            }
        }).start();
    }

    public static void lambda$deleteAllFileInFolder$1(String str, boolean z) {
        deleteFolder(str);
        if (z) {
            new File(str).delete();
        }
    }

    private static void deleteFolder(String str) {
        File[] listFiles = new File(str).listFiles();
        if (listFiles == null || listFiles.length <= 0) {
            return;
        }
        for (File file : listFiles) {
            if (file.isDirectory()) {
                deleteFolder(file.getPath());
            }
            file.delete();
        }
    }

    public static void pickImage(ActivityResultLauncher<Intent> activityResultLauncher) {
        Intent addCategory = new Intent("android.intent.action.OPEN_DOCUMENT").setType("image/*")
                .addCategory("android.intent.category.OPENABLE");
        addCategory.putExtra("android.intent.extra.MIME_TYPES", new String[] { "image/jpeg", "image/png" });
        activityResultLauncher.launch(addCategory);
    }

    public static GradientDrawable bgLayout(Context context, int i) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius((context.getResources().getDisplayMetrics().widthPixels * 4.0f) / 80.0f);
        gradientDrawable.setColor(i);
        return gradientDrawable;
    }

    public static void vibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(100L, -1));
        } else {
            vibrator.vibrate(100L);
        }
    }

    public static String[] arrAlphaB() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ#".split("");
    }

    /**
     * Normalizes a phone number by removing non-numeric characters and country
     * codes.
     * Handles Indian country code (91) and ensures 10-digit numbers.
     * 
     * @param phoneNumber The phone number to normalize (can be null)
     * @return Normalized phone number string, or empty string if input is null
     */
    public static String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return "";
        }

        String cleanNumber = phoneNumber.replaceAll("[^0-9]", "");

        if (cleanNumber.startsWith("91") && cleanNumber.length() == 12) {
            return cleanNumber.substring(2);
        }

        if (cleanNumber.length() > 10) {
            return cleanNumber.substring(cleanNumber.length() - 10);
        }

        return cleanNumber;
    }
}
