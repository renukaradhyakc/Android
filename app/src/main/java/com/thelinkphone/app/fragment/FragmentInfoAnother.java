package com.thelinkphone.app.fragment;

import android.annotation.SuppressLint;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.LayoutChooseSimInfo;
import com.thelinkphone.app.custom.LayoutShowRecent;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.custom.ViewItemInfo;
import com.thelinkphone.app.dialog.DialogNotification;
import com.thelinkphone.app.dialog.DialogResult;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemRecent;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.utils.ActionUtils;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ReadContact;
import com.thelinkphone.app.utils.SimUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;


public class FragmentInfoAnother extends Fragment {
    private ContactResult contactResult;
    private ItemRecentGroup itemRecentGroup;
    private ViewInfoAnother viewInfoAnother;
    private boolean missedOnly;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() { 
        @Override 
        public final void onActivityResult(Object obj) {
            FragmentInfoAnother.this.m136x282fd38e((ActivityResult) obj);
        }
    });
    private final ActivityResultLauncher<Intent> lPer = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() { 
        @Override 
        public final void onActivityResult(Object obj) {
            FragmentInfoAnother.this.m137x75ef4b8f((ActivityResult) obj);
        }
    });

    public void setContactResult(ContactResult contactResult) {
        this.contactResult = contactResult;
    }

    public static FragmentInfoAnother newInstance(ItemRecentGroup itemRecentGroup, boolean missedOnly) {
        FragmentInfoAnother fragmentInfoAnother = new FragmentInfoAnother();
        Bundle bundle = new Bundle();
        bundle.putString("dataG", new Gson().toJson(itemRecentGroup));
        bundle.putBoolean("missedOnly", missedOnly);
        fragmentInfoAnother.setArguments(bundle);
        return fragmentInfoAnother;
    }

    @Override 
    public void onCreate(Bundle bundle) {
        Log.d("INFO_DEBUG", "onCreate START");
        String string;
        super.onCreate(bundle);
//        if (getArguments() == null || (string = getArguments().getString("dataG")) == null || string.isEmpty()) {
//            return;
//        }

        if (getArguments() == null) {
            Log.d("INFO_DEBUG", "getArguments NULL");
            return;
        }

        string = getArguments().getString("dataG");

        Log.d("INFO_DEBUG", "json length = " + (string == null ? "null" : string.length()));

        if (string == null || string.isEmpty()) {
            Log.d("INFO_DEBUG", "json empty");
            return;
        }

        this.missedOnly = getArguments().getBoolean("missedOnly", false);
        this.itemRecentGroup = (ItemRecentGroup) new Gson().fromJson(string, new TypeToken<ItemRecentGroup>() { 
        }.getType());
        Log.d("INFO_DEBUG", "onCreate END");
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        Log.d("INFO_DEBUG", "onCreateView START");
        ViewInfoAnother viewInfoAnother = new ViewInfoAnother(layoutInflater.getContext());
        Log.d("INFO_DEBUG", "ViewInfoAnother created");
        this.viewInfoAnother = viewInfoAnother;
        Log.d("INFO_DEBUG", "onCreateView END");
        return viewInfoAnother;
    }


    public void onBack() {
        try {
            ContactResult contactResult = this.contactResult;
            if (contactResult != null) {
                contactResult.onBack();
            }
        } catch (IllegalStateException unused) {
        }
    }



    public  void m136x282fd38e(ActivityResult activityResult) {
        if (getContext() == null || activityResult.getResultCode() != -1 || activityResult.getData() == null || activityResult.getData().getData() == null) {
            return;
        }
        Cursor query = getContext().getContentResolver().query(activityResult.getData().getData(), null, null, null, null);
        if (query != null && query.moveToFirst()) {
            @SuppressLint("Range") ItemContact contact = ReadContact.getContact(getContext(), query.getString(query.getColumnIndex("_id")));
            if (contact != null) {
                if (getActivity() instanceof ActivityHome) {
                    ((ActivityHome) getActivity()).addNewContact(contact);
                }
                ContactResult contactResult = this.contactResult;
                if (contactResult != null) {
                    contactResult.onAddNewContact(this.itemRecentGroup, contact);
                }
            }
        }
        if (query != null) {
            query.close();
        }
        new Handler().postDelayed(new Runnable() { 
            @Override 
            public final void run() {
                FragmentInfoAnother.this.onBack();
            }
        }, 300L);
    }



    public  void m137x75ef4b8f(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
            this.viewInfoAnother.showDialogBlock();
        }
    }



    public class ViewInfoAnother extends RelativeLayout {
        private final ArrayList<ItemContact> arrBlock;
        private final ArrayList<ItemSimInfo> arrSim;
        private final int posSim;
        private final boolean theme;
        private final TextW tvBlock;





        public ViewInfoAnother(Context context) {
            super(context);
            Log.d("INFO_DEBUG", "Constructor START");

            Log.d("INFO_DEBUG", "number=" + FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number);
            Log.d("INFO_DEBUG", "country=" + FragmentInfoAnother.this.itemRecentGroup.country);
            Log.d("INFO_DEBUG", "time=" + FragmentInfoAnother.this.itemRecentGroup.time);
            Log.d("INFO_DEBUG", "recentCount=" + FragmentInfoAnother.this.itemRecentGroup.arrRecent.size());
            Log.d("INFO_DEBUG", "simId=" + FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).simId);


            TextW tvBlock1;
            TextW textW;
            TextW textW2;
            Iterator<ItemRecent> it;
            Log.d("INFO_DEBUG", "before sim lookup");
            ArrayList<ItemSimInfo> availableSIMCardLabels = SimUtils.getAvailableSIMCardLabels(context);
            Log.d("INFO_DEBUG", "SIM count = " + availableSIMCardLabels.size());
            this.arrSim = availableSIMCardLabels;
            this.posSim = MyShare.getPosSim(context);
            int widthScreen = OtherUtils.getWidthScreen(context);
            int i = widthScreen / 25;
            int i2 = (widthScreen * 18) / 100;
            this.theme = MyShare.getTheme(context);
            this.arrBlock = MyShare.getArrBlock(getContext());
            ImageView imageView = new ImageView(context);
            imageView.setId(100);
            imageView.setImageResource(R.drawable.ic_back);
            imageView.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewInfoAnother.this.m138x9f01e15a(view);
                }
            });
            LayoutParams layoutParams = new LayoutParams((int) (i * 1.5f), i * 3);
            int i3 = i / 2;
            layoutParams.setMargins(i3, MyShare.getSizeNotification(context), 0, 0);
            addView(imageView, layoutParams);
            TextW textW3 = new TextW(context);
            textW3.setText(R.string.recents);
            textW3.setGravity(16);
            textW3.setupText(400, 4.2f);
            textW3.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewInfoAnother.this.m139x7f7b375b(view);
                }
            });
            textW3.setTextColor(Color.parseColor("#007AFF"));
            LayoutParams layoutParams2 = new LayoutParams(-2, -1);
            layoutParams2.addRule(6, imageView.getId());
            layoutParams2.addRule(8, imageView.getId());
            layoutParams2.addRule(17, imageView.getId());
            addView(textW3, layoutParams2);
            ScrollView scrollView = new ScrollView(context);
            scrollView.setFillViewport(true);
            LayoutParams layoutParams3 = new LayoutParams(-1, -1);
            layoutParams3.addRule(14);
            layoutParams3.addRule(3, imageView.getId());
            addView(scrollView, layoutParams3);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(1);
            scrollView.addView(linearLayout, -1, -2);
            ImageView imageView2 = new ImageView(context);
            imageView2.setImageResource(R.drawable.ic_no_contact);
            linearLayout.addView(imageView2, i2, i2);
            TextW textW4 = new TextW(context);
            textW4.setupText(400, 7.0f);
            textW4.setSingleLine();
            textW4.setEllipsize(TextUtils.TruncateAt.END);
            textW4.setPadding(i, i / 8, i, 0);
            String str = FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number;
            Log.d("INFO_DEBUG", "number = " + str);
            textW4.setText(str == null ? "" : str);
            linearLayout.addView(textW4, -2, -2);
            textW4.setOnLongClickListener(new OnLongClickListener() { 
                @Override 
                public final boolean onLongClick(View view) {
                    return ViewInfoAnother.this.m140x5ff48d5c(view);
                }
            });
            Log.d("INFO_DEBUG", "Entering SIM block check");
            Log.d("INFO_DEBUG", "SIM count check = " + availableSIMCardLabels.size());
            if (availableSIMCardLabels.size() > 1) {
                Log.d("INFO_DEBUG", "Inside SIM block");
                Log.d("INFO_DEBUG", "MULTI SIM PATH");
                int i4 = 0;
                while (true) {
                    if (i4 >= this.arrSim.size()) {
                        i4 = -1;
                        break;
                    }
                    String id = this.arrSim.get(i4).handle.getId();
                    String str2 = FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).simId;
                    if (str2 != null && str2.equals(id)) {
                        break;
                    }
                    i4++;
                }
                if (i4 != -1) {
                    LinearLayout linearLayout2 = new LinearLayout(context);
                    linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout2.setGravity(16);
                    linearLayout.addView(linearLayout2, -2, -2);
                    textW = new TextW(context);
                    textW.setupText(400, 3.3f);
                    linearLayout2.addView(textW, -2, -2);
                    LayoutChooseSimInfo layoutChooseSimInfo = new LayoutChooseSimInfo(context);
                    layoutChooseSimInfo.init(this.theme);
                    layoutChooseSimInfo.setSim(i4);
                    layoutChooseSimInfo.hideViewNext();
                    linearLayout2.addView(layoutChooseSimInfo, -2, -2);
                    textW.setText("  " + FragmentInfoAnother.this.getString(R.string.last_used) + ":");
                    Log.d("INFO_DEBUG", "country value = " + FragmentInfoAnother.this.itemRecentGroup.country);
                    if (FragmentInfoAnother.this.itemRecentGroup.country != null || FragmentInfoAnother.this.itemRecentGroup.country.isEmpty()) {
                        textW2 = null;
                    } else {
                        textW2 = new TextW(context);
                        textW2.setupText(400, 3.3f);
                        textW2.setText(FragmentInfoAnother.this.itemRecentGroup.country);
                        linearLayout.addView(textW2, -2, -2);
                    }
                    LinearLayout linearLayout3 = new LinearLayout(context);
                    linearLayout3.setOrientation(LinearLayout.HORIZONTAL);
                    linearLayout3.setGravity(1);
                    LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
                    layoutParams4.setMargins(0, i, 0, 0);
                    linearLayout.addView(linearLayout3, layoutParams4);
                    int i5 = (widthScreen * 78) / 360;
                    int i6 = (widthScreen * 5) / 360;
                    ViewItemInfo viewItemInfo = new ViewItemInfo(context);
                    viewItemInfo.setOnClickListener(new OnClickListener() { 
                        @Override 
                        public final void onClick(View view) {
                            ViewInfoAnother.this.m141x406de35d(view);
                        }
                    });
                    TextW textW5 = textW;
                    TextW textW6 = textW2;
                    viewItemInfo.setInfo(R.drawable.ic_message, R.string.message, true, this.theme);
                    LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(i5, -2);
                    layoutParams5.setMargins(i6, 0, i6, 0);
                    linearLayout3.addView(viewItemInfo, layoutParams5);
                    ViewItemInfo viewItemInfo2 = new ViewItemInfo(context);
                    viewItemInfo2.setOnClickListener(new OnClickListener() { 
                        @Override 
                        public final void onClick(View view) {
                            ViewInfoAnother.this.m142x20e7395e(view);
                        }
                    });
                    viewItemInfo2.setInfo(R.drawable.ic_call_info, R.string.call, true, this.theme);
                    LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(i5, -2);
                    layoutParams6.setMargins(i6, 0, i6, 0);
                    linearLayout3.addView(viewItemInfo2, layoutParams6);
                    ViewItemInfo viewItemInfo3 = new ViewItemInfo(context);
                    viewItemInfo3.setInfo(R.drawable.ic_facetime, R.string.facetime, false, this.theme);
                    LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(i5, -2);
                    layoutParams7.setMargins(i6, 0, i6, 0);
                    linearLayout3.addView(viewItemInfo3, layoutParams7);
                    ViewItemInfo viewItemInfo4 = new ViewItemInfo(context);
                    viewItemInfo4.setInfo(R.drawable.ic_mail, R.string.mail, false, this.theme);
                    LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(i5, -2);
                    layoutParams8.setMargins(i6, 0, i6, 0);
                    linearLayout3.addView(viewItemInfo4, layoutParams8);
                    LinearLayout linearLayout4 = new LinearLayout(context);
                    linearLayout4.setPadding(i3, i3, i3, i);
                    linearLayout4.setOrientation(LinearLayout.VERTICAL);
                    int i7 = (widthScreen * 342) / 360;
                    LinearLayout.LayoutParams layoutParams9 = new LinearLayout.LayoutParams(i7, -2);
                    layoutParams9.setMargins(0, i, 0, 0);
                    linearLayout.addView(linearLayout4, layoutParams9);
//                    TextW textW7 = new TextW(context);
//                    textW7.setPadding(i3, i3, i3, 0);
//                    textW7.setupText(400, 3.5f);
//                    textW7.setText(OtherUtils.longToTimeTitle(getContext(), FragmentInfoAnother.this.itemRecentGroup.time));
//                    linearLayout4.addView(textW7, -1, -2);
//                    it = FragmentInfoAnother.this.itemRecentGroup.arrRecent.iterator();
//                    while (it.hasNext()) {
//                        LayoutShowRecent layoutShowRecent = new LayoutShowRecent(context);
//                        layoutShowRecent.setRecent(it.next(), this.theme);
//                        linearLayout4.addView(layoutShowRecent, -1, -2);
//                    }
                    addRecentsGroupedByDay(linearLayout4, context, FragmentInfoAnother.this.itemRecentGroup.arrRecent, this.theme);
                    LinearLayout linearLayout5 = new LinearLayout(context);
                    linearLayout5.setOrientation(LinearLayout.VERTICAL);
                    LinearLayout.LayoutParams layoutParams10 = new LinearLayout.LayoutParams(i7, -2);
                    layoutParams10.setMargins(0, i, 0, 0);
                    linearLayout.addView(linearLayout5, layoutParams10);
                    TextW textW8 = new TextW(context);
                    textW8.setText(R.string.share_contact);
                    textW8.setupText(400, 3.9f);
                    textW8.setTextColor(Color.parseColor("#007AFF"));
                    textW8.setPadding(i, i, i, i);
                    textW8.setOnClickListener(new OnClickListener() { 
                        @Override 
                        public final void onClick(View view) {
                            ViewInfoAnother.this.m143x1608f5f(view);
                        }
                    });
                    linearLayout5.addView(textW8, -1, -2);
                    View view = new View(context);
                    LinearLayout.LayoutParams layoutParams11 = new LinearLayout.LayoutParams(-1, 1);
                    layoutParams11.setMargins(i, 0, 0, 0);
                    linearLayout5.addView(view, layoutParams11);
                    TextW textW9 = new TextW(context);
                    textW9.setText(R.string.add_new);
                    textW9.setupText(400, 3.9f);
                    textW9.setTextColor(Color.parseColor("#007AFF"));
                    textW9.setPadding(i, i, i, i);
                    textW9.setOnClickListener(new OnClickListener() { 
                        @Override 
                        public final void onClick(View view2) {
                            ViewInfoAnother.this.m144xe1d9e560(view2);
                        }
                    });
                    linearLayout5.addView(textW9, -1, -2);
                    TextW textW10 = new TextW(context);
                    tvBlock1 = textW10;
                    textW10.setupText(400, 3.9f);
                    textW10.setPadding(i, i, i, i);
                    textW10.setOnClickListener(new OnClickListener() { 
                        @Override 
                        public final void onClick(View view2) {
                            ViewInfoAnother.this.m145xc2533b61(view2);
                        }
                    });
                    LinearLayout.LayoutParams layoutParams12 = new LinearLayout.LayoutParams(i7, -2);
                    layoutParams12.setMargins(0, i, 0, 0);
                    linearLayout.addView(textW10, layoutParams12);
                    linearLayout.addView(new View(context), -1, widthScreen / 10);
                    if (this.theme) {
                        setBackgroundColor(Color.parseColor("#F2F2F7"));
                        textW4.setTextColor(-16777216);
                        if (textW6 != null) {
                            textW6.setTextColor(Color.parseColor("#8A8A8E"));
                        }
                        if (textW5 != null) {
                            textW5.setTextColor(Color.parseColor("#8A8A8E"));
                        }
                        float f = (widthScreen * 3.0f) / 100.0f;
//                        textW7.setTextColor(-16777216);
                        linearLayout5.setBackground(OtherUtils.bgIcon(-1, f));
                        view.setBackgroundColor(Color.parseColor("#dedede"));
                        textW10.setBackground(OtherUtils.bgIcon(-1, f));
                    } else {
                        setBackgroundColor(Color.parseColor("#2C2C2C"));
                        textW4.setTextColor(-1);
                        if (textW6 != null) {
                            textW6.setTextColor(Color.parseColor("#F5F5F5"));
                        }
                        if (textW5 != null) {
                            textW5.setTextColor(Color.parseColor("#F5F5F5"));
                        }
                        float f2 = (widthScreen * 3.0f) / 100.0f;
//                        textW7.setTextColor(-1);
                        linearLayout5.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2));
                        view.setBackgroundColor(Color.parseColor("#5c5c5c"));
                        textW10.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2));
                    }
                    Log.d("INFO_DEBUG", "Calling updateBlock()");
                    updateBlock();
                }
            }
            Log.d("INFO_DEBUG", "AFTER MULTI SIM BLOCK");
            textW = null;
            Log.d("INFO_DEBUG", "AFTER textW = null");
            if (FragmentInfoAnother.this.itemRecentGroup.country != null) {
            }
            textW2 = null;
            Log.d("INFO_DEBUG", "AFTER textW2 = null");
            Log.d("INFO_DEBUG", "BEFORE linearLayout32");
            LinearLayout linearLayout32 = new LinearLayout(context);
            Log.d("INFO_DEBUG", "AFTER linearLayout32");
            linearLayout32.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout32.setGravity(1);
            LinearLayout.LayoutParams layoutParams42 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams42.setMargins(0, i, 0, 0);
            linearLayout.addView(linearLayout32, layoutParams42);
            int i52 = (widthScreen * 78) / 360;
            int i62 = (widthScreen * 5) / 360;
            Log.d("INFO_DEBUG", "BEFORE viewItemInfo5");
            ViewItemInfo viewItemInfo5 = new ViewItemInfo(context);
            Log.d("INFO_DEBUG", "AFTER viewItemInfo5 constructor");
            Log.d("INFO_DEBUG", "BEFORE setInfo MESSAGE");
            viewItemInfo5.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    ViewInfoAnother.this.m141x406de35d(view2);
                }
            });
            Log.d("INFO_DEBUG", "After setInfo MESSAGE");
            TextW textW52 = textW;
            TextW textW62 = textW2;
            viewItemInfo5.setInfo(R.drawable.ic_message, R.string.message, true, this.theme);
            Log.d("INFO_DEBUG", "AFTER viewItemInfo5.setInfo");
            Log.d("INFO_DEBUG", "BEFORE linearLayout52");
            LinearLayout.LayoutParams layoutParams52 = new LinearLayout.LayoutParams(i52, -2);
            layoutParams52.setMargins(i62, 0, i62, 0);
            Log.d("INFO_DEBUG", "AFTER linearLayout32");
            linearLayout32.addView(viewItemInfo5, layoutParams52);
            Log.d("INFO_DEBUG", "BEFORE viewItemInfo22");
            ViewItemInfo viewItemInfo22 = new ViewItemInfo(context);
            Log.d("INFO_DEBUG", "AFTER viewItemInfo5 constructor");
            viewItemInfo22.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    ViewInfoAnother.this.m142x20e7395e(view2);
                }
            });
            viewItemInfo22.setInfo(R.drawable.ic_call_info, R.string.call, true, this.theme);
            LinearLayout.LayoutParams layoutParams62 = new LinearLayout.LayoutParams(i52, -2);
            layoutParams62.setMargins(i62, 0, i62, 0);
            linearLayout32.addView(viewItemInfo22, layoutParams62);
            ViewItemInfo viewItemInfo32 = new ViewItemInfo(context);
            viewItemInfo32.setInfo(R.drawable.ic_facetime, R.string.facetime, false, this.theme);
            LinearLayout.LayoutParams layoutParams72 = new LinearLayout.LayoutParams(i52, -2);
            layoutParams72.setMargins(i62, 0, i62, 0);
            linearLayout32.addView(viewItemInfo32, layoutParams72);
            Log.d("INFO_DEBUG", "AFTER viewItemInfo42");
            ViewItemInfo viewItemInfo42 = new ViewItemInfo(context);
            viewItemInfo42.setInfo(R.drawable.ic_mail, R.string.mail, false, this.theme);
            Log.d("INFO_DEBUG", "BEFORE layoutParams82");
            LinearLayout.LayoutParams layoutParams82 = new LinearLayout.LayoutParams(i52, -2);
            layoutParams82.setMargins(i62, 0, i62, 0);
            linearLayout32.addView(viewItemInfo42, layoutParams82);
            Log.d("INFO_DEBUG", "BEFORE linearLayout42");
            LinearLayout linearLayout42 = new LinearLayout(context);
            linearLayout42.setPadding(i3, i3, i3, i);
            linearLayout42.setOrientation(LinearLayout.VERTICAL);
            int i72 = (widthScreen * 342) / 360;
            Log.d("INFO_DEBUG", "BEFORE layoutParams92");
            LinearLayout.LayoutParams layoutParams92 = new LinearLayout.LayoutParams(i72, -2);
            layoutParams92.setMargins(0, i, 0, 0);
            linearLayout.addView(linearLayout42, layoutParams92);
//            TextW textW72 = new TextW(context);
//            textW72.setPadding(i3, i3, i3, 0);
//            textW72.setupText(400, 3.5f);
//            textW72.setText(OtherUtils.longToTimeTitle(getContext(), FragmentInfoAnother.this.itemRecentGroup.time));
//            linearLayout42.addView(textW72, -1, -2);
//            it = FragmentInfoAnother.this.itemRecentGroup.arrRecent.iterator();
            Log.d("INFO_DEBUG", "Debug1");
//            while (it.hasNext()) {
//                LayoutShowRecent layoutShowRecent = new LayoutShowRecent(context);
//                layoutShowRecent.setRecent(it.next(), this.theme);
//                linearLayout42.addView(layoutShowRecent, -1, -2);
//            }
            addRecentsGroupedByDay(linearLayout42, context, FragmentInfoAnother.this.itemRecentGroup.arrRecent, this.theme);
            Log.d("INFO_DEBUG", "Debug2");
            LinearLayout linearLayout52 = new LinearLayout(context);
            linearLayout52.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams102 = new LinearLayout.LayoutParams(i72, -2);
            layoutParams102.setMargins(0, i, 0, 0);
            linearLayout.addView(linearLayout52, layoutParams102);
            TextW textW82 = new TextW(context);
            textW82.setText(R.string.share_contact);
            textW82.setupText(400, 3.9f);
            textW82.setTextColor(Color.parseColor("#007AFF"));
            textW82.setPadding(i, i, i, i);
            textW82.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    ViewInfoAnother.this.m143x1608f5f(view2);
                }
            });
            linearLayout52.addView(textW82, -1, -2);
            View view2 = new View(context);
            LinearLayout.LayoutParams layoutParams112 = new LinearLayout.LayoutParams(-1, 1);
            layoutParams112.setMargins(i, 0, 0, 0);
            linearLayout52.addView(view2, layoutParams112);
            TextW textW92 = new TextW(context);
            textW92.setText(R.string.add_new);
            textW92.setupText(400, 3.9f);
            textW92.setTextColor(Color.parseColor("#007AFF"));
            textW92.setPadding(i, i, i, i);
            textW92.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view22) {
                    ViewInfoAnother.this.m144xe1d9e560(view22);
                }
            });
            linearLayout52.addView(textW92, -1, -2);
            TextW textW102 = new TextW(context);
            tvBlock1 = textW102;
            this.tvBlock = tvBlock1;
            textW102.setupText(400, 3.9f);
            textW102.setPadding(i, i, i, i);
            textW102.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view22) {
                    ViewInfoAnother.this.m145xc2533b61(view22);
                }
            });
            LinearLayout.LayoutParams layoutParams122 = new LinearLayout.LayoutParams(i72, -2);
            layoutParams122.setMargins(0, i, 0, 0);
            linearLayout.addView(textW102, layoutParams122);
            linearLayout.addView(new View(context), -1, widthScreen / 10);
            float radius2 = (widthScreen * 3.0f) / 100.0f;
            Log.d("THEME_DEBUG before if", "theme = " + this.theme);
            Log.d("THEME_DEBUG before if", "MyShare.getTheme = " + MyShare.getTheme(context));
            if (this.theme) {
                setBackgroundColor(Color.parseColor("#F2F2F7"));
                textW4.setTextColor(-16777216);
//                textW72.setTextColor(-16777216);
                linearLayout52.setBackground(OtherUtils.bgIcon(-1, radius2));
                view2.setBackgroundColor(Color.parseColor("#dedede"));
                textW102.setBackground(OtherUtils.bgIcon(-1, radius2));
            } else {
                setBackgroundColor(Color.parseColor("#2C2C2C"));
                textW4.setTextColor(-1);
//                textW72.setTextColor(-1);
                linearLayout52.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), radius2));
                view2.setBackgroundColor(Color.parseColor("#5c5c5c"));
                textW102.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), radius2));
            }
            updateBlock();
            Log.d("INFO_DEBUG", "End of Constructor");
        }



        public  void m138x9f01e15a(View view) {
            FragmentInfoAnother.this.onBack();
        }



        public  void m139x7f7b375b(View view) {
            FragmentInfoAnother.this.onBack();
        }



        public  boolean m140x5ff48d5c(View view) {
            OtherUtils.copyToClip(getContext(), FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number);
            return true;
        }



        public  void m141x406de35d(View view) {
            OtherUtils.sendMessage(getContext(), FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number);
        }



        public  void m142x20e7395e(View view) {
            onCallClick();
        }



        public  void m143x1608f5f(View view) {
            ActionUtils.shareText(getContext(), FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number);
        }



        public  void m144xe1d9e560(View view) {
            ActionUtils.onNewContact(FragmentInfoAnother.this.launcher, FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number);
        }



        public  void m145xc2533b61(View view) {
            onBlock();
        }

        public void onCallClick() {
            PhoneAccountHandle phoneAccountHandle;
            String str = FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number;
            if (str == null || str.isEmpty() || this.arrSim.size() == 0) {
                return;
            }
            if (this.posSim < this.arrSim.size()) {
                phoneAccountHandle = this.arrSim.get(this.posSim).handle;
            } else {
                phoneAccountHandle = this.arrSim.get(0).handle;
            }
            OtherUtils.call(getContext(), str, phoneAccountHandle);
        }

        private void onBlock() {
            boolean z = false;
            String str = FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number;
            Iterator<ItemContact> it = this.arrBlock.iterator();
            while (true) {
                if (!it.hasNext()) {
                    z = true;
                    break;
                }
                ItemContact next = it.next();
                if (str != null && !next.getArrPhone().isEmpty() && next.getArrPhone().get(0).getNumber() != null && PhoneNumberUtils.compare(str, next.getArrPhone().get(0).getNumber())) {
                    this.arrBlock.remove(next);
                    MyShare.putBlockNumber(getContext(), this.arrBlock);
                    updateBlock();
                    break;
                }
            }
            if (z) {
                if (Build.VERSION.SDK_INT >= 29) {
                    RoleManager roleManager = (RoleManager) getContext().getSystemService(Context.ROLE_SERVICE);
                    if (roleManager.isRoleHeld("android.app.role.CALL_SCREENING")) {
                        showDialogBlock();
                        return;
                    }
                    FragmentInfoAnother.this.lPer.launch(roleManager.createRequestRoleIntent("android.app.role.CALL_SCREENING"));
                    return;
                }
                showDialogBlock();
            }
        }


        public void showDialogBlock() {
            new DialogNotification(getContext(), this.theme, new DialogResult() { 
                @Override 
                public final void onActionClick() {
                    ViewInfoAnother.this.m146xbf660a2a();
                }
            }).show();
        }



        public  void m146xbf660a2a() {
            this.arrBlock.add(new ItemContact(FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number));
            MyShare.putBlockNumber(getContext(), this.arrBlock);
            updateBlock();
        }

        private void updateBlock() {
            Log.d("INFO_DEBUG", "updateBlock START");
            boolean z = false;
            String str = FragmentInfoAnother.this.itemRecentGroup.arrRecent.get(0).number;
            Iterator<ItemContact> it = this.arrBlock.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ItemContact next = it.next();
                if (str != null && !next.getArrPhone().isEmpty() && next.getArrPhone().get(0).getNumber() != null && PhoneNumberUtils.compare(str, next.getArrPhone().get(0).getNumber())) {
                    z = true;
                    break;
                }
            }
            if (z) {
                this.tvBlock.setTextColor(Color.parseColor("#007AFF"));
                this.tvBlock.setText(R.string.unblock_this_caller);
                return;
            }
            this.tvBlock.setTextColor(Color.parseColor("#FF2828"));
            this.tvBlock.setText(R.string.block_this_caller);

            Log.d("INFO_DEBUG", "updateBlock END");
        }

        private void addRecentsGroupedByDay(LinearLayout container, Context context, ArrayList<ItemRecent> arrRecent, boolean theme) {
            Calendar cal = Calendar.getInstance();
            int lastYear = -1;
            int lastDayOfYear = -1;
            int widthScreen = OtherUtils.getWidthScreen(context);
            int pad = widthScreen / 25;
            float radius = (widthScreen * 3.0f) / 100.0f;
            Log.d("THEME_DEBUG inside method", "theme = " + this.theme);
            Log.d("THEME_DEBUG inside method", "MyShare.getTheme = " + MyShare.getTheme(context));
            int bgColor = theme ? -1 : Color.parseColor("#424141");
            LinearLayout currentDayBlock = null;

            for (ItemRecent recent : arrRecent) {
                if (missedOnly && recent.type != 3) continue;

                cal.setTimeInMillis(recent.time);
                int year = cal.get(java.util.Calendar.YEAR);
                int dayOfYear = cal.get(java.util.Calendar.DAY_OF_YEAR);

                if (year != lastYear || dayOfYear != lastDayOfYear) {
                    currentDayBlock = new LinearLayout(context);
                    currentDayBlock.setOrientation(LinearLayout.VERTICAL);
                    currentDayBlock.setBackground(OtherUtils.bgIcon(bgColor, radius));
                    currentDayBlock.setPadding(pad, pad / 2, pad, pad / 2);

                    LinearLayout.LayoutParams blockParams = new LinearLayout.LayoutParams(-1, -2);
                    blockParams.setMargins(0, pad, 0, 0);
                    container.addView(currentDayBlock, blockParams);

                    TextW header = new TextW(context);
                    header.setupText(400, 3.3f);
                    header.setText(OtherUtils.longToTimeTitle(context, recent.time));
                    header.setPadding(0, pad / 2, 0, pad / 2);
                    header.setTextColor(theme ? Color.parseColor("#8A8A8E") : Color.parseColor("#F5F5F5"));
                    currentDayBlock.addView(header, -1, -2);

                    lastYear = year;
                    lastDayOfYear = dayOfYear;
                }

                LayoutShowRecent layoutShowRecent = new LayoutShowRecent(context);
                layoutShowRecent.setRecent(recent, theme);
                currentDayBlock.addView(layoutShowRecent, -1, -2);
            }
        }
    }
}
