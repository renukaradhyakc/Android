package com.thelinkphone.app.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telecom.PhoneAccountHandle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aitsuki.swipe.SwipeMenuRecyclerView;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.R;
import com.thelinkphone.app.adapter.AdapterRecent;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.custom.ViewModeRecent;
import com.thelinkphone.app.dialog.DialogAddFav;
import com.thelinkphone.app.dialog.DialogNotification;
import com.thelinkphone.app.dialog.DialogResult;
import com.thelinkphone.app.dialog.FavResult;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemFavorites;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.repository.RecentsRepository;
import com.thelinkphone.app.utils.CallBlockReasonResolver;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ReadContact;
import com.thelinkphone.app.utils.SimUtils;


import java.util.ArrayList;


public class FragmentRecents extends BaseFragment {
    private ViewFragmentRecents viewFragmentRecents;

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (this.viewFragmentRecents == null) {
            long createStart = System.currentTimeMillis();
            Log.d("RECENTS_UI", "Creating ViewFragmentRecents");
            this.viewFragmentRecents = new ViewFragmentRecents(layoutInflater.getContext());
            Log.d("RECENTS_UI", "ViewFragmentRecents created in " + (System.currentTimeMillis() - createStart) + " ms");
        }
        return this.viewFragmentRecents;
    }

    @Override 
    public void onResume() {
        super.onResume();
        ViewFragmentRecents viewFragmentRecents = this.viewFragmentRecents;
        if (viewFragmentRecents != null) {
            viewFragmentRecents.loadAllRecent();
        }
    }



    public class ViewFragmentRecents extends RelativeLayout implements AdapterRecent.RecentItemClick {
        private final AdapterRecent adapterRecent;
        private final ArrayList<ItemRecentGroup> arrRecent;
        private final ArrayList<ItemSimInfo> arrSim;
        private final int posSim;
        private final boolean theme;
        private final TextW tvEdit;
        private final TextW tvEmpty;
        private final TextW tvRemoveAll;
        private volatile boolean isLoading = false;
        private boolean isNavigating = false;
        private CallBlockReasonResolver blockReasonResolver;

        public ViewFragmentRecents(Context context) {
            super(context);
            long ctorStart = System.currentTimeMillis();
            Log.d("RECENTS_UI", "Constructor START");
            ArrayList<ItemSimInfo> availableSIMCardLabels = SimUtils.getAvailableSIMCardLabels(context);
            this.arrSim = availableSIMCardLabels;
            this.posSim = MyShare.getPosSim(context);
            int widthScreen = OtherUtils.getWidthScreen(context) / 25;
            boolean theme = MyShare.getTheme(context);
            this.theme = theme;
            ArrayList<ItemRecentGroup> arrayList = new ArrayList<>();
            this.arrRecent = arrayList;
            AdapterRecent adapterRecent = new AdapterRecent(arrayList, availableSIMCardLabels, theme, this);
            this.adapterRecent = adapterRecent;
            TextW textW = new TextW(context);
            this.tvEdit = textW;
            textW.setId(100);
            textW.setTextColor(Color.parseColor("#007AFF"));
            textW.setPadding(widthScreen, widthScreen, widthScreen, widthScreen);
            textW.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewFragmentRecents.this.m149x69848d70(view);
                }
            });
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            layoutParams.addRule(21);
            layoutParams.setMargins(0, 0, 0, 0);
            addView(textW, layoutParams);
            TextW textW2 = new TextW(context);
            this.tvRemoveAll = textW2;
            textW2.setId(654982);
            textW2.setTextColor(Color.parseColor("#007AFF"));
            textW2.setupText(400, 4.0f);
            textW2.setPadding(widthScreen, widthScreen, widthScreen, widthScreen);
            textW2.setText(R.string.delete);
            textW2.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewFragmentRecents.this.m150x49fde371(view);
                }
            });
            LayoutParams layoutParams2 = new LayoutParams(-2, -2);
            layoutParams2.setMargins(0, 0, 0, 0);
            addView(textW2, layoutParams2);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setId(View.generateViewId());
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(17);
            LayoutParams layoutParams3 = new LayoutParams(-1, -1);
            layoutParams3.addRule(6, textW.getId());
            layoutParams3.addRule(8, textW.getId());
            addView(linearLayout, layoutParams3);
            ViewModeRecent viewModeRecent = new ViewModeRecent(context);
            viewModeRecent.setModeResult(new ViewModeRecent.ModeResult() { 
                @Override 
                public final void onMode(int mode) {
                    ViewFragmentRecents.this.m151x2a773972(mode);
                }
            });
            linearLayout.addView(viewModeRecent, -2, -2);

            // --- Search bar: fixed sibling, NOT inside the RecyclerView, so it never scrolls away ---
            int contentMargin = dp(16);
            LinearLayout searchBar = new LinearLayout(context);
            searchBar.setId(View.generateViewId());
            searchBar.setOrientation(LinearLayout.HORIZONTAL);
            searchBar.setGravity(Gravity.CENTER_VERTICAL);
            searchBar.setPadding(dp(16), dp(14), dp(16), dp(14));

            GradientDrawable searchBg = new GradientDrawable();
            searchBg.setCornerRadius(dp(22));
            searchBg.setColor(theme ? Color.parseColor("#F0F0F0") : Color.parseColor("#3A3A3C"));
            searchBar.setBackground(searchBg);

            ImageView searchIcon = new ImageView(context);
            searchIcon.setImageResource(R.drawable.ic_search);
            searchIcon.setColorFilter(theme ? Color.parseColor("#8A8A8E") : Color.parseColor("#AEAEB2"));
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(16), dp(16));
            iconParams.setMarginEnd(dp(8));
            searchBar.addView(searchIcon, iconParams);

            TextW tvSearchHint = new TextW(context);
            tvSearchHint.setText(R.string.search);
            tvSearchHint.setTextColor(theme ? Color.parseColor("#8A8A8E") : Color.parseColor("#AEAEB2"));
            tvSearchHint.setupText(400, 3.6f);
            searchBar.addView(tvSearchHint, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            searchBar.setOnClickListener(v -> {
                Log.d("RECENTS_SEARCH", "Search bar tapped, missedOnly=" + adapterRecent.getMode());
                if (getActivity() instanceof ActivityHome) {
                    ActivityHome activity = (ActivityHome) getActivity();
                    FragmentRecentsSearch newInstance = FragmentRecentsSearch.newInstance(adapterRecent.getMode());
                    newInstance.setContactResult(FragmentRecents.this.contactResult);
                    activity.showFragment(newInstance, true);
                }
            });

            LayoutParams searchBarParams = new LayoutParams(-1, -2);
            searchBarParams.addRule(3, linearLayout.getId());
            searchBarParams.setMargins(contentMargin, dp(4), contentMargin, dp(10));
            addView(searchBar, searchBarParams);
            // --- end search bar ---
            TextW textW3 = new TextW(context);
            this.tvEmpty = textW3;
            textW3.setupText(400, 3.8f);
            textW3.setText(R.string.empty_recent);
            textW3.setGravity(17);
            textW3.setTextColor(Color.parseColor("#aaaaaa"));
            textW3.setVisibility(View.GONE);
            LayoutParams layoutParams4 = new LayoutParams(-1, -1);
            layoutParams4.setMargins(contentMargin, 0, contentMargin, 0);
            layoutParams4.addRule(3, searchBar.getId());
            addView(textW3, layoutParams4);
            SwipeMenuRecyclerView swipeMenuRecyclerView = new SwipeMenuRecyclerView(context);
            swipeMenuRecyclerView.setAdapter(adapterRecent);
            swipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
            LayoutParams layoutParams5 = new LayoutParams(-1, -1);
            layoutParams5.addRule(3, searchBar.getId());
            addView(swipeMenuRecyclerView, layoutParams5);
            Log.d("RECENTS_UI", "RecyclerView attached");
            if (theme) {
                setBackgroundColor(-1);
            } else {
                setBackgroundColor(Color.parseColor("#2C2C2C"));
            }
            updateEdit();
            Log.d("RECENTS_UI", "Constructor END = " + (System.currentTimeMillis() - ctorStart) + " ms");
        }



        public  void m149x69848d70(View view) {
            AdapterRecent adapterRecent = this.adapterRecent;
            adapterRecent.setChoose(!adapterRecent.isChoose());
            updateEdit();
        }



        public  void m150x49fde371(View view) {
            onRemoveAll();
        }



        public  void m151x2a773972(int mode) {
            this.adapterRecent.setMode(mode);
        }

        private int dp(int value) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
        }

        private void updateEdit() {
            if (this.adapterRecent.isChoose()) {
                this.tvEdit.setText(R.string.done);
                this.tvRemoveAll.setVisibility(View.VISIBLE);
                this.tvEdit.setupText(600, 4.0f);
                return;
            }
            this.tvEdit.setText(R.string.edit);
            this.tvRemoveAll.setVisibility(View.GONE);
            this.tvEdit.setupText(400, 4.0f);
        }

        private void checkList() {
            Log.d("RECENTS_UI", "arrRecent size = " + arrRecent.size());
            if (this.arrRecent.isEmpty()) {
                this.tvEmpty.setVisibility(View.VISIBLE);
                this.tvEdit.setVisibility(View.INVISIBLE);
                this.tvRemoveAll.setVisibility(View.GONE);
                if (this.adapterRecent.isChoose()) {
                    this.adapterRecent.setChoose(false);
                    updateEdit();
                    return;
                }
                return;
            }
            this.tvEmpty.setVisibility(View.GONE);
            this.tvEdit.setVisibility(View.VISIBLE);
        }


        public void loadAllRecent() {

            Log.d("RECENTS_CACHE", "loadAllRecent() called");
            blockReasonResolver = CallBlockReasonResolver.load(getContext());
            adapterRecent.setBlockReasonResolver(blockReasonResolver);
            if (RecentsRepository.hasCache()) {

                Log.d("RECENTS_CACHE", "CACHE HIT");
                isNavigating=false;
                arrRecent.clear();
                arrRecent.addAll(RecentsRepository.getCache());

                Log.d("RECENTS_CACHE", "Loaded from cache. size=" + arrRecent.size());

                checkList();
                adapterRecent.addNewData();

                Log.d("RECENTS_CACHE", "UI updated from cache");

                return;
            }

            Log.d("RECENTS_CACHE", "CACHE MISS");

            if (isLoading) {
                Log.d("RECENTS_CACHE", "Already loading, skipping");
                return;
            }
            isLoading=true;
            isNavigating=false;

            final Handler handler = new Handler(new Handler.Callback() {
                @Override
                public final boolean handleMessage(Message message) {
                    return ViewFragmentRecents.this.m147x64a531a9(message);
                }
            });
            Log.d("RECENTS_UI", "Starting background load");
            new Thread(new Runnable() {
                @Override
                public final void run() {
                    ViewFragmentRecents.this.m148x451e87aa(handler);
                }
            }).start();
        }



        public  boolean m147x64a531a9(Message message) {
            Log.d("RECENTS_UI", "UI handler received message");
            long uiStart = System.currentTimeMillis();
            isLoading = false;

            checkList();
            Log.d("RECENTS_UI", "checkList finished in " + (System.currentTimeMillis() - uiStart) + " ms");
            this.adapterRecent.addNewData();
            Log.d("RECENTS_UI", "adapterRecent.addNewData finished in " + (System.currentTimeMillis() - uiStart) + " ms");
            return true;
        }



        public  void m148x451e87aa(Handler handler) {
            long t = System.currentTimeMillis();
            Log.d("RECENTS_UI", "Background load started");
            Log.d("RECENTS_CACHE", "Reading CallLog");
            ArrayList<ItemRecentGroup> data = ReadContact.getAllRecents(getContext());
            Log.d("RECENTS_CACHE", "CallLog loaded. size=" + data.size());
            this.arrRecent.clear();
            this.arrRecent.addAll(data);
            RecentsRepository.setCache(data);
            Log.d("RECENTS_CACHE", "Cache saved");
            Log.d("RECENTS_UI", "ReadContact finished in " + (System.currentTimeMillis() - t) + " ms, size=" + arrRecent.size());
            handler.sendEmptyMessage(1);
            Log.d("RECENTS_UI", "Sending UI update message");
        }

        @Override 
        public void onLongClick(final ItemRecentGroup itemRecentGroup) {
            ItemContact contactWithNumber = ReadContact.getContactWithNumber(getContext(), itemRecentGroup.arrRecent.get(0).number);
            if (contactWithNumber == null) {
                contactWithNumber = new ItemContact(itemRecentGroup.arrRecent.get(0).number);
            }
            new DialogAddFav(getContext(), this.theme, false, contactWithNumber, new FavResult() { 
                @Override 
                public final void onFavResult(ItemFavorites itemFavorites) {
                    ViewFragmentRecents.this.m152xf95ee2(itemRecentGroup, itemFavorites);
                }
            }).show();
        }

        public  void m152xf95ee2(ItemRecentGroup itemRecentGroup, ItemFavorites itemFavorites) {
            if (itemFavorites.type == 0) {
                OtherUtils.sendMessage(getContext(), itemFavorites.number);
            } else {
                onItemClick(itemRecentGroup);
            }
        }

        @Override 
        public void onItemClick(ItemRecentGroup itemRecentGroup) {
            PhoneAccountHandle phoneAccountHandle;
            if (this.arrSim.size() > 0) {
                if (this.posSim < this.arrSim.size()) {
                    phoneAccountHandle = this.arrSim.get(this.posSim).handle;
                } else {
                    phoneAccountHandle = this.arrSim.get(0).handle;
                }
                OtherUtils.call(getContext(), itemRecentGroup.arrRecent.get(0).number, phoneAccountHandle);
            }
        }

        @Override 
        public void onDel(ItemRecentGroup itemRecentGroup) {
            if (!OtherUtils.checkPer(getContext(), "android.permission.WRITE_CALL_LOG")) {
                FragmentRecents.this.requestPermissions(new String[]{"android.permission.WRITE_CALL_LOG"}, 1);
                return;
            }
            String[] strArr = new String[itemRecentGroup.arrRecent.size()];
            for (int i = 0; i < itemRecentGroup.arrRecent.size(); i++) {
                strArr[i] = itemRecentGroup.arrRecent.get(i).id;
            }
            ReadContact.removeRecents(getContext(), strArr);
            this.adapterRecent.removeRecent(itemRecentGroup);
            checkList();
        }

        @Override 
        public void onInfo(ItemRecentGroup itemRecentGroup) {

            if (isNavigating) return;
            isNavigating = true;

            if (FragmentRecents.this.getActivity() instanceof ActivityHome) {
                ActivityHome activity = (ActivityHome) FragmentRecents.this.getActivity();
                int displayMode = adapterRecent.getMode();
                ItemContact contactWithNumber = ReadContact.getContactWithNumber(getContext(), itemRecentGroup.arrRecent.get(0).number);
                if (contactWithNumber != null) {
                    FragmentInfo newInstance = FragmentInfo.newInstance(contactWithNumber, itemRecentGroup, R.string.back, displayMode);
                    newInstance.setContactResult(FragmentRecents.this.contactResult);
                    activity.showFragment(newInstance, true);
                }
                else {
                    FragmentInfoAnother newInstance2 = FragmentInfoAnother.newInstance(itemRecentGroup, displayMode);
                    newInstance2.setContactResult(FragmentRecents.this.contactResult);
                    activity.showFragment(newInstance2, true);
                }
            }
        }

        private void onRemoveAll() {
            new DialogNotification(getContext(), R.string.delete_all_recents, R.string.delete_all_recents_message, this.theme, new DialogResult() {
                @Override 
                public final void onActionClick() {
                    ViewFragmentRecents.this.m153x10def8b4();
                }
            }).show();
        }



        public  void m153x10def8b4() {
            ReadContact.removeAllRecents(getContext());
            this.adapterRecent.removeAll();
            checkList();
            updateEdit();
        }
    }
}
