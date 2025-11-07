package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewItemContact extends RelativeLayout {
    private final AvatarPeople av;
    private final TextW tvName;

    public ViewItemContact(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = (widthScreen * 13) / 100;
        int i2 = widthScreen / 50;
        AvatarPeople avatarPeople = new AvatarPeople(context);
        this.av = avatarPeople;
        avatarPeople.setId(132);
        avatarPeople.setTextSize(6.0f);
        LayoutParams layoutParams = new LayoutParams(i, i);
        int i3 = i2 * 2;
        layoutParams.setMargins(i3, i2, i3, i2);
        addView(avatarPeople, layoutParams);
        TextW textW = new TextW(context);
        this.tvName = textW;
        textW.setSingleLine();
        textW.setGravity(16);
        textW.setupText(450, 4.5f);
        textW.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams layoutParams2 = new LayoutParams(-1, -1);
        layoutParams2.addRule(6, avatarPeople.getId());
        layoutParams2.addRule(8, avatarPeople.getId());
        layoutParams2.addRule(17, avatarPeople.getId());
        layoutParams2.setMargins(0, 0, i2, 0);
        addView(textW, layoutParams2);
        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#CFCFCF"));
        LayoutParams layoutParams3 = new LayoutParams(-1, 2);
        layoutParams3.addRule(3, avatarPeople.getId());
        layoutParams3.addRule(17, avatarPeople.getId());
        addView(view, layoutParams3);
    }

    public void setItemContact(ItemContact itemContact, boolean z) {
        String name = itemContact.getName();
        if (name == null) {
            name = getContext().getString(R.string.unknown);
        }
        this.tvName.setText(name);
        this.av.setImage(itemContact.getPhoto(), name);
        if (z) {
            this.tvName.setTextColor(Color.parseColor("#121212"));
        } else {
            this.tvName.setTextColor(-1);
        }
    }
}
