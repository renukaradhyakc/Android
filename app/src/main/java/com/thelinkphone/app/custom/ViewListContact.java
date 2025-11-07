package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewListContact extends LinearLayout {
    private final TextW tvName;

    public ViewListContact(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        int widthScreen = OtherUtils.getWidthScreen(context) / 25;
        TextW textW = new TextW(context);
        this.tvName = textW;
        textW.setupText(500, 3.8f);
        textW.setTextColor(Color.parseColor("#8A8A8E"));
        textW.setPadding(widthScreen, widthScreen, widthScreen, widthScreen);
        addView(textW, -1, -2);
        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams = new LayoutParams(-1, 1);
        layoutParams.setMargins(widthScreen, 0, widthScreen, 0);
        addView(view, layoutParams);
    }

    public void setContact(ItemContact itemContact, boolean z) {
        if (itemContact.getName() != null) {
            this.tvName.setText(itemContact.getName());
        } else {
            this.tvName.setText("");
        }
        if (z) {
            this.tvName.setTextColor(-16777216);
        } else {
            this.tvName.setTextColor(-1);
        }
    }
}
