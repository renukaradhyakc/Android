package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.utils.OtherUtils;



public class LayoutItemBlock extends RelativeLayout {
    private final AvatarPeople av;
    private final ImageView imDel;
    private final TextW tvName;
    private final TextW tvNumber;

    public LayoutItemBlock(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 25;
        int i2 = widthScreen / 10;
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setId(123);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(16);
        addView(linearLayout, -1, -2);
        AvatarPeople avatarPeople = new AvatarPeople(context);
        this.av = avatarPeople;
        avatarPeople.setTextSize(5.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i2, i2);
        layoutParams.setMargins(i, i, i, i);
        linearLayout.addView(avatarPeople, layoutParams);
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(linearLayout2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextW textW = new TextW(context);
        this.tvName = textW;
        textW.setupText(600, 4.4f);
        textW.setSingleLine();
        textW.setEllipsize(TextUtils.TruncateAt.END);
        linearLayout2.addView(textW, -1, -2);
        TextW textW2 = new TextW(context);
        this.tvNumber = textW2;
        textW2.setupText(400, 3.2f);
        textW2.setSingleLine();
        textW2.setEllipsize(TextUtils.TruncateAt.END);
        textW2.setTextColor(Color.parseColor("#B8B8B8"));
        textW2.setPadding(0, i / 8, 0, 0);
        linearLayout2.addView(textW2, -1, -2);
        int i3 = (int) (i * 3.5f);
        ImageView imageView = new ImageView(context);
        this.imDel = imageView;
        imageView.setImageResource(R.drawable.ic_del);
        imageView.setPadding(i, i, i, i);
        linearLayout.addView(imageView, i3, i3);
        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams2 = new LayoutParams(-1, 1);
        int i4 = i2 + (i * 2);
        layoutParams2.setMargins(i4, 0, 0, 0);
        addView(view, layoutParams2);
        View view2 = new View(context);
        view2.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams3 = new LayoutParams(-1, 1);
        layoutParams3.setMargins(i4, 0, 0, 0);
        layoutParams3.addRule(3, linearLayout.getId());
        addView(view2, layoutParams3);
    }

    public ImageView getImDel() {
        return this.imDel;
    }

    public void setContact(ItemContact itemContact, boolean z) {
        this.av.setImage(itemContact.getPhoto(), itemContact.getName());
        if (itemContact.getName() != null && !itemContact.getName().isEmpty()) {
            this.tvName.setText(itemContact.getName());
        } else {
            this.tvName.setText(R.string.unknown);
        }
        ItemPhone itemPhone = null;
        if (itemContact.getArrPhone() != null && itemContact.getArrPhone().size() > 0) {
            itemPhone = itemContact.getArrPhone().get(0);
        }
        if (itemPhone != null && itemPhone.getNumber() != null && !itemPhone.getNumber().isEmpty()) {
            this.tvNumber.setText(itemPhone.getNumber());
        } else {
            this.tvNumber.setText("");
        }
        if (z) {
            this.tvName.setTextColor(-16777216);
        } else {
            this.tvName.setTextColor(-1);
        }
    }
}
