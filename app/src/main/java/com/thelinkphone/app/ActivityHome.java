package com.thelinkphone.app;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.thelinkphone.app.custom.LayoutChooseContact;
import com.thelinkphone.app.custom.ViewTabMode;
import com.thelinkphone.app.dialog.FavResult;
import com.thelinkphone.app.fragment.BillCaptureFragment;
import com.thelinkphone.app.fragment.ContactResult;
import com.thelinkphone.app.fragment.EventsFragment;
import com.thelinkphone.app.fragment.FragmentContact;
import com.thelinkphone.app.fragment.FragmentFavorites;
import com.thelinkphone.app.fragment.FragmentInfo;
import com.thelinkphone.app.fragment.FragmentPad;
import com.thelinkphone.app.fragment.FragmentRecents;
import com.thelinkphone.app.fragment.FragmentSetting;
import com.thelinkphone.app.fragment.ScanFrag;
import com.thelinkphone.app.fragment.ScheduledEventsFrag;
import com.thelinkphone.app.fragment.SettingsFragment;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.model.QrRequest;
import com.thelinkphone.app.service.IncomingCallPopupService;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.CallDisplayMode;
import com.thelinkphone.app.utils.MyConst;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ReadContact;
import com.thelinkphone.app.utils.SpamProtectionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;


public class ActivityHome extends AppCompatActivity {
    private static final String TAG = "ActivityHome";
    private ArrayList<ItemContact> arrAllContact;
    private final ContactResult contactResult = new AnonymousClass1();
    private FragmentContact fragmentContact;
    private FragmentFavorites fragmentFavorites;
    private FragmentPad fragmentPad;
    private FragmentRecents fragmentRecents;
    private ScanFrag fragmentScan;
    private BillCaptureFragment billCaptureFragment;

    private ScheduledEventsFrag scheduledEventsFrag;
    private EventsFragment eventsFragment;
    private FragmentSetting fragmentSetting;
    private SettingsFragment mSettingsFrag;
    private LayoutChooseContact layoutChooseContact;
    private int layoutPos;
    private LinearLayout llFragment;
    private String number;
    private int pos;
    private RelativeLayout rlMain;
    private boolean showAdsFist;

    private SharedPreferences sharedPreferences;
    private String token;
    private String email;

    private boolean isAccessChecked = false;
    private boolean hasAccess = false;
    private ViewTabMode viewTabMode;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_home);

        initializeSession();
        getDataCall();
        handleDeepLink(); // Handle deep links for LinkPhone calls
        initContact();
        initView();
        handleBillNavigation();
        /*AdAdmob adAdmob = new AdAdmob( this);
        adAdmob.FullscreenAd( this);*/

    }

    private void initializeSession() {
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        token = sharedPreferences.getString("auth_token", null);
        email = sharedPreferences.getString("user_email", null);

        Log.d(TAG, "Session initialized - Token: " + (token != null ? "present" : "null") +
                ", Email: " + (email != null ? "present" : "null"));
    }

    private void handleDeepLink() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        Uri data = intent.getData();

        Log.d(TAG, "Intent action: " + action);
        Log.d(TAG, "Intent data: " + (data != null ? data.toString() : "null"));

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();

            Log.d(TAG, "Deep link scheme: " + scheme + ", host: " + host);

            // Handle LinkPhone deep links
            if ("linkphone".equals(scheme) ||"callalink".equals(scheme) ||
                ("https".equals(scheme) && "app.callalink.com".equals(host))) {

                String userParam = null;

                // Handle HTTPS URLs like: https://www.app.thelinkphone.com/call/codpr1044p
                if ("https".equals(scheme) && data.getPath() != null && data.getPath().startsWith("/call/")) {
                    userParam = data.getPath().substring("/call/".length());
                    Log.d(TAG, "HTTPS deep link user from path: " + userParam);
                }
                // Handle custom scheme URLs like: linkphone://call?user=codpr1044p
                else if ("callalink".equals(scheme) || "linkphone".equals(scheme)) {
                    userParam = data.getQueryParameter("user");
                    Log.d(TAG, "Custom scheme deep link user parameter: " + userParam);
                }

                if (userParam != null && !userParam.isEmpty()) {
                    // Process the LinkPhone deep link to get phone number
                    processLinkPhoneDeepLink(userParam);
                    return;
                }
            }
        }
    }

    private void processLinkPhoneDeepLink(String userParam) {

        SharedPreferences prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String token = prefs.getString("auth_token", null);

        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Processing LinkPhone deep link for user: " + userParam);

        // Show a toast to indicate deep link processing
        Toast.makeText(this, "Processing CallALink call link...", Toast.LENGTH_SHORT).show();

        // Use the same API call as QR scanning to get the phone number
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        QrRequest qrRequest = new QrRequest(userParam);

        apiService.scanQr("Bearer " + token,qrRequest).enqueue(
                new retrofit2.Callback<com.thelinkphone.app.model.QRResponse>() {
            @Override
            public void onResponse(
                    retrofit2.Call<com.thelinkphone.app.model.QRResponse> call,
                    retrofit2.Response<com.thelinkphone.app.model.QRResponse> response) {

                if (response.code() == 401) {
                    Toast.makeText(ActivityHome.this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    String phoneNumber = response.body().getPhoneNumber();
                    Log.d(TAG, "Deep link resolved to phone number: " + phoneNumber);


                    com.thelinkphone.app.utils.CallerInfoManager.storeLinkPhoneUsername(
                        ActivityHome.this, phoneNumber, userParam);

                    placeCallFromDeepLink(phoneNumber);
                } else {
                    Log.e(TAG, "Deep link user not found");
                    Toast.makeText(ActivityHome.this, "User not found for this CallALink link", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(
                    retrofit2.Call<com.thelinkphone.app.model.QRResponse> call,
                    Throwable t) {
                Log.e(TAG, "Deep link API call failed: " + t.getMessage());
                Toast.makeText(ActivityHome.this, "Failed to process CallALink link", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void placeCallFromDeepLink(String phoneNumber) {
        ArrayList<com.thelinkphone.app.item.ItemSimInfo> arrSim = com.thelinkphone.app.utils.SimUtils.getAvailableSIMCardLabels(this);
        int posSim = MyShare.getPosSim(this);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Invalid phone number from CallALink link", Toast.LENGTH_SHORT).show();
            return;
        }

        if (arrSim.size() == 0) {
            Toast.makeText(this, "No SIM card available", Toast.LENGTH_SHORT).show();
            return;
        }

        final android.telecom.PhoneAccountHandle finalPhoneAccountHandle;
        if (posSim < arrSim.size()) {
            finalPhoneAccountHandle = arrSim.get(posSim).handle;
        } else {
            finalPhoneAccountHandle = arrSim.get(0).handle;
        }

        final String finalPhoneNumber = phoneNumber;

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "Placing call from deep link to: " + finalPhoneNumber);
                    OtherUtils.call(ActivityHome.this, finalPhoneNumber, finalPhoneAccountHandle);
                } catch (Exception e) {
                    Log.e(TAG, "Error making call from deep link: " + e.getMessage());
                    Toast.makeText(ActivityHome.this, "Failed to make call", Toast.LENGTH_SHORT).show();
                }
            }
        }, 500);
    }

    private void getDataCall() {
        String action;
        this.layoutPos = MyShare.getLayout(this);
        Intent intent = getIntent();
        if (intent == null || (action = intent.getAction()) == null) {
            return;
        }
        if ((action.equals("android.intent.action.DIAL") || action.equals("android.intent.action.VIEW")) && intent.getData() != null && intent.getDataString() != null && intent.getDataString().contains("tel:")) {
            String dataString = intent.getDataString();
            this.number = Uri.decode(dataString).substring(dataString.indexOf("tel:") + 4);
            this.layoutPos = 3;
        }
    }

    private void initContact() {
        this.arrAllContact = new ArrayList<>();
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public final boolean handleMessage(Message message) {
                return ActivityHome.this.m44xb144c5a4(message);
            }
        });
        new Thread(new Runnable() {
            @Override
            public final void run() {
                ActivityHome.this.m45xb0ce5fa5(handler);
            }
        }).start();
    }



    public  boolean m44xb144c5a4(Message message) {
        FragmentContact fragmentContact = this.fragmentContact;
        if (fragmentContact != null) {
            fragmentContact.updateList();
        }
        FragmentFavorites fragmentFavorites = this.fragmentFavorites;
        if (fragmentFavorites != null) {
            fragmentFavorites.updateList();
            return true;
        }
        return true;
    }



    public  void m45xb0ce5fa5(Handler handler) {
        this.arrAllContact.addAll(ReadContact.getAllContact(this));
        handler.sendEmptyMessage(1);
    }

    private void initView() {
        rlMain = findViewById(R.id.ll_main);
        int widthScreen = OtherUtils.getWidthScreen(this);
        Log.e("rlMain", "" + rlMain);
        rlMain.setBackgroundColor(-16777216);
        this.llFragment = (LinearLayout) findViewById(R.id.ll_fragment);
        boolean theme = MyShare.getTheme(this);
        getWindow().setStatusBarColor(0);

        if (theme) {
            getWindow().setNavigationBarColor(Color.parseColor("#EFEFEF"));
            /*getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);*/
          //  getWindow().getDecorView().setSystemUiVisibility(Build.VERSION.SDK_INT >= 26 ? 9232 : 9216);
            this.llFragment.setBackground(OtherUtils.bgMain(-1, widthScreen / 50.0f));
        } else {
            getWindow().setNavigationBarColor(Color.parseColor("#2C2C2C"));
            /*getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        */  //  getWindow().getDecorView().setSystemUiVisibility(1024);
            this.llFragment.setBackground(OtherUtils.bgMain(Color.parseColor("#2C2C2C"), widthScreen / 50.0f));
        }
        viewTabMode = (ViewTabMode) findViewById(R.id.v_tab);
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewTabMode.setFragmentManager(fragmentManager,R.id.frame);
        viewTabMode.setTabResult(new ViewTabMode.TabResult() {
            @Override
            public final void onTapClick(int i) {
                ActivityHome.this.onTabClick(i);
            }
        });
        this.pos = this.layoutPos; // Synchronize pos with layoutPos
        viewTabMode.setTabDefault(this.layoutPos);
        this.showAdsFist = true;

        // Initialize privacy protection based on call settings
        initializePrivacyProtection();

        getBlockPermission();
    }

    private void initializePrivacyProtection() {
        // Move to background thread to avoid ANR
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SpamProtectionManager spamProtectionManager = new SpamProtectionManager(ActivityHome.this);

                    // Apply protection based on current call settings
                    spamProtectionManager.applyProtectionBasedOnSettings();
                    spamProtectionManager.logProtectionSummary();

                    // Log status based on settings
                    boolean isPhonelinkScheduled = MyShare.isCallSettingPhonelinkScheduled(ActivityHome.this);
                    boolean isDefaultDialer = spamProtectionManager.isDefaultDialer();

                    if (isDefaultDialer && isPhonelinkScheduled) {
                        Log.d(TAG, "Privacy protection ACTIVE - Phonelink Scheduled mode blocks spam apps");
                    } else if (isDefaultDialer && !isPhonelinkScheduled) {
                        Log.d(TAG, "Privacy protection INACTIVE - Unrestricted mode allows spam apps");
                    } else {
                        Log.w(TAG, "Privacy protection LIMITED - app is not default dialer");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing privacy protection: " + e.getMessage());
                }
            }
        }).start();
    }

    private void getBlockPermission()
    {
        if (Build.VERSION.SDK_INT >= 29) {
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager.isRoleHeld("android.app.role.CALL_SCREENING")) {
               // showDialogBlock();
                return;
            }
            ActivityHome.this.lPer.launch(roleManager.createRequestRoleIntent("android.app.role.CALL_SCREENING"));
            return;
        }
    }

    private final ActivityResultLauncher<Intent> lPer = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() {
        @Override
        public final void onActivityResult(Object obj) {
            m137x75ef4b8f((ActivityResult) obj);
        }
    });

    public  void m137x75ef4b8f(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
          //  this.viewInfoAnother.showDialogBlock();
        }
    }


    public void onTabClick(int i) {
        if (this.pos == i) {
            showTab();
            return;
        }
        this.pos = i;
        if (this.showAdsFist) {
            this.showAdsFist = false;
            ActivityHome.this.showTab();

            return;
        }
        showTab();
    }


    public void showTab() {
        Log.d("BillFlow", "showTab position = " + pos);
        MyShare.putLayout(this, this.pos);
        int i = this.pos;
//        if (i == 0) {
//            // Replace FragmentFavorites with ScanFrag
//            if (this.fragmentScan == null) {
//                ScanFrag scanFrag = new ScanFrag();
//                this.fragmentScan = scanFrag;
//                // Set any necessary parameters for ScanFrag
//            }
//            showFragment(this.fragmentScan, false);
        if (i == 0) {
            boolean billMode = MyShare.isBillMode(this);

            if (billMode) {
                showBillCaptureFragment();
            } else {
                showQrFragment();
            }
        /*if (i == 0) {
            if (this.fragmentFavorites == null) {
                FragmentFavorites fragmentFavorites = new FragmentFavorites();
                this.fragmentFavorites = fragmentFavorites;
                fragmentFavorites.setContactResult(this.contactResult);
            }
            showFragment(this.fragmentFavorites, false);*/
        } else if (i == 1) {
            if (this.scheduledEventsFrag == null) {
                ScheduledEventsFrag scheduledEventsFrag = new ScheduledEventsFrag();
                this.scheduledEventsFrag = scheduledEventsFrag;
//                fragmentRecents.setContactResult(this.contactResult);
            }
            showFragment(this.scheduledEventsFrag, false);
        } else if (i == 2) {
            if (this.eventsFragment == null) {
                EventsFragment eventsFragment = new EventsFragment();
                this.eventsFragment = eventsFragment;
               // fragmentContact.setContactResult(this.contactResult);
            }
            showFragment(this.eventsFragment, false);
        } else if (i == 3) {
            // Call Log Fragment
            if (this.fragmentRecents == null) {
                FragmentRecents fragmentRecents = new FragmentRecents();
                this.fragmentRecents = fragmentRecents;
                fragmentRecents.setContactResult(this.contactResult);
            }
            showFragment(this.fragmentRecents, false);
        } else if (i == 4) {
            if (this.fragmentPad == null) {
                FragmentPad fragmentPad = new FragmentPad();
                this.fragmentPad = fragmentPad;
                fragmentPad.setContactResult(this.contactResult);
            }
            showFragment(this.fragmentPad, false);
        } else {
            if (this.mSettingsFrag == null) {
                this.mSettingsFrag = new SettingsFragment();
            }
            showFragment(this.mSettingsFrag, false);
        }
    }

    public void showFragment(Fragment fragment, boolean z) {
        // Check if activity is finishing or destroyed to prevent crashes
        if (isFinishing() || isDestroyed()) {
            return;
        }

        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        beginTransaction.setReorderingAllowed(true);
        if (z) {
            beginTransaction.setCustomAnimations(R.anim.anim_fragment_in, R.anim.anim_fragment_out, R.anim.anim_fragment_pop_in, R.anim.anim_fragment_pop_out);
            beginTransaction.addToBackStack(fragment.getTag());
        } else if (!getSupportFragmentManager().isStateSaved()) {
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStack();
            }
        }
        beginTransaction.replace(R.id.frame, fragment);
        try {
            beginTransaction.commitAllowingStateLoss(); // Use commitAllowingStateLoss to prevent IllegalStateException
        } catch (Exception e) {
            Log.e(TAG, "Error committing fragment transaction: " + e.getMessage());
            Toast.makeText(this, (int) R.string.error, Toast.LENGTH_SHORT).show();
        }
    }

    public void onChangeTheme() {
        startActivity(new Intent(this, ActivityApplyTheme.class));
        finish();
    }

    public void addNewContact(ItemContact itemContact) {
        this.arrAllContact.add(itemContact);

        Collections.sort(this.arrAllContact, new Comparator<ItemContact>() {
            @Override
            public int compare(ItemContact o1, ItemContact o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());

            }
        });
    }

    public void updateContact(ItemContact itemContact) {
        Iterator<ItemContact> it = this.arrAllContact.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            ItemContact next = it.next();
            if (next.getId().equals(itemContact.getId())) {
                this.arrAllContact.remove(next);
                break;
            }
        }
        addNewContact(itemContact);
    }

    public void removeContact(String str) {
        Iterator<ItemContact> it = this.arrAllContact.iterator();
        while (it.hasNext()) {
            ItemContact next = it.next();
            if (next.getId().equals(str)) {
                this.arrAllContact.remove(next);
                return;
            }
        }
    }

    public ArrayList<ItemContact> getArrAllContact() {
        return this.arrAllContact;
    }

    public String getNumber() {
        return this.number;
    }

    public void showLayoutContact(FavResult favResult) {
        if (this.arrAllContact.size() == 0) {
            Toast.makeText(this, (int) R.string.empty_contact, Toast.LENGTH_SHORT).show();
            return;
        }
        if (this.layoutChooseContact == null) {
            this.layoutChooseContact = new LayoutChooseContact(this);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
            layoutParams.setMargins(0, (OtherUtils.getWidthScreen(this) * 13) / 100, 0, 0);
            this.rlMain.addView(this.layoutChooseContact, layoutParams);
        }
        this.layoutChooseContact.setFavResult(favResult);
        this.layoutChooseContact.show(this.llFragment, this.arrAllContact);
    }




    public class AnonymousClass1 implements ContactResult {
        AnonymousClass1() {
        }

        @Override
        public void onContactChange() {
            if (ActivityHome.this.fragmentContact != null) {
                ActivityHome.this.fragmentContact.updateList();
            }
            if (ActivityHome.this.fragmentPad != null) {
                ActivityHome.this.fragmentPad.checkNum();
            }
        }

        @Override
        public void onFavoritesChange() {
            if (ActivityHome.this.fragmentFavorites != null) {
                ActivityHome.this.fragmentFavorites.updateList();
            }
        }

        @Override
        public void onAddNewContact(final ItemRecentGroup itemRecentGroup, final ItemContact itemContact) {
            onContactChange();
            new Handler().postDelayed(new Runnable() {
                @Override
                public final void run() {
                    AnonymousClass1.this.m46x22192003(itemContact, itemRecentGroup);
                }
            }, 1000L);
        }



        public  void m46x22192003(ItemContact itemContact, ItemRecentGroup itemRecentGroup) {
            FragmentInfo newInstance = FragmentInfo.newInstance(itemContact, itemRecentGroup, R.string.back, CallDisplayMode.ALL);
            newInstance.setContactResult(ActivityHome.this.contactResult);
            ActivityHome.this.showFragment(newInstance, true);
        }

        @Override
        public void onBack() {
            ActivityHome.this.onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        LayoutChooseContact layoutChooseContact = this.layoutChooseContact;
        if (layoutChooseContact != null && layoutChooseContact.getVisibility() == View.VISIBLE) {
            this.layoutChooseContact.hide();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Handle deep links when app is already running
        setIntent(intent);
        handleDeepLink();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (OtherUtils.checkPer(this) || !OtherUtils.checkPermission(this)) {
            startActivity(new Intent(this, ActivityRequestPermission.class));
            finish();
        }

        String currentToken = sharedPreferences.getString("auth_token", null);
        String currentEmail = sharedPreferences.getString("user_email", null);

        if (currentToken == null || currentEmail == null || currentEmail.trim().isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        if (!currentToken.equals(token) || !currentEmail.equals(email)) {
            token = currentToken;
            email = currentEmail;
//            isAccessChecked = false; // Force recheck
            Log.d(TAG, "Session credentials changed, forcing access recheck");
        }

        if (!isAccessChecked) {
            performAccessCheck();
        } else if (hasAccess) {
            Log.d(TAG, "Access already verified, skipping check");
        } else {
            // Access was denied before, check again
            performAccessCheck();
        }
    }

    private void performAccessCheck() {
        Log.d(TAG, "Performing access check for: " + email);

        AccessManager.checkAccess(this, email, new AccessManager.AccessCallback() {
            @Override
            public void onAccessGranted() {
                isAccessChecked = true;
                hasAccess = true;
                Log.d(TAG, "Access granted - normal flow");
            }

            @Override
            public void onAccessDenied(String reason) {
                isAccessChecked = true;
                hasAccess = false;
                Log.d(TAG, "Access denied: " + reason);

                Intent intent = new Intent(ActivityHome.this, ActivityPaywall.class);
                intent.putExtra("ENTRY_SOURCE", reason);

                if ("TRIAL_CONSUMED".equals(reason)) {
                    intent.putExtra("DISABLE_BACK", true);
                }

                startActivity(intent);
            }
        });
    }

    private void handleBillNavigation() {

        boolean openBill = getIntent().getBooleanExtra("OPEN_BILL", false);
        Log.d("BillFlow", "OPEN_BILL=" + openBill + " BILL_ID=" + getIntent().getLongExtra("BILL_ID", 0));
        if (!openBill) {
            return;
        }
        long billId = getIntent().getLongExtra("BILL_ID", 0);
        EventsFragment.billUrl = MyConst.WEB_BASE_URL + "bills/" + billId;

        Log.d("BillFlow", "billUrl set to " + EventsFragment.billUrl);
        viewTabMode.setTabDefault(2);
    }


    @Override
    public void onPause() {
        super.onPause();

    }

    public void showQrFragment() {
        if (fragmentScan == null) {
            fragmentScan = new ScanFrag();
            fragmentScan.setModeSwitchListener(() -> {
                showBillCaptureFragment();
            });
        }
        MyShare.putScannerMode(this, false);
        showFragment(fragmentScan, false);
    }

    public void showBillCaptureFragment() {
        if (billCaptureFragment == null) {
            billCaptureFragment = new BillCaptureFragment();
            billCaptureFragment.setModeSwitchListener(() -> {
                showQrFragment();
            });
        }
        MyShare.putScannerMode(this, true);
        showFragment(billCaptureFragment, false);
    }

    @Override

    protected void onDestroy() {
        super.onDestroy();
    }
}
