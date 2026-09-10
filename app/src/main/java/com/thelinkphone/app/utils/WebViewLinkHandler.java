package com.thelinkphone.app.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;

/**
 * Shared logic for handling special URL schemes and external hand-offs
 * (tel:, mailto:, market:, whatsapp://, video meetings, maps, CallaLink deep links)
 * from a WebView. Used by any fragment hosting a WebView that needs this behavior,
 * so the URL-handling rules live in exactly one place.
 */
public final class WebViewLinkHandler {

    public interface Reloadable {
        void reloadWebView();
        default void loadUrl(String url) {
            reloadWebView(); // fallback for anything that doesn't override this
        }
    }

    private static WeakReference<Reloadable> pendingOAuthTarget;
    private WebViewLinkHandler() {
        // no instances
    }

    /**
     * Attempts to handle a URL via a special scheme or external hand-off.
     * @return true if the URL was handled (caller should not load it in the WebView), false otherwise.
     */
    public static boolean handleSpecialUrl(Fragment fragment, WebView view, String url) {
        if (url == null) return false;

        if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            fragment.startActivity(intent);
            return true;
        }

        if (url.startsWith("mailto:")) {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            fragment.startActivity(i);
            return true;
        }

        if (url.startsWith("market:") || (Uri.parse(url).getScheme() != null && Uri.parse(url).getScheme().equals("market"))) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                Activity host = (Activity) view.getContext();
                host.startActivity(intent);
                return true;
            } catch (ActivityNotFoundException e) {
                Uri uri = Uri.parse(url);
                view.loadUrl("http://play.google.com/store/apps/" + uri.getHost() + "?" + uri.getQuery());
                return true;
            }
        }

        if (url.startsWith("whatsapp://")) {
            view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        if (isOAuthConsentUrl(url)) {
            if (fragment instanceof Reloadable) {
                pendingOAuthTarget = new WeakReference<>((Reloadable) fragment);
            }
            launchCustomTab(view.getContext(), url);
            return true;
        }

        if (shouldHandOffExternally(url)) {
            return launchExternallyOrBrowser(fragment, url);
        }

        return false;
    }

    private static void launchCustomTab(Context context, String url) {
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build();
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }

    public static Reloadable consumePendingOAuthTarget() {
        Reloadable target = pendingOAuthTarget != null ? pendingOAuthTarget.get() : null;
        pendingOAuthTarget = null;
        return target;
    }

    private static boolean isVideoMeetingUrl(String url) {
        if (url == null) return false;
        return url.startsWith("zoommtg://")
                || url.contains("meet.google.com")
                || url.contains("zoom.us/j/")
                || url.contains("zoom.us/wc/");
    }

    private static boolean isMapsUrl(String url) {
        if (url == null) return false;
        return url.contains("google.com/maps") || url.contains("maps.google.com");
    }

    private static boolean isCallalinkDeepLink(String url) {
        if (url == null) return false;
        return url.contains("/call/");
    }

    private static boolean isOAuthConsentUrl(String url) {
        if (url == null) return false;
        return url.contains("accounts.google.com/o/oauth2")
                || url.contains("accounts.google.com/signin")
                || url.contains("zoom.us/oauth")
                || url.contains("zoom.us/launch/oauth");
    }

    private static boolean shouldHandOffExternally(String url) {
        return isVideoMeetingUrl(url) || isMapsUrl(url) || isCallalinkDeepLink(url);
    }

    private static boolean launchExternallyOrBrowser(Fragment fragment, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            fragment.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            fragment.startActivity(browserIntent);
        }
        return true;
    }
}