package com.thelinkphone.app.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.aitsuki.swipe.SwipeMenuRecyclerView;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.R;
import com.thelinkphone.app.adapter.AdapterFav;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.dialog.DialogAddFav;
import com.thelinkphone.app.dialog.FavResult;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemFavorites;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.SimUtils;


import java.util.ArrayList;
import java.util.Iterator;


public class FragmentFavorites extends BaseFragment {
    private ViewFavorites viewFavorites;

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        getArrContact();
        if (this.viewFavorites == null) {
            this.viewFavorites = new ViewFavorites(layoutInflater.getContext());
        }
        return this.viewFavorites;
    }

    public void updateList() {
        this.viewFavorites.makeArr();
    }



    public class ViewFavorites extends RelativeLayout implements AdapterFav.FavItemClick {
        private final AdapterFav adapterFav;
        private final ArrayList<ItemFavorites> arrFav;
        private final ArrayList<ItemSimInfo> arrSim;
        private final int posSim;
        private final boolean theme;
        private final TextW tvEdit;
        private final TextW tvEmpty;

        public ViewFavorites(Context context) {
            super(context);
            this.arrSim = SimUtils.getAvailableSIMCardLabels(context);
            this.posSim = MyShare.getPosSim(context);
            int widthScreen = OtherUtils.getWidthScreen(context) / 25;
            ArrayList<ItemFavorites> arrayList = new ArrayList<>();
            this.arrFav = arrayList;
            makeArr();
            boolean theme = MyShare.getTheme(context);
            this.theme = theme;
            AdapterFav adapterFav = new AdapterFav(arrayList, theme, this);
            this.adapterFav = adapterFav;
            ImageView imageView = new ImageView(context);
            imageView.setId(100);
            imageView.setPadding(widthScreen, widthScreen, widthScreen, widthScreen);
            imageView.setImageResource(R.drawable.ic_add_contact);
            imageView.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewFavorites.this.m116x8b7ec51e(view);
                }
            });
            int i = (int) (widthScreen * 3.2f);
            LayoutParams layoutParams = new LayoutParams(i, i);
            layoutParams.setMargins(0, MyShare.getSizeNotification(context), 0, 0);
            addView(imageView, layoutParams);
            TextW textW = new TextW(context);
            this.tvEdit = textW;
            textW.setupText(400, 4.0f);
            textW.setPadding(widthScreen, 0, widthScreen, 0);
            textW.setGravity(16);
            textW.setTextColor(Color.parseColor("#007AFF"));
            textW.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewFavorites.this.m117x8c4d439f(view);
                }
            });
            LayoutParams layoutParams2 = new LayoutParams(-2, -1);
            layoutParams2.addRule(6, imageView.getId());
            layoutParams2.addRule(8, imageView.getId());
            layoutParams2.addRule(21);
            addView(textW, layoutParams2);
            TextW textW2 = new TextW(context);
            textW2.setId(101);
            textW2.setupText(600, 8.0f);
            textW2.setText(R.string.favorites);
            LayoutParams layoutParams3 = new LayoutParams(-1, -2);
            layoutParams3.addRule(3, imageView.getId());
            layoutParams3.setMargins(widthScreen, 0, 0, widthScreen);
            addView(textW2, layoutParams3);
            TextW textW3 = new TextW(context);
            this.tvEmpty = textW3;
            textW3.setupText(400, 3.8f);
            textW3.setText(R.string.empty_fav);
            textW3.setGravity(17);
            textW3.setTextColor(Color.parseColor("#aaaaaa"));
            LayoutParams layoutParams4 = new LayoutParams(-1, -1);
            layoutParams4.setMargins(widthScreen, 0, widthScreen, 0);
            layoutParams4.addRule(3, textW2.getId());
            addView(textW3, layoutParams4);
            SwipeMenuRecyclerView swipeMenuRecyclerView = new SwipeMenuRecyclerView(context);
            swipeMenuRecyclerView.setAdapter(adapterFav);
            swipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(context, 1, false));
            LayoutParams layoutParams5 = new LayoutParams(-1, -1);
            layoutParams5.addRule(3, textW2.getId());
            addView(swipeMenuRecyclerView, layoutParams5);
            if (theme) {
                textW2.setTextColor(-16777216);
                setBackgroundColor(-1);
            } else {
                textW2.setTextColor(-1);
                setBackgroundColor(Color.parseColor("#2C2C2C"));
            }
            checkList();
            updateEdit();
        }



        public  void m116x8b7ec51e(View view) {
            if (FragmentFavorites.this.getActivity() instanceof ActivityHome) {
                ((ActivityHome) FragmentFavorites.this.getActivity()).showLayoutContact(new FavResult() { 
                    @Override 
                    public final void onFavResult(ItemFavorites itemFavorites) {
                        ViewFavorites.this.onAddFav(itemFavorites);
                    }
                });
            }
        }



        public  void m117x8c4d439f(View view) {
            AdapterFav adapterFav = this.adapterFav;
            adapterFav.setChoose(!adapterFav.isChoose());
            updateEdit();
        }

        private void checkList() {
            if (this.tvEmpty == null) {
                return;
            }
            if (this.arrFav.isEmpty()) {
                this.tvEmpty.setVisibility(View.VISIBLE);
                this.tvEdit.setVisibility(View.INVISIBLE);
                return;
            }
            this.tvEmpty.setVisibility(View.GONE);
            this.tvEdit.setVisibility(View.VISIBLE);
        }

        private void updateEdit() {
            if (this.adapterFav.isChoose()) {
                this.tvEdit.setText(R.string.done);
            } else {
                this.tvEdit.setText(R.string.edit);
            }
        }


        public void makeArr() {
            this.arrFav.clear();
            this.arrFav.addAll(MyShare.getFav(getContext()));
            checkList();
            if (this.arrFav.isEmpty()) {
                AdapterFav adapterFav = this.adapterFav;
                if (adapterFav != null) {
                    adapterFav.notifyDataSetChanged();
                    return;
                }
                return;
            }
            Iterator<ItemContact> it = FragmentFavorites.this.arrAllContact.iterator();
            while (it.hasNext()) {
                ItemContact next = it.next();
                Iterator<ItemFavorites> it2 = this.arrFav.iterator();
                while (it2.hasNext()) {
                    ItemFavorites next2 = it2.next();
                    if (next2.id.equals(next.getId())) {
                        next2.name = next.getName();
                        next2.photo = next.getPhoto();
                    }
                }
            }
            AdapterFav adapterFav2 = this.adapterFav;
            if (adapterFav2 != null) {
                adapterFav2.notifyDataSetChanged();
            }
        }


        public void onAddFav(ItemFavorites itemFavorites) {
            Iterator<ItemFavorites> it = this.arrFav.iterator();
            while (it.hasNext()) {
                ItemFavorites next = it.next();
                if (next.type == itemFavorites.type && next.number.equals(itemFavorites.number)) {
                    return;
                }
            }
            Iterator<ItemContact> it2 = FragmentFavorites.this.arrAllContact.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                ItemContact next2 = it2.next();
                if (next2.getId().equals(itemFavorites.id)) {
                    itemFavorites.name = next2.getName();
                    itemFavorites.photo = next2.getPhoto();
                    break;
                }
            }
            this.arrFav.add(itemFavorites);
            this.adapterFav.notifyItemInserted(this.arrFav.size() - 1);
            updateDatabase();
            checkList();
        }

        @Override 
        public void onLongClick(ItemFavorites itemFavorites) {
            Iterator<ItemContact> it = FragmentFavorites.this.arrAllContact.iterator();
            while (it.hasNext()) {
                ItemContact next = it.next();
                if (itemFavorites.id.equals(next.getId())) {
                    new DialogAddFav(getContext(), this.theme, false, next, new FavResult() { 
                        @Override 
                        public final void onFavResult(ItemFavorites itemFavorites2) {
                            ViewFavorites.this.onItemClick(itemFavorites2);
                        }
                    }).show();
                    return;
                }
            }
        }

        @Override 
        public void onItemClick(ItemFavorites itemFavorites) {
            PhoneAccountHandle phoneAccountHandle;
            if (itemFavorites.type == 0) {
                OtherUtils.sendMessage(getContext(), itemFavorites.number);
            } else if (this.arrSim.size() > 0) {
                if (this.posSim < this.arrSim.size()) {
                    phoneAccountHandle = this.arrSim.get(this.posSim).handle;
                } else {
                    phoneAccountHandle = this.arrSim.get(0).handle;
                }
                OtherUtils.call(getContext(), itemFavorites.number, phoneAccountHandle);
            }
        }

        @Override 
        public void onDel(ItemFavorites itemFavorites) {
            int indexOf = this.arrFav.indexOf(itemFavorites);
            this.arrFav.remove(itemFavorites);
            this.adapterFav.notifyItemRemoved(indexOf);
            updateDatabase();
            checkList();
        }

        @Override 
        public void onInfo(ItemFavorites itemFavorites) {
            if (FragmentFavorites.this.getActivity() instanceof ActivityHome) {
                ItemContact itemContact = null;
                Iterator<ItemContact> it = FragmentFavorites.this.arrAllContact.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    ItemContact next = it.next();
                    if (itemFavorites.id.equals(next.getId())) {
                        itemContact = next;
                        break;
                    }
                }
                if (itemContact == null) {
                    return;
                }
                FragmentInfo newInstance = FragmentInfo.newInstance(itemContact, R.string.favorites);
                newInstance.setContactResult(FragmentFavorites.this.contactResult);
                ((ActivityHome) FragmentFavorites.this.getActivity()).showFragment(newInstance, true);
            }
        }

        private void updateDatabase() {
            ArrayList arrayList = new ArrayList();
            Iterator<ItemFavorites> it = this.arrFav.iterator();
            while (it.hasNext()) {
                arrayList.add(new ItemFavorites(it.next()));
            }
            MyShare.putFav(getContext(), arrayList);
        }
    }
}
