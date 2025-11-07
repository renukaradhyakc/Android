package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemRecorder;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewItemRecorder extends RelativeLayout {
    private final AvatarPeople avatarPeople;
    private final ImageView imStatus;
    private final TextW tvNum;
    private final TextW tvSize;
    private final TextW tvTime;
    private final View vChoose;

    public ViewItemRecorder(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 25;
        int i2 = widthScreen / 10;
        AvatarPeople avatarPeople = new AvatarPeople(context);
        this.avatarPeople = avatarPeople;
        avatarPeople.setId(120);
        avatarPeople.setTextSize(5.3f);
        LayoutParams layoutParams = new LayoutParams(i2, i2);
        layoutParams.setMargins(i, i, i, i);
        addView(avatarPeople, layoutParams);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setId(121);
        linearLayout.setGravity(16);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        int i3 = i2 + (i * 2);
        LayoutParams layoutParams2 = new LayoutParams(-1, i3);
        layoutParams2.addRule(17, avatarPeople.getId());
        addView(linearLayout, layoutParams2);
        TextW textW = new TextW(context);
        this.tvNum = textW;
        textW.setupText(600, 4.4f);
        textW.setSingleLine();
        textW.setEllipsize(TextUtils.TruncateAt.END);
        textW.setPadding(0, 0, 0, i / 8);
        linearLayout.addView(textW, -1, -2);
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout2.setGravity(16);
        linearLayout.addView(linearLayout2, -1, -2);
        ImageView imageView = new ImageView(context);
        this.imStatus = imageView;
        linearLayout2.addView(imageView, i, i);
        TextW textW2 = new TextW(context);
        this.tvTime = textW2;
        textW2.setTextColor(Color.parseColor("#B8B8B8"));
        textW2.setupText(300, 3.3f);
        textW2.setPadding(i / 2, 0, 0, 0);
        linearLayout2.addView(textW2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextW textW3 = new TextW(context);
        this.tvSize = textW3;
        textW3.setTextColor(Color.parseColor("#B8B8B8"));
        textW3.setupText(300, 3.3f);
        textW3.setPadding(0, 0, i, 0);
        linearLayout2.addView(textW3, -2, -2);
        View view = new View(context);
        this.vChoose = view;
        view.setVisibility(View.GONE);
        LayoutParams layoutParams3 = new LayoutParams(-1, -1);
        layoutParams3.addRule(18, avatarPeople.getId());
        layoutParams3.addRule(6, avatarPeople.getId());
        layoutParams3.addRule(19, avatarPeople.getId());
        layoutParams3.addRule(8, avatarPeople.getId());
        addView(view, layoutParams3);
        View view2 = new View(context);
        view2.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams4 = new LayoutParams(-1, 1);
        layoutParams4.setMargins(i3, 0, 0, 0);
        addView(view2, layoutParams4);
        View view3 = new View(context);
        view3.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams5 = new LayoutParams(-1, 1);
        layoutParams5.setMargins(i3, 0, 0, 0);
        layoutParams5.addRule(3, linearLayout.getId());
        addView(view3, layoutParams5);
    }

    public void setItemRecorder(ItemRecorder itemRecorder, boolean z, boolean z2, boolean z3) {
        String name = itemRecorder.getName();
        if (name == null || name.isEmpty()) {
            name = itemRecorder.getNum();
        }
        this.avatarPeople.setImage(itemRecorder.getUri(), name);
        this.tvNum.setText(name);
        this.tvTime.setText(OtherUtils.longToTime(getContext(), itemRecorder.getTime()));
        this.tvSize.setText(OtherUtils.getReadableFileSize(itemRecorder.getSize()));
        if (itemRecorder.getStatus() == 2) {
            this.imStatus.setImageResource(R.drawable.ic_call_in_info);
        } else {
            this.imStatus.setImageResource(R.drawable.ic_call_out_info);
        }
        if (z) {
            this.vChoose.setVisibility(View.GONE);
            this.avatarPeople.setVisibility(View.VISIBLE);
        } else {
            this.vChoose.setVisibility(View.VISIBLE);
            this.avatarPeople.setVisibility(View.INVISIBLE);
            if (z2) {
                this.vChoose.setBackgroundResource(R.drawable.im_choose_item);
            } else {
                this.vChoose.setBackgroundResource(R.drawable.im_not_choose_item);
            }
        }
        if (z3) {
            this.tvNum.setTextColor(-16777216);
        } else {
            this.tvNum.setTextColor(-1);
        }
    }
}
