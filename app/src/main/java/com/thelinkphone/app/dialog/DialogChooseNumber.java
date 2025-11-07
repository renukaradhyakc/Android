package com.thelinkphone.app.dialog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import com.thelinkphone.app.custom.ViewItemNumber;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.utils.OtherUtils;


public class DialogChooseNumber extends BaseDialog implements ViewItemNumber.OnItemNumberClick {
    private final NumberContactResult contactResult;
    private final ItemContact itemContact;
    private final boolean theme;


    public interface NumberContactResult {
        void onNumberResult(ItemPhone itemPhone);
    }

    @Override 
    public void onLongClickNumber(ItemPhone itemPhone) {
    }

    public DialogChooseNumber(Context context, ItemContact itemContact, boolean z, NumberContactResult numberContactResult) {
        super(context);
        this.itemContact = itemContact;
        this.theme = z;
        this.contactResult = numberContactResult;
        setCancelable(true);
    }

    @Override 
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setGravity(17);
        LinearLayout linearLayout2 = new LinearLayout(getContext());
        linearLayout2.setLayoutTransition(new LayoutTransition());
        linearLayout2.setOrientation(1);
        linearLayout2.setGravity(1);
        linearLayout.addView(linearLayout2, (widthScreen * 7) / 10, -2);
        setContentView(linearLayout);
        for (int i = 0; i < this.itemContact.getArrPhone().size(); i++) {
            ViewItemNumber viewItemNumber = new ViewItemNumber(getContext());
            viewItemNumber.setNumber(this.itemContact.getArrPhone().get(i), null, this.theme, this);
            linearLayout2.addView(viewItemNumber, -1, -2);
            if (i < this.itemContact.getArrPhone().size() - 1) {
                View view = new View(getContext());
                if (this.theme) {
                    view.setBackgroundColor(Color.parseColor("#dedede"));
                } else {
                    view.setBackgroundColor(Color.parseColor("#5c5c5c"));
                }
                linearLayout2.addView(view, -1, 1);
            }
        }
        if (this.theme) {
            linearLayout2.setBackground(OtherUtils.bgIcon(-1, (widthScreen * 4.0f) / 100.0f));
        } else {
            linearLayout2.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), (widthScreen * 4.0f) / 100.0f));
        }
    }

    @Override 
    public void onNumberResult(ItemPhone itemPhone) {
        this.contactResult.onNumberResult(itemPhone);
    }
}
