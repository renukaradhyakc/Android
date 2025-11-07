package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.RelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.adapter.AdapterContactHome;
import com.thelinkphone.app.dialog.DialogAddFav;
import com.thelinkphone.app.dialog.FavResult;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemFavorites;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;


import java.util.ArrayList;


public class LayoutChooseContact extends RelativeLayout implements AdapterContactHome.ContactResult {
    private AdapterContactHome adapterContact;
    private FavResult favResult;
    private final int height;
    private final RecyclerView rvContact;
    private final boolean theme;
    private View vBg;
    private final ViewSearch viewSearch;
    private final int w;

    public void setFavResult(FavResult favResult) {
        this.favResult = favResult;
    }

    public LayoutChooseContact(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        int heightScreen = OtherUtils.getHeightScreen(context) - ((widthScreen * 13) / 100);
        this.height = heightScreen;
        int i = widthScreen / 25;
        boolean theme = MyShare.getTheme(context);
        this.theme = theme;
        TextW textW = new TextW(context);
        textW.setId(1000);
        textW.setupText(400, 2.8f);
        textW.setGravity(1);
        textW.setText(R.string.title_search_contact);
        int i2 = i / 2;
        textW.setPadding(i, i2, i, 0);
        addView(textW, -1, -2);
        TextW textW2 = new TextW(context);
        textW2.setId(1001);
        textW2.setupText(600, 4.0f);
        textW2.setText(R.string.contacts);
        textW2.setPadding(i, i, i, i);
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.addRule(14);
        layoutParams.addRule(3, textW.getId());
        addView(textW2, layoutParams);
        TextW textW3 = new TextW(context);
        textW3.setupText(400, 4.0f);
        textW3.setPadding(i, i, i, i);
        textW3.setText(R.string.cancel);
        textW3.setTextColor(Color.parseColor("#007AFF"));
        textW3.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutChooseContact.this.m66x4d0dca9(view);
            }
        });
        LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        layoutParams2.addRule(21);
        layoutParams2.addRule(3, textW.getId());
        addView(textW3, layoutParams2);
        ViewSearch viewSearch = new ViewSearch(context);
        this.viewSearch = viewSearch;
        viewSearch.setId(1002);
        viewSearch.setListenerTextChange(new ViewSearch.TextResult() { 
            @Override 
            public final void onTextChange(String str) {
                LayoutChooseContact.this.m67xf86060ea(str);
            }
        });
        LayoutParams layoutParams3 = new LayoutParams(-1, -2);
        layoutParams3.addRule(3, textW2.getId());
        layoutParams3.setMargins(i, 0, i, i2);
        addView(viewSearch, layoutParams3);
        RecyclerView recyclerView = new RecyclerView(context);
        this.rvContact = recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(context, 1, false));
        LayoutParams layoutParams4 = new LayoutParams(-1, -1);
        layoutParams4.addRule(3, viewSearch.getId());
        addView(recyclerView, layoutParams4);
        ViewScrollAlphaB viewScrollAlphaB = new ViewScrollAlphaB(context);
        viewScrollAlphaB.setAlphaBResult(new ViewScrollAlphaB.AlphaBResult() { 
            @Override 
            public final void onAlphaBResult(String str) {
                LayoutChooseContact.this.m68xebefe52b(str);
            }
        });
        LayoutParams layoutParams5 = new LayoutParams((widthScreen * 8) / 100, viewScrollAlphaB.makeHeightView());
        layoutParams5.addRule(15);
        layoutParams5.addRule(21);
        addView(viewScrollAlphaB, layoutParams5);
        if (theme) {
            setBackground(OtherUtils.bgMain(-1, widthScreen / 40.0f));
            textW.setTextColor(-16777216);
            textW2.setTextColor(-16777216);
        } else {
            setBackground(OtherUtils.bgMain(Color.parseColor("#2C2C2C"), widthScreen / 40.0f));
            textW.setTextColor(-1);
            textW2.setTextColor(-1);
        }
        setTranslationY(heightScreen);
    }



    public  void m66x4d0dca9(View view) {
        hide();
    }



    public  void m67xf86060ea(String str) {
        AdapterContactHome adapterContactHome = this.adapterContact;
        if (adapterContactHome != null) {
            adapterContactHome.filter(str);
        }
    }



    public  void m68xebefe52b(String str) {
        int locationAlphaB;
        AdapterContactHome adapterContactHome = this.adapterContact;
        if (adapterContactHome == null || (locationAlphaB = adapterContactHome.getLocationAlphaB(str)) == -1) {
            return;
        }
        this.rvContact.scrollToPosition(locationAlphaB);
    }

    public void show(View view, ArrayList<ItemContact> arrayList) {
        this.vBg = view;
        if (this.adapterContact == null) {
            AdapterContactHome adapterContactHome = new AdapterContactHome(arrayList, MyShare.getTheme(getContext()), this);
            this.adapterContact = adapterContactHome;
            this.rvContact.setAdapter(adapterContactHome);
        } else {
            this.viewSearch.clearText();
            this.rvContact.scrollToPosition(0);
        }
        setVisibility(0);
        animate().setDuration(350L).translationY(0.0f).withEndAction(null).start();
        view.setPivotX(this.w / 2.0f);
        view.animate().setDuration(350L).alpha(0.8f).scaleX(0.92f).scaleY(0.92f).translationY(this.w / 50.0f).start();
    }

    public void hide() {
        animate().setDuration(350L).translationY(this.height).withEndAction(new Runnable() { 
            @Override 
            public final void run() {
                LayoutChooseContact.this.m65x41888628();
            }
        }).start();
        this.vBg.animate().setDuration(350L).alpha(1.0f).scaleX(1.0f).scaleY(1.0f).translationY(0.0f).start();
    }



    public  void m65x41888628() {
        setVisibility(8);
    }

    @Override 
    public void onItemClick(ItemContact itemContact) {
        new DialogAddFav(getContext(), this.theme, itemContact, new FavResult() { 
            @Override 
            public final void onFavResult(ItemFavorites itemFavorites) {
                LayoutChooseContact.this.m69x5128cc23(itemFavorites);
            }
        }).show();
    }



    public  void m69x5128cc23(ItemFavorites itemFavorites) {
        hide();
        this.favResult.onFavResult(itemFavorites);
    }

    @Override 
    public void onLongClick(ItemContact itemContact) {
        onItemClick(itemContact);
    }
}
