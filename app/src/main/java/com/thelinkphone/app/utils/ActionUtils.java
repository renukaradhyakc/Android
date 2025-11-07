package com.thelinkphone.app.utils;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.Fragment;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemContact;



public class ActionUtils {



    public static void openLink(Context context, String str) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(str.replace("HTTPS", "https")));
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(context, (int) R.string.no_browser, Toast.LENGTH_SHORT).show();
        }
    }



    public static void shareApp(Context context) {
        shareText(context, "https://play.google.com/store/apps/details?id=" + context.getPackageName());
    }

    public static void shareText(Context context, String str) {
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.SUBJECT", context.getString(R.string.app_name));
        intent.putExtra("android.intent.extra.TEXT", str);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.app_name)));
    }

    public static void ratePkg(Context context, String str) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + str));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 2);
    }

    public static void showKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }
        inputMethodManager.showSoftInput(view, 1);
    }

    public static void actionCall(Context context, String str) {
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(str)));
            context.startActivity(intent);
        } catch (ActivityNotFoundException unused) {
            Toast.makeText(context, (int) R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public static void onNewContact(ActivityResultLauncher<Intent> activityResultLauncher, String str) {
        Intent intent = new Intent("android.intent.action.INSERT");
        intent.setType("vnd.android.cursor.dir/contact");
        intent.putExtra("phone", str);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        activityResultLauncher.launch(intent);
    }

    public static void onNewContact(Fragment fragment, String str) {
        Intent intent = new Intent("android.intent.action.INSERT");
        intent.setType("vnd.android.cursor.dir/contact");
        intent.putExtra("phone", str);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        fragment.startActivityForResult(intent, 1);
    }

    public static void onEditContact(Context context, ActivityResultLauncher<Intent> activityResultLauncher, ItemContact itemContact) {
        if (itemContact.getId() == null || itemContact.getId().isEmpty()) {
            if (itemContact.getArrPhone().size() > 0) {
                onNewContact(activityResultLauncher, itemContact.getArrPhone().get(0).getNumber());
                return;
            } else {
                Toast.makeText(context, (int) R.string.error, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        Intent intent = new Intent("android.intent.action.EDIT");
        intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(itemContact.getId())));
        intent.putExtra("finishActivityOnSaveCompleted", true);
        activityResultLauncher.launch(intent);
    }
}
