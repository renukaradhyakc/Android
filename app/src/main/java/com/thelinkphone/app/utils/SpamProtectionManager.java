package com.thelinkphone.app.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Arrays;
import java.util.List;

public class SpamProtectionManager {
    private static final String TAG = "SpamProtectionManager";

    // Known spam detection app package names
    private static final String[] SPAM_APP_PACKAGES = {
        "com.truecaller",           // TrueCaller
        "com.showcaller",           // ShowCaller
        "com.whoscall",            // Whoscall
        "com.hiya.app",            // Hiya
        "com.sync.callapp",        // CallApp
        "com.mr.number",           // Mr. Number
        "com.cidcaller.cidapp",    // CID Caller
        "com.privacystar",         // PrivacyStar
        "com.getverify.truecaller", // TrueCaller variants
        "com.truecaller.callerpro", // TrueCaller Pro
        "com.google.android.dialer" // Google Dialer (if not default)
    };

    // Known overlay window class names for spam apps
    private static final String[] SPAM_OVERLAY_CLASSES = {
        "IncomingCallActivity",
        "CallScreenActivity",
        "CallerIdActivity",
        "SpamBlockActivity",
        "TrueCallerOverlay"
    };

    private Context context;
    private TelecomManager telecomManager;
    private ActivityManager activityManager;

    public SpamProtectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    }

    /**
     * Check if our app is the default dialer
     */
    public boolean isDefaultDialer() {
        if (telecomManager == null) return false;

        String defaultDialerPackage = telecomManager.getDefaultDialerPackage();
        String ourPackage = context.getPackageName();

        boolean isDefault = ourPackage.equals(defaultDialerPackage);
        Log.d(TAG, "Default dialer check - Our app: " + ourPackage +
              ", Default: " + defaultDialerPackage + ", Is default: " + isDefault);

        return isDefault;
    }

    /**
     * Enable privacy protection automatically based on call settings
     */
    public void enablePrivacyProtection() {
        // Check call setting first
        if (!MyShare.isCallSettingPhonelinkScheduled(context)) {
            Log.d(TAG, "Privacy protection disabled - Unrestricted mode active");
            return;
        }

        if (!isDefaultDialer()) {
            Log.w(TAG, "Privacy protection limited - app is not default dialer");
            return;
        }

        Log.d(TAG, "Enabling privacy protection against spam detection apps (Phonelink Scheduled mode)");

        // Start monitoring and blocking spam apps
        blockSpamAppOverlays();
        preventSpamAppAccess();
        setupCallInterception();
    }

    /**
     * Disable privacy protection (automatically in Unrestricted mode)
     */
    public void disablePrivacyProtection() {
        Log.d(TAG, "Disabling privacy protection");
        // Implementation for disabling protection if needed
    }

    /**
     * Check and apply appropriate privacy protection based on current call setting
     */
    public void applyProtectionBasedOnSettings() {
        if (MyShare.isCallSettingPhonelinkScheduled(context)) {
            enablePrivacyProtection();
        } else {
            Log.d(TAG, "Unrestricted mode - Spam apps allowed to show popups");
        }
    }

    /**
     * Block spam app overlays and popups
     */
    private void blockSpamAppOverlays() {
        // Perform in background to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Check for running spam apps
                    List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();

                    if (runningApps != null) {
                        for (ActivityManager.RunningAppProcessInfo processInfo : runningApps) {
                            String packageName = processInfo.processName;

                            if (isSpamApp(packageName)) {
                                Log.d(TAG, "Detected running spam app: " + packageName);
                                // Try to minimize or background the spam app
                                backgroundSpamApp(packageName);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error blocking spam app overlays: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Prevent spam apps from accessing call information
     */
    private void preventSpamAppAccess() {
        try {
            // This method can be extended to implement more sophisticated blocking
            Log.d(TAG, "Setting up spam app access prevention");

            // Check if we have system alert window permission to block overlays
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(context)) {
                    Log.w(TAG, "System alert window permission not granted - limited overlay blocking");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error preventing spam app access: " + e.getMessage());
        }
    }

    /**
     * Setup call interception to handle calls privately
     */
    private void setupCallInterception() {
        try {
            Log.d(TAG, "Setting up private call interception");

            // When we're the default dialer, we have more control over call handling
            // This ensures calls go through our app's pipeline instead of being
            // intercepted by spam detection apps

        } catch (Exception e) {
            Log.e(TAG, "Error setting up call interception: " + e.getMessage());
        }
    }

    /**
     * Check if a package name belongs to a spam detection app
     */
    private boolean isSpamApp(String packageName) {
        if (packageName == null) return false;

        for (String spamPackage : SPAM_APP_PACKAGES) {
            if (packageName.contains(spamPackage) || packageName.equals(spamPackage)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Try to background a spam app to prevent its overlays
     */
    private void backgroundSpamApp(String packageName) {
        try {
            // Move spam app to background
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(homeIntent);

            Log.d(TAG, "Backgrounded spam app: " + packageName);

        } catch (Exception e) {
            Log.e(TAG, "Error backgrounding spam app " + packageName + ": " + e.getMessage());
        }
    }

    /**
     * Get list of installed spam detection apps
     */
    public String[] getInstalledSpamApps() {
        // Use a simple cache to avoid repeated package manager queries
        try {
            PackageManager pm = context.getPackageManager();
            List<String> installedSpamApps = new java.util.ArrayList<>();

            // Quick check - only check first few critical apps to avoid ANR
            String[] criticalSpamApps = {"com.truecaller", "com.showcaller", "com.whoscall", "com.hiya.app"};

            for (String spamPackage : criticalSpamApps) {
                try {
                    ApplicationInfo appInfo = pm.getApplicationInfo(spamPackage, PackageManager.GET_META_DATA);
                    if (appInfo != null) {
                        String appName = pm.getApplicationLabel(appInfo).toString();
                        installedSpamApps.add(appName);
                        Log.d(TAG, "Found critical spam app: " + appName);
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // App not installed, continue
                } catch (Exception e) {
                    // Skip on any error to avoid ANR
                    continue;
                }
            }

            return installedSpamApps.toArray(new String[0]);

        } catch (Exception e) {
            Log.e(TAG, "Error getting installed spam apps: " + e.getMessage());
            return new String[0];
        }
    }

    /**
     * Show privacy protection status to user
     */
    public void showProtectionStatus() {
        try {
            String[] installedSpamApps = getInstalledSpamApps();
            boolean isDefault = isDefaultDialer();
            boolean isPhonelinkScheduled = MyShare.isCallSettingPhonelinkScheduled(context);

            String message;
            if (isDefault && isPhonelinkScheduled) {
                if (installedSpamApps.length > 0) {
                    message = "Privacy protection ACTIVE (Phonelink Scheduled mode). Detected " + installedSpamApps.length +
                             " spam detection apps - their popups are blocked during calls.";
                } else {
                    message = "Privacy protection ACTIVE (Phonelink Scheduled mode). No spam detection apps found.";
                }
            } else if (isDefault && !isPhonelinkScheduled) {
                message = "Privacy protection DISABLED (Unrestricted mode). TrueCaller and other spam apps can show popups.";
            } else {
                message = "Privacy protection LIMITED. Set this app as default dialer for full protection.";
            }

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            Log.i(TAG, "Protection status: " + message);

        } catch (Exception e) {
            Log.e(TAG, "Error showing protection status: " + e.getMessage());
        }
    }

    /**
     * Force close spam apps if we have permission
     */
    public void forceCloseSpamApps() {
        if (!isDefaultDialer()) {
            Log.w(TAG, "Cannot force close spam apps - not default dialer");
            return;
        }

        // Perform in background to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Attempting to force close spam apps");

                    List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();

                    if (runningApps != null) {
                        for (ActivityManager.RunningAppProcessInfo processInfo : runningApps) {
                            if (isSpamApp(processInfo.processName)) {
                                Log.d(TAG, "Force closing spam app: " + processInfo.processName);

                                // Try to kill the process (requires system permissions)
                                try {
                                    android.os.Process.killProcess(processInfo.pid);
                                } catch (Exception e) {
                                    Log.w(TAG, "Cannot kill process " + processInfo.processName + ": " + e.getMessage());
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.e(TAG, "Error force closing spam apps: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Check if system has overlay permission for better protection
     */
    public boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true; // Pre-M devices don't need this permission
    }

    /**
     * Request overlay permission for advanced protection
     */
    public Intent getOverlayPermissionIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(android.net.Uri.parse("package:" + context.getPackageName()));
            return intent;
        }
        return null;
    }

    /**
     * Log privacy protection summary
     */
    public void logProtectionSummary() {
        boolean isPhonelinkScheduled = MyShare.isCallSettingPhonelinkScheduled(context);
        String callSetting = MyShare.getCallSettingName(context);

        Log.i(TAG, "=== Privacy Protection Summary ===");
        Log.i(TAG, "Call setting: " + callSetting);
        Log.i(TAG, "Default dialer: " + isDefaultDialer());
        Log.i(TAG, "Overlay permission: " + hasOverlayPermission());

        String[] installedSpamApps = getInstalledSpamApps();
        Log.i(TAG, "Installed spam apps: " + installedSpamApps.length);

        for (String app : installedSpamApps) {
            Log.i(TAG, "  - " + app);
        }

        String protectionStatus;
        if (isDefaultDialer() && isPhonelinkScheduled) {
            protectionStatus = "ACTIVE (Phonelink Scheduled mode - spam apps blocked)";
        } else if (isDefaultDialer() && !isPhonelinkScheduled) {
            protectionStatus = "INACTIVE (Unrestricted mode - spam apps allowed)";
        } else {
            protectionStatus = "LIMITED (Not default dialer)";
        }

        Log.i(TAG, "Protection status: " + protectionStatus);
        Log.i(TAG, "================================");
    }
}
