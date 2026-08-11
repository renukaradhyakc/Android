package com.thelinkphone.app.fragment;

import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.AvatarPeople;
import com.thelinkphone.app.custom.EditW;
import com.thelinkphone.app.custom.LayoutChooseSimInfo;
import com.thelinkphone.app.custom.LayoutListSim;
import com.thelinkphone.app.custom.LayoutSchedulePreview;
import com.thelinkphone.app.custom.LayoutShowRecent;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.custom.ViewFragmentUnifiedSchedule;
import com.thelinkphone.app.custom.ViewItemCallalinkLink;
import com.thelinkphone.app.custom.ViewItemInfo;
import com.thelinkphone.app.custom.ViewItemNumber;
import com.thelinkphone.app.custom.ViewQrBadgeOverlay;
import com.thelinkphone.app.dialog.DialogAddFav;
import com.thelinkphone.app.dialog.DialogChooseNumber;
import com.thelinkphone.app.dialog.DialogNotification;
import com.thelinkphone.app.dialog.DialogResult;
import com.thelinkphone.app.dialog.DialogShowQr;
import com.thelinkphone.app.dialog.DialogShowSim;
import com.thelinkphone.app.dialog.FavResult;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemFavorites;
import com.thelinkphone.app.item.ItemNote;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.item.ItemRecent;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.item.ItemSchedulePreview;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.mapper.ScheduleMapper;
import com.thelinkphone.app.model.GenericResponse;
import com.thelinkphone.app.model.PhoneScheduleResponse;
import com.thelinkphone.app.repository.PhoneScheduleRepository;
import com.thelinkphone.app.utils.ActionUtils;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.CallBlockReasonResolver;
import com.thelinkphone.app.utils.CallDisplayMode;
import com.thelinkphone.app.utils.CallLogGroupHelper;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ReadContact;
import com.thelinkphone.app.utils.SimUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FragmentInfo extends Fragment {
    private ContactResult contactResult;
    private ItemContact itemContact;
    private ItemRecentGroup itemRecentGroup;
    private int title;
    private LayoutSchedulePreview schedulePreview;
    private ViewInfo viewInfo;
    private int displayMode;
    private PhoneScheduleRepository scheduleRepository;
    private String authToken;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() { 
        @Override 
        public final void onActivityResult(Object obj) {
            FragmentInfo.this.m118xc805e5b5((ActivityResult) obj);
        }
    });
    private final ActivityResultLauncher<Intent> lPer = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() { 
        @Override 
        public final void onActivityResult(Object obj) {
            FragmentInfo.this.m119x54f2fcd4((ActivityResult) obj);
        }
    });

    public void setContactResult(ContactResult contactResult) {
        this.contactResult = contactResult;
    }

    public static FragmentInfo newInstance(ItemContact itemContact, int i) {
        FragmentInfo fragmentInfo = new FragmentInfo();
        Bundle bundle = new Bundle();
        bundle.putString("dataf", new Gson().toJson(itemContact));
        bundle.putInt("title", i);
        fragmentInfo.setArguments(bundle);
        return fragmentInfo;
    }

    public static FragmentInfo newInstance(ItemContact itemContact, ItemRecentGroup itemRecentGroup, int i, int displayMode) {
        FragmentInfo fragmentInfo = new FragmentInfo();
        Bundle bundle = new Bundle();
        bundle.putString("dataf", new Gson().toJson(itemContact));
        bundle.putString("dataG", new Gson().toJson(itemRecentGroup));
        bundle.putInt("title", i);
        bundle.putInt("displayMode", displayMode);
        fragmentInfo.setArguments(bundle);
        return fragmentInfo;
    }

    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        if (getArguments() != null) {
            this.title = getArguments().getInt("title");
            String string = getArguments().getString("dataf");
            this.displayMode = getArguments().getInt("displayMode", CallDisplayMode.ALL);
            if (string != null && !string.isEmpty()) {
                this.itemContact = (ItemContact) new Gson().fromJson(string, new TypeToken<ItemContact>() { 
                }.getType());
            }
            String string2 = getArguments().getString("dataG");
            if (string2 != null && !string2.isEmpty()) {
                this.itemRecentGroup = (ItemRecentGroup) new Gson().fromJson(string2, new TypeToken<ItemRecentGroup>() { 
                }.getType());
            }
        }
        if (this.itemContact == null) {
            this.itemContact = new ItemContact("", getString(R.string.unknown), "", new ArrayList());
        }
        SharedPreferences prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        authToken = prefs.getString("auth_token", null);
        if (authToken != null) {
            scheduleRepository = new PhoneScheduleRepository(
                    ApiClient.getClient().create(ApiService.class), "Bearer " + authToken);
        }
    }

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ViewInfo viewInfo = new ViewInfo(layoutInflater.getContext());
        this.viewInfo = viewInfo;
        return viewInfo;
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

    private void onDeleteContact() {
        if (getActivity() instanceof ActivityHome) {
            ((ActivityHome) getActivity()).removeContact(this.itemContact.getId());
        }
        ContactResult contactResult = this.contactResult;
        if (contactResult != null) {
            contactResult.onContactChange();
        }
        new Handler().postDelayed(new Runnable() { 
            @Override 
            public final void run() {
                FragmentInfo.this.onBack();
            }
        }, 300L);
    }



    public  void m118xc805e5b5(ActivityResult activityResult) {
        if (activityResult.getResultCode() == 3 && (activityResult.getData() == null || activityResult.getData().getData() == null)) {
            onDeleteContact();
        } else if (getContext() == null || activityResult.getResultCode() != -1 || activityResult.getData() == null || activityResult.getData().getData() == null) {
        } else {
            ItemContact contact = ReadContact.getContact(getContext(), this.itemContact.getId());
            if (contact == null) {
                onDeleteContact();
                return;
            }
            this.itemContact = contact;
            this.viewInfo.updateContact();
            if (getActivity() instanceof ActivityHome) {
                ((ActivityHome) getActivity()).updateContact(contact);
            }
            ContactResult contactResult = this.contactResult;
            if (contactResult != null) {
                contactResult.onContactChange();
            }
        }
    }



    public  void m119x54f2fcd4(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
            this.viewInfo.showDialogBlock();
        }
    }



    public class ViewInfo extends RelativeLayout implements ViewItemNumber.OnItemNumberClick {
        private ArrayList<ItemContact> arrBlock;
        private ArrayList<ItemFavorites> arrFav;
        private ArrayList<ItemNote> arrNote;
        private final ArrayList<ItemSimInfo> arrSim;
        private final ArrayList<ViewItemNumber> arrViewNumber;
        private final AvatarPeople av;
        private final EditW edtNote;
        private final Handler handler;
        private ItemNote itemNote;
        private final LinearLayout llNumber;
        private int posSim;
        private final Runnable runnable;
        private final boolean theme;
        private final TextW tvBlock;
        private final TextW tvName;
        private final ViewItemInfo viewItemSchedule;
        private CallBlockReasonResolver blockReasonResolver;

        public ViewInfo(Context context) {
            super(context);
            this.runnable = new Runnable() { 
                @Override 
                public void run() {
                    if (ViewInfo.this.getContext() != null) {
                        MyShare.putArrNote(ViewInfo.this.getContext(), ViewInfo.this.arrNote);
                    }
                }
            };
            ArrayList<ItemSimInfo> availableSIMCardLabels = SimUtils.getAvailableSIMCardLabels(context);
            this.arrSim = availableSIMCardLabels;
            this.posSim = MyShare.getPosSim(context);
            int widthScreen = OtherUtils.getWidthScreen(context);
            int i = widthScreen / 25;
            int i2 = (widthScreen * 18) / 100;
            boolean theme = MyShare.getTheme(context);
            this.theme = theme;
            this.arrViewNumber = new ArrayList<>();
            this.tvBlock = new TextW(context);
            this.edtNote = new EditW(context);
            this.handler = new Handler(new Handler.Callback() {
                @Override
                public final boolean handleMessage(Message message) {
                    return ViewInfo.this.m120xfc74ef22(message);
                }
            });
            int i3 = i / 2;
            int statusBarInset = widthScreen / 20;
            ImageView imageView = new ImageView(context);
            imageView.setId(View.generateViewId());
            imageView.setImageResource(R.drawable.ic_back);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewInfo.this.m121xd8366ae3(view);
                }
            });
            LayoutParams layoutParams = new LayoutParams((int) (i * 1.5f), (int) (i * 1.3f));
            layoutParams.setMargins(i3, statusBarInset, 0, 0);
            addView(imageView, layoutParams);
            TextW textW = new TextW(context);
            textW.setText(FragmentInfo.this.title);
            textW.setGravity(16);
            textW.setupText(400, 4.2f);
            textW.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewInfo.this.m123xb3f7e6a4(view);
                }
            });
            textW.setTextColor(Color.parseColor("#007AFF"));
            LayoutParams layoutParams2 = new LayoutParams(-2, -1);
            layoutParams2.addRule(6, imageView.getId());
            layoutParams2.addRule(8, imageView.getId());
            layoutParams2.addRule(17, imageView.getId());
            addView(textW, layoutParams2);
            if (FragmentInfo.this.itemRecentGroup == null) {
                TextW textW2 = new TextW(context);
                textW2.setText(R.string.edit);
                textW2.setGravity(16);
                textW2.setupText(400, 4.2f);
                textW2.setTextColor(Color.parseColor("#007AFF"));
                textW2.setPadding(i, 0, i, 0);
                textW2.setOnClickListener(new OnClickListener() { 
                    @Override 
                    public final void onClick(View view) {
                        ViewInfo.this.m124x8fb96265(view);
                    }
                });
                LayoutParams layoutParams3 = new LayoutParams(-2, -1);
                layoutParams3.addRule(6, imageView.getId());
                layoutParams3.addRule(8, imageView.getId());
                layoutParams3.addRule(21);
                addView(textW2, layoutParams3);
            }
            ScrollView scrollView = new ScrollView(context);
            scrollView.setFillViewport(true);
            LayoutParams layoutParams4 = new LayoutParams(-1, -1);
            layoutParams4.addRule(14);
            layoutParams4.addRule(3, imageView.getId());
            addView(scrollView, layoutParams4);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(1);
            scrollView.addView(linearLayout, -1, -2);
            AvatarPeople avatarPeople = new AvatarPeople(context);
            this.av = avatarPeople;
            avatarPeople.setTextSize(12.0f);

            ViewQrBadgeOverlay avatarOverlay = new ViewQrBadgeOverlay(context, avatarPeople, i2);
            linearLayout.addView(avatarOverlay, i2, i2);

            String qrLink = (FragmentInfo.this.itemRecentGroup != null)
                    ? FragmentInfo.this.itemRecentGroup.getCallalinkLink() : null;
            if (qrLink != null) {
                avatarOverlay.setBadgeVisible(true);
                String qrName = FragmentInfo.this.itemRecentGroup.getDisplayNameForQr();
                avatarOverlay.setOnBadgeClick(v -> new DialogShowQr(getContext(), qrLink, qrName, theme).show());
            }
            TextW textW3 = new TextW(context);
            this.tvName = textW3;
            textW3.setupText(400, 7.0f);
            textW3.setSingleLine();
            textW3.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            textW3.setMarqueeRepeatLimit(-1);
            textW3.setHorizontallyScrolling(true);
            textW3.setSelected(true);
            textW3.setPadding(i, i / 8, i, 0);
            linearLayout.addView(textW3, -2, -2);
            if (availableSIMCardLabels.size() > 1) {
                LayoutChooseSimInfo layoutChooseSimInfo = new LayoutChooseSimInfo(context);
                layoutChooseSimInfo.init(theme);
                layoutChooseSimInfo.setSim(this.posSim);
                LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-2, -2);
                layoutParams5.setMargins(0, i3, 0, 0);
                linearLayout.addView(layoutChooseSimInfo, layoutParams5);
                layoutChooseSimInfo.setOnClickListener(new OnClickListener() { 
                    @Override 
                    public final void onClick(View view) {
                        ViewInfo.this.onChooseSimClick(view);
                    }
                });
            }
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout2.setGravity(1);
            LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams6.setMargins(0, i, 0, 0);
            linearLayout.addView(linearLayout2, layoutParams6);
            final int ICON_COUNT = 5;
            int marginUnit = (widthScreen * 5) / 360;
            int iconWidth = (widthScreen - (marginUnit * 2 * ICON_COUNT)) / ICON_COUNT;
            ViewItemInfo viewItemInfo = new ViewItemInfo(context);
            viewItemInfo.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewInfo.this.m125x6b7ade26(view);
                }
            });
            viewItemInfo.setInfo(R.drawable.ic_message, R.string.message, true, theme);
            LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(iconWidth, -2);
            layoutParams7.setMargins(marginUnit, 0, marginUnit, 0);
            linearLayout2.addView(viewItemInfo, layoutParams7);
            ViewItemInfo viewItemInfo2 = new ViewItemInfo(context);
            viewItemInfo2.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewInfo.this.m126x473c59e7(view);
                }
            });
            viewItemInfo2.setInfo(R.drawable.ic_call_info, R.string.call, true, theme);
            LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(iconWidth, -2);
            layoutParams8.setMargins(marginUnit, 0, marginUnit, 0);
            linearLayout2.addView(viewItemInfo2, layoutParams8);
            this.viewItemSchedule = new ViewItemInfo(context);
            this.viewItemSchedule.setInfo(R.drawable.ic_schedule, R.string.schedule, true, theme);
            this.viewItemSchedule.setOnClickListener(new OnClickListener() {
                @Override
                public final void onClick(View view) {
                    ViewInfo.this.m140xopenSchedule(view);
                }
            });
            LinearLayout.LayoutParams layoutParams10b = new LinearLayout.LayoutParams(iconWidth, -2);
            layoutParams10b.setMargins(marginUnit, 0, marginUnit, 0);
            linearLayout2.addView(this.viewItemSchedule, layoutParams10b);
            ViewItemInfo viewItemInfo3 = new ViewItemInfo(context);
            viewItemInfo3.setInfo(R.drawable.ic_facetime, R.string.facetime, false, theme);
            LinearLayout.LayoutParams layoutParams9 = new LinearLayout.LayoutParams(iconWidth, -2);
            layoutParams9.setMargins(marginUnit, 0, marginUnit, 0);
            linearLayout2.addView(viewItemInfo3, layoutParams9);
            ViewItemInfo viewItemInfo4 = new ViewItemInfo(context);
            viewItemInfo4.setInfo(R.drawable.ic_mail, R.string.mail, false, theme);
            LinearLayout.LayoutParams layoutParams10 = new LinearLayout.LayoutParams(iconWidth, -2);
            layoutParams10.setMargins(marginUnit, 0, marginUnit, 0);
            linearLayout2.addView(viewItemInfo4, layoutParams10);
            if (FragmentInfo.this.itemRecentGroup != null) {
                LinearLayout linearLayout3 = new LinearLayout(context);
                linearLayout3.setPadding(i3, i3, i3, i);
                linearLayout3.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams layoutParams11 = new LinearLayout.LayoutParams((widthScreen * 342) / 360, -2);
                layoutParams11.setMargins(0, i, 0, 0);
                linearLayout.addView(linearLayout3, layoutParams11);
//                TextW textW4 = new TextW(context);
//                textW4.setPadding(i3, i3, i3, 0);
//                textW4.setupText(400, 3.5f);
//                textW4.setText(OtherUtils.longToTimeTitle(getContext(), FragmentInfo.this.itemRecentGroup.time));
//                linearLayout3.addView(textW4, -1, -2);
//                Iterator<ItemRecent> it = FragmentInfo.this.itemRecentGroup.arrRecent.iterator();
//                while (it.hasNext()) {
//                    LayoutShowRecent layoutShowRecent = new LayoutShowRecent(context);
//                    layoutShowRecent.setRecent(it.next(), this.theme);
//                    linearLayout3.addView(layoutShowRecent, -1, -2);
//                }
//                if (this.theme) {
//                    linearLayout3.setBackground(OtherUtils.bgIcon(-1, (widthScreen * 3.0f) / 100.0f));
//                    textW4.setTextColor(-16777216);
//                } else {
//                    linearLayout3.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), (widthScreen * 3.0f) / 100.0f));
//                    textW4.setTextColor(-1);
//                }
                this.blockReasonResolver = CallBlockReasonResolver.load(context);
                CallLogGroupHelper.addRecentsGroupedByDay(
                        linearLayout3, context, FragmentInfo.this.itemRecentGroup.arrRecent, this.theme,
                        FragmentInfo.this.displayMode, this.blockReasonResolver, 3,
                        () -> {
                            if (getActivity() instanceof ActivityHome) {
                                FragmentCallLogs f = FragmentCallLogs.newInstance(
                                        FragmentInfo.this.itemRecentGroup, FragmentInfo.this.displayMode);
                                f.setContactResult(FragmentInfo.this.contactResult);
                                ((ActivityHome) getActivity()).showFragment(f, true);
                            }
                        });
            }
            LinearLayout linearLayout4 = new LinearLayout(context);
            this.llNumber = linearLayout4;
            linearLayout4.setOrientation(LinearLayout.VERTICAL);
            int i6 = (widthScreen * 342) / 360;
            LinearLayout.LayoutParams layoutParams12 = new LinearLayout.LayoutParams(i6, -2);
            layoutParams12.setMargins(0, i, 0, 0);
            linearLayout.addView(linearLayout4, layoutParams12);

            ViewItemCallalinkLink viewCallalinkLink = new ViewItemCallalinkLink(context);
            LinearLayout.LayoutParams callalinkParams = new LinearLayout.LayoutParams(i6, -2);
            callalinkParams.setMargins(0, i, 0, 0);
            linearLayout.addView(viewCallalinkLink, callalinkParams);

            if (FragmentInfo.this.itemRecentGroup != null) {
                viewCallalinkLink.setLink(FragmentInfo.this.itemRecentGroup.getCallalinkLink(), FragmentInfo.this.itemRecentGroup.getDisplayNameForQr());
            } else {
                viewCallalinkLink.setVisibility(View.GONE);
            }

            schedulePreview = new LayoutSchedulePreview(context);
            LinearLayout.LayoutParams scheduleParams = new LinearLayout.LayoutParams(i6, -2);
            scheduleParams.setMargins(0, i, 0, 0);
            linearLayout.addView(schedulePreview, scheduleParams);

            schedulePreview.setOnManageScheduleClickListener(phone -> {
                if (getActivity() instanceof ActivityHome && phone != null) {
                    FragmentScheduleEditor editor = FragmentScheduleEditor.newInstance(phone);
                    ((ActivityHome) getActivity()).showFragment(editor, true);
                }
            });

            schedulePreview.setOnRemoveScheduleClickListener(phone -> {
                if (phone == null) return;
                new DialogNotification(getContext(), R.string.remove_schedule_title,
                        R.string.remove_schedule_message, theme, new DialogResult() {
                    @Override
                    public void onActionClick() {
                        if (authToken == null) {
                            Toast.makeText(getContext(), "Session expired. Please log in again.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        scheduleRepository.deleteSchedule(phone, new Callback<GenericResponse>() {
                            @Override
                            public void onResponse(Call<GenericResponse> call, Response<GenericResponse> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    if (!isAdded() || getContext() == null) return;
                                    Toast.makeText(getContext(), "Schedule removed", Toast.LENGTH_SHORT).show();
                                    ViewFragmentUnifiedSchedule.invalidateCache(getContext());
                                    loadSchedule(); // refresh — LayoutSchedulePreview already shows "No schedule set" when null
                                } else {
                                    String msg = (response.body() != null && response.body().getMessage() != null)
                                            ? response.body().getMessage() : "Couldn't remove schedule. Please try again.";
                                    Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<GenericResponse> call, Throwable t) {
                                if (!isAdded() || getContext() == null) return;
                                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).show();
            });

            LinearLayout linearLayout5 = new LinearLayout(context);
            linearLayout5.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams13 = new LinearLayout.LayoutParams(i6, -2);
            layoutParams13.setMargins(0, i, 0, 0);
            linearLayout.addView(linearLayout5, layoutParams13);
            TextW textW5 = new TextW(context);
            textW5.setText(R.string.note);
            textW5.setupText(350, 3.2f);
            textW5.setPadding(i, i, 0, 0);
            linearLayout5.addView(textW5, -1, -2);
            this.edtNote.setupText(400, 3.9f);
            this.edtNote.setBackgroundColor(0);
            this.edtNote.setPadding(i, 0, i, i);
            this.edtNote.setHint(R.string.add_note);
            this.edtNote.setHintTextColor(Color.parseColor("#B8B8B8"));
            this.edtNote.addTextChangedListener(new TextWatcher() { 
                @Override 
                public void afterTextChanged(Editable editable) {
                }

                @Override 
                public void beforeTextChanged(CharSequence charSequence, int i7, int i8, int i9) {
                }

                @Override 
                public void onTextChanged(CharSequence charSequence, int i7, int i8, int i9) {
                    ViewInfo.this.handler.removeCallbacks(ViewInfo.this.runnable);
                    ViewInfo.this.itemNote.note = charSequence.toString();
                    ViewInfo.this.handler.postDelayed(ViewInfo.this.runnable, 500L);
                }
            });
            linearLayout5.addView(this.edtNote, -1, -2);
            LinearLayout linearLayout6 = new LinearLayout(context);
            linearLayout6.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams14 = new LinearLayout.LayoutParams(i6, -2);
            layoutParams14.setMargins(0, i, 0, 0);
            linearLayout.addView(linearLayout6, layoutParams14);
            TextW textW6 = new TextW(context);
            textW6.setText(R.string.send_message);
            textW6.setupText(400, 3.9f);
            textW6.setTextColor(Color.parseColor("#007AFF"));
            textW6.setPadding(i, i, i, i);
            textW6.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewInfo.this.m127x22fdd5a8(view);
                }
            });
            linearLayout6.addView(textW6, -1, -2);
            View view = new View(context);
            LinearLayout.LayoutParams layoutParams15 = new LinearLayout.LayoutParams(-1, 1);
            layoutParams15.setMargins(i, 0, 0, 0);
            linearLayout6.addView(view, layoutParams15);
            TextW textW7 = new TextW(context);
            textW7.setText(R.string.share_contact);
            textW7.setupText(400, 3.9f);
            textW7.setTextColor(Color.parseColor("#007AFF"));
            textW7.setPadding(i, i, i, i);
            textW7.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    ViewInfo.this.m128xfebf5169(view2);
                }
            });
            linearLayout6.addView(textW7, -1, -2);
            View view2 = new View(context);
            LinearLayout.LayoutParams layoutParams16 = new LinearLayout.LayoutParams(-1, 1);
            layoutParams16.setMargins(i, 0, 0, 0);
            linearLayout6.addView(view2, layoutParams16);
            TextW textW8 = new TextW(context);
            textW8.setText(R.string.add_fav);
            textW8.setupText(400, 3.9f);
            textW8.setTextColor(Color.parseColor("#007AFF"));
            textW8.setPadding(i, i, i, i);
            textW8.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view3) {
                    ViewInfo.this.m129xda80cd2a(view3);
                }
            });
            linearLayout6.addView(textW8, -1, -2);
            this.tvBlock.setupText(400, 3.9f);
            this.tvBlock.setPadding(i, i, i, i);
            this.tvBlock.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view3) {
                    ViewInfo.this.m130xb64248eb(view3);
                }
            });
            LinearLayout.LayoutParams layoutParams17 = new LinearLayout.LayoutParams(i6, -2);
            layoutParams17.setMargins(0, i, 0, 0);
            linearLayout.addView(this.tvBlock, layoutParams17);
            linearLayout.addView(new View(context), -1, widthScreen / 10);
            Log.d("THEME_DEBUG before if", "theme = " + this.theme);
            Log.d("THEME_DEBUG before if", "MyShare.getTheme = " + MyShare.getTheme(context));
            if (this.theme) {
                setBackgroundColor(Color.parseColor("#F2F2F7"));
                float f = (widthScreen * 3.0f) / 100.0f;
                linearLayout4.setBackground(OtherUtils.bgIcon(-1, f));
                linearLayout6.setBackground(OtherUtils.bgIcon(-1, f));
                linearLayout5.setBackground(OtherUtils.bgIcon(-1, f));
                this.tvBlock.setBackground(OtherUtils.bgIcon(-1, f));
                viewCallalinkLink.setBackground(OtherUtils.bgIcon(-1, f));
                this.tvName.setTextColor(-16777216);
                textW5.setTextColor(-16777216);
                this.edtNote.setTextColor(-16777216);
                view.setBackgroundColor(Color.parseColor("#dedede"));
                view2.setBackgroundColor(Color.parseColor("#dedede"));
            } else {
                setBackgroundColor(Color.parseColor("#2C2C2C"));
                float f2 = (widthScreen * 3.0f) / 100.0f;
                linearLayout4.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2));
                linearLayout6.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2));
                linearLayout5.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2));
                this.tvBlock.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2));
                viewCallalinkLink.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2));
                this.tvName.setTextColor(-1);
                textW5.setTextColor(-1);
                this.edtNote.setTextColor(-1);
                view.setBackgroundColor(Color.parseColor("#5c5c5c"));
                view2.setBackgroundColor(Color.parseColor("#5c5c5c"));
            }
            updateContact();
            loadSchedule();
            new Thread(new Runnable() { 
                @Override 
                public final void run() {
                    ViewInfo.this.m122x2c824af3();
                }
            }).start();
        }



        public  boolean m120xfc74ef22(Message message) {
            Iterator<ItemNote> it = this.arrNote.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ItemNote next = it.next();
                if (next.idContact.equals(FragmentInfo.this.itemContact.getId())) {
                    this.itemNote = next;
                    break;
                }
            }
            if (this.itemNote == null) {
                ItemNote itemNote = new ItemNote();
                this.itemNote = itemNote;
                itemNote.idContact = FragmentInfo.this.itemContact.getId();
                this.arrNote.add(this.itemNote);
            }
            if (this.itemNote.note != null && !this.itemNote.note.isEmpty()) {
                this.edtNote.setText(this.itemNote.note);
            }
            updateViewFav();
            updateBlock();
            return true;
        }



        public  void m121xd8366ae3(View view) {
            FragmentInfo.this.onBack();
        }



        public  void m123xb3f7e6a4(View view) {
            FragmentInfo.this.onBack();
        }



        public  void m124x8fb96265(View view) {
            ActionUtils.onEditContact(getContext(), FragmentInfo.this.launcher, FragmentInfo.this.itemContact);
        }



        public  void m125x6b7ade26(View view) {
            onMessageClick();
        }



        public  void m126x473c59e7(View view) {
            onCallClick();
        }



        public  void m127x22fdd5a8(View view) {
            onMessageClick();
        }



        public  void m128xfebf5169(View view) {
            onShareClick();
        }



        public  void m129xda80cd2a(View view) {
            onAddToFav();
        }



        public  void m130xb64248eb(View view) {
            onBlock();
        }



        public  void m122x2c824af3() {
            this.arrBlock = MyShare.getArrBlock(getContext());
            this.arrFav = MyShare.getFav(getContext());
            this.arrNote = MyShare.getArrNote(getContext());
            this.handler.sendEmptyMessage(1);
        }

        public void m140xopenSchedule(View view) {
            if (FragmentInfo.this.itemContact.getArrPhone().isEmpty()) return;
            String phone = FragmentInfo.this.itemContact.getArrPhone().get(0).getNumber();
            if (getActivity() instanceof ActivityHome && phone != null) {
                FragmentScheduleEditor editor = FragmentScheduleEditor.newInstance(phone);
                ((ActivityHome) getActivity()).showFragment(editor, true);
            }
        }

        public void updateContact() {
            this.av.setImage(FragmentInfo.this.itemContact.getPhoto(), FragmentInfo.this.itemContact.getName());
            if (FragmentInfo.this.itemContact.getName() != null) {
                this.tvName.setText(FragmentInfo.this.itemContact.getName());
            } else {
                this.tvName.setText(R.string.unknown);
            }
            int widthScreen = OtherUtils.getWidthScreen(getContext()) / 25;
            this.llNumber.removeAllViews();
            if (FragmentInfo.this.itemContact.getArrPhone().size() > 0) {
                this.llNumber.setVisibility(View.VISIBLE);
                for (int i = 0; i < FragmentInfo.this.itemContact.getArrPhone().size(); i++) {
                    ViewItemNumber viewItemNumber = new ViewItemNumber(getContext());
                    viewItemNumber.setNumber(FragmentInfo.this.itemContact.getArrPhone().get(i), FragmentInfo.this.itemRecentGroup, this.theme, this);
                    if (FragmentInfo.this.itemRecentGroup != null && FragmentInfo.this.itemRecentGroup.arrRecent.get(0).type == 3 && PhoneNumberUtils.compare(FragmentInfo.this.itemRecentGroup.arrRecent.get(0).number, FragmentInfo.this.itemContact.getArrPhone().get(i).getNumber())) {
                        viewItemNumber.setMissCall();
                    }
                    this.llNumber.addView(viewItemNumber, -1, -2);
                    this.arrViewNumber.add(viewItemNumber);
                    if (i < FragmentInfo.this.itemContact.getArrPhone().size() - 1) {
                        View view = new View(getContext());
                        if (this.theme) {
                            view.setBackgroundColor(Color.parseColor("#dedede"));
                        } else {
                            view.setBackgroundColor(Color.parseColor("#5c5c5c"));
                        }
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, 1);
                        layoutParams.setMargins(widthScreen, 0, 0, 0);
                        this.llNumber.addView(view, layoutParams);
                    }
                }
            } else {
                this.llNumber.setVisibility(View.GONE);
            }
            ArrayList<ItemContact> arrayList = this.arrBlock;
            if (arrayList != null) {
                Iterator<ItemContact> it = arrayList.iterator();
                while (it.hasNext()) {
                    ItemContact next = it.next();
                    if (next.getId() != null && next.getId().equals(FragmentInfo.this.itemContact.getId())) {
                        this.arrBlock.remove(next);
                        this.arrBlock.add(new ItemContact(FragmentInfo.this.itemContact.getId(), FragmentInfo.this.itemContact.getArrPhone()));
                        MyShare.putBlockNumber(getContext(), this.arrBlock);
                        return;
                    }
                }
            }
        }


        public void onChooseSimClick(final View view) {
            int[] iArr = new int[2];
            view.getLocationInWindow(iArr);
            new DialogShowSim(getContext(), this.arrSim, iArr[1], new LayoutListSim.SimItemClick() { 
                @Override 
                public final void onSimClick(int i) {
                    ViewInfo.this.m133xf533e573(view, i);
                }
            }).show();
        }



        public  void m133xf533e573(View view, int i) {
            this.posSim = i;
            MyShare.putPosSim(getContext(), i);
            ((LayoutChooseSimInfo) view).setSim(i);
        }

        private void onMessageClick() {
            if (FragmentInfo.this.itemContact.getArrPhone().size() == 0) {
                return;
            }
            if (FragmentInfo.this.itemContact.getArrPhone().size() == 1) {
                OtherUtils.sendMessage(getContext(), FragmentInfo.this.itemContact.getArrPhone().get(0).getNumber());
            } else {
                new DialogChooseNumber(getContext(), FragmentInfo.this.itemContact, this.theme, new DialogChooseNumber.NumberContactResult() { 
                    @Override 
                    public final void onNumberResult(ItemPhone itemPhone) {
                        ViewInfo.this.m134x1076ea3b(itemPhone);
                    }
                }).show();
            }
        }



        public  void m134x1076ea3b(ItemPhone itemPhone) {
            OtherUtils.sendMessage(getContext(), itemPhone.getNumber());
        }

        public void onCallClick() {
            if (FragmentInfo.this.itemContact.getArrPhone().size() == 0) {
                return;
            }
            if (FragmentInfo.this.itemContact.getArrPhone().size() == 1) {
                makeCall(FragmentInfo.this.itemContact.getArrPhone().get(0).getNumber());
            } else {
                new DialogChooseNumber(getContext(), FragmentInfo.this.itemContact, this.theme, new DialogChooseNumber.NumberContactResult() { 
                    @Override 
                    public final void onNumberResult(ItemPhone itemPhone) {
                        ViewInfo.this.m132xbc1c3f0b(itemPhone);
                    }
                }).show();
            }
        }



        public  void m132xbc1c3f0b(ItemPhone itemPhone) {
            makeCall(itemPhone.getNumber());
        }

        private void makeCall(String str) {
            PhoneAccountHandle phoneAccountHandle;
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

        private void onShareClick() {
            StringBuilder sb = new StringBuilder();
            if (FragmentInfo.this.itemContact.getName() != null) {
                sb.append(FragmentInfo.this.itemContact.getName());
                sb.append(":");
            }
            Iterator<ItemPhone> it = FragmentInfo.this.itemContact.getArrPhone().iterator();
            while (it.hasNext()) {
                ItemPhone next = it.next();
                if (next.getNumber() != null) {
                    sb.append("\n");
                    sb.append(next.getNumber());
                }
            }
            ActionUtils.shareText(getContext(), sb.toString());
        }

        private void onAddToFav() {
            new DialogAddFav(getContext(), this.theme, FragmentInfo.this.itemContact, new FavResult() { 
                @Override 
                public final void onFavResult(ItemFavorites itemFavorites) {
                    ViewInfo.this.m131x858cb9bf(itemFavorites);
                }
            }).show();
        }



        public  void m131x858cb9bf(ItemFavorites itemFavorites) {
            boolean z;
            Iterator<ItemFavorites> it = this.arrFav.iterator();
            while (true) {
                if (!it.hasNext()) {
                    z = true;
                    break;
                }
                ItemFavorites next = it.next();
                if (itemFavorites.type == next.type && itemFavorites.number.equals(next.number)) {
                    z = false;
                    break;
                }
            }
            if (z) {
                this.arrFav.add(itemFavorites);
                MyShare.putFav(getContext(), this.arrFav);
                updateViewFav();
                FragmentInfo.this.contactResult.onFavoritesChange();
            }
        }
        @Override 
        public void onNumberResult(ItemPhone itemPhone) {
            makeCall(itemPhone.getNumber());
        }

        @Override 
        public void onLongClickNumber(ItemPhone itemPhone) {
            OtherUtils.copyToClip(getContext(), itemPhone.getNumber());
        }

        private void updateViewFav() {
            Iterator<ViewItemNumber> it = this.arrViewNumber.iterator();
            while (it.hasNext()) {
                it.next().updateViewFav(this.arrFav);
            }
        }

        private void updateBlock() {
            boolean z;
            Iterator<ItemContact> it = this.arrBlock.iterator();
            while (true) {
                z = true;
                if (!it.hasNext()) {
                    z = false;
                    break;
                }
                ItemContact next = it.next();
                if ((FragmentInfo.this.itemContact.getId() != null && FragmentInfo.this.itemContact.getId().equals(next.getId())) || (!FragmentInfo.this.itemContact.getArrPhone().isEmpty() && FragmentInfo.this.itemContact.getArrPhone().get(0).getNumber() != null && !next.getArrPhone().isEmpty() && next.getArrPhone().get(0).getNumber() != null && PhoneNumberUtils.compare(FragmentInfo.this.itemContact.getArrPhone().get(0).getNumber(), next.getArrPhone().get(0).getNumber()))) {
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
        }

        private void onBlock() {
            if (isBlocked()) {
                unblockContact();
            } else {
                if (Build.VERSION.SDK_INT >= 29) {
                    RoleManager roleManager = (RoleManager) getContext().getSystemService(Context.ROLE_SERVICE);
                    if (roleManager.isRoleHeld("android.app.role.CALL_SCREENING")) {
                        showDialogBlock();
                    } else {
                        FragmentInfo.this.lPer.launch(roleManager.createRequestRoleIntent("android.app.role.CALL_SCREENING"));
                    }
                } else {
                    showDialogBlock();
                }
            }
        }

        private boolean isBlocked() {
            for (ItemContact c : arrBlock) {
                if (matchesCurrentContact(c)) return true;
            }
            return false;
        }

        private void unblockContact() {
            Iterator<ItemContact> it = arrBlock.iterator();
            while (it.hasNext()) {
                if (matchesCurrentContact(it.next())) {
                    it.remove();
                    break;
                }
            }
            MyShare.putBlockNumber(getContext(), arrBlock);
            updateBlock();
        }

        private boolean matchesCurrentContact(ItemContact candidate) {
            if (FragmentInfo.this.itemContact.getId() != null
                    && FragmentInfo.this.itemContact.getId().equals(candidate.getId())) {
                return true;
            }
            return !FragmentInfo.this.itemContact.getArrPhone().isEmpty()
                    && FragmentInfo.this.itemContact.getArrPhone().get(0).getNumber() != null
                    && !candidate.getArrPhone().isEmpty()
                    && candidate.getArrPhone().get(0).getNumber() != null
                    && PhoneNumberUtils.compare(
                    FragmentInfo.this.itemContact.getArrPhone().get(0).getNumber(),
                    candidate.getArrPhone().get(0).getNumber());
        }

        public void showDialogBlock() {
            new DialogNotification(getContext(), this.theme, new DialogResult() {
                @Override
                public void onActionClick() {
                    arrBlock.add(new ItemContact(FragmentInfo.this.itemContact.getId(), FragmentInfo.this.itemContact.getArrPhone()));
                    MyShare.putBlockNumber(getContext(), arrBlock);
                    updateBlock();
                }
            }).show();
        }

        private void loadSchedule() {

            if (FragmentInfo.this.itemContact.getArrPhone().isEmpty()) {
                schedulePreview.setVisibility(GONE);
                viewItemSchedule.setInfo(R.drawable.ic_schedule, R.string.schedule, true, theme);
                return;
            }

            String phone = FragmentInfo.this.itemContact.getArrPhone().get(0).getNumber();

            if (authToken == null || authToken.isEmpty()) {
                schedulePreview.setVisibility(GONE);
                viewItemSchedule.setInfo(R.drawable.ic_schedule, R.string.schedule, true, theme);
                return;
            }

            scheduleRepository.getSchedule(phone, new Callback<PhoneScheduleResponse>() {
                @Override
                public void onResponse(Call<PhoneScheduleResponse> call, Response<PhoneScheduleResponse> response) {
                    if (!isAdded() || getContext() == null) return;
                    if (!response.isSuccessful() || response.body() == null) {
                        viewItemSchedule.setInfo(R.drawable.ic_schedule, R.string.schedule, true, theme);
                        schedulePreview.setVisibility(GONE);
                        return;
                    }
                    ItemSchedulePreview item = ScheduleMapper.toItem(response.body());
                    schedulePreview.setSchedule(item, phone);
                    updateScheduleIcon(item);
                }

                @Override
                public void onFailure(Call<PhoneScheduleResponse> call, Throwable t) {
                    if (!isAdded() || getContext() == null) return;
                    Log.e("Schedule", "Failed to load schedule", t);
                    schedulePreview.setVisibility(GONE);
                    viewItemSchedule.setInfo(R.drawable.ic_schedule, R.string.schedule, true, theme);
                }
            });
        }

        private void updateScheduleIcon(ItemSchedulePreview item) {
            boolean hasSchedule = false;
            if (item != null && item.days != null) {
                for (com.thelinkphone.app.item.ItemScheduleDay day : item.days) {
                    if (day.hasSchedule) {
                        hasSchedule = true;
                        break;
                    }
                }
            }

            if (hasSchedule) {
                viewItemSchedule.setInfo(R.drawable.ic_scheduled, R.string.scheduled, true, theme);
            } else {
                viewItemSchedule.setInfo(R.drawable.ic_schedule, R.string.schedule, true, theme);
            }
        }
    }
}
