package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.utils.OtherUtils;



public class LayoutRecent extends RelativeLayout {
    private FavOnItemClick favOnItemClick;
    private ImageView imDel;
    private ImageView imInfo;
    private ImageView imSim;
    private ImageView imStatus;
    private TextW tvName;
    private TextW tvStatus;
    private TextW tvTime;

    public void setFavOnItemClick(FavOnItemClick favOnItemClick) {
        this.favOnItemClick = favOnItemClick;
    }

    public LayoutRecent(Context context) {
        super(context);
        init(context);
    }

    public LayoutRecent(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public LayoutRecent(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        int widthScreen = OtherUtils.getWidthScreen(context) / 25;
        int i = (int) (widthScreen * 3.2f);
        setOnLongClickListener(new OnLongClickListener() { 
            @Override 
            public final boolean onLongClick(View view) {
                return LayoutRecent.this.m85x48749e71(view);
            }
        });
        TextW textW = new TextW(context);
        this.tvName = textW;
        textW.setId(150);
        this.tvName.setupText(600, 4.2f);
        int i2 = widthScreen / 2;
        this.tvName.setPadding(0, i2, 0, 0);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setId(955);
        TextW textW2 = new TextW(context);
        this.tvTime = textW2;
        textW2.setId(140);
        this.tvTime.setupText(400, 3.6f);
        this.tvTime.setTextColor(Color.parseColor("#a8a8a8"));
        this.tvTime.setGravity(16);
        this.tvTime.setPadding(widthScreen, 0, 0, 0);
        View view = new View(context);
        view.setId(152);
        view.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams = new LayoutParams(-1, 1);
        layoutParams.addRule(3, relativeLayout.getId());
        layoutParams.setMargins(i, i2, 0, 0);
        addView(view, layoutParams);
        ImageView imageView = new ImageView(context);
        this.imStatus = imageView;
        imageView.setPadding(0, (widthScreen * 75) / 100, 0, widthScreen / 8);
        LayoutParams layoutParams2 = new LayoutParams(i, -1);
        layoutParams2.addRule(6, this.tvName.getId());
        layoutParams2.addRule(8, this.tvName.getId());
        addView(this.imStatus, layoutParams2);
        ImageView imageView2 = new ImageView(context);
        this.imDel = imageView2;
        imageView2.setId(153);
        this.imDel.setImageResource(R.drawable.ic_del);
        this.imDel.setPadding(widthScreen, 0, widthScreen, 0);
        this.imDel.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view2) {
                LayoutRecent.this.m86x49aaf150(view2);
            }
        });
        LayoutParams layoutParams3 = new LayoutParams(i, -1);
        layoutParams3.addRule(8, view.getId());
        layoutParams3.addRule(6, this.tvName.getId());
        addView(this.imDel, layoutParams3);
        ImageView imageView3 = new ImageView(context);
        this.imInfo = imageView3;
        imageView3.setId(154);
        this.imInfo.setImageResource(R.drawable.ic_info_fav);
        this.imInfo.setPadding(i2, 0, widthScreen, 0);
        this.imInfo.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view2) {
                LayoutRecent.this.m87x4ae1442f(view2);
            }
        });
        LayoutParams layoutParams4 = new LayoutParams(i - (widthScreen / 4), -1);
        layoutParams4.addRule(6, this.imDel.getId());
        layoutParams4.addRule(8, this.imDel.getId());
        layoutParams4.addRule(21);
        addView(this.imInfo, layoutParams4);
        LayoutParams layoutParams5 = new LayoutParams(-2, -1);
        layoutParams5.addRule(16, this.imInfo.getId());
        layoutParams5.addRule(6, this.imDel.getId());
        layoutParams5.addRule(8, this.imDel.getId());
        addView(this.tvTime, layoutParams5);
        LayoutParams layoutParams6 = new LayoutParams(-1, -2);
        layoutParams6.addRule(17, this.imDel.getId());
        layoutParams6.addRule(16, this.tvTime.getId());
        addView(this.tvName, layoutParams6);
        LayoutParams layoutParams7 = new LayoutParams(-1, -2);
        layoutParams7.addRule(3, this.tvName.getId());
        layoutParams7.addRule(18, this.tvName.getId());
        layoutParams7.addRule(19, this.tvName.getId());
        addView(relativeLayout, layoutParams7);
        TextW textW3 = new TextW(context);
        this.tvStatus = textW3;
        textW3.setId(151);
        this.tvStatus.setupText(400, 3.4f);
        this.tvStatus.setTextColor(Color.parseColor("#a8a8a8"));
        int i3 = widthScreen / 10;
        ImageView imageView4 = new ImageView(context);
        this.imSim = imageView4;
        imageView4.setId(655);
        this.imSim.setPadding(i3, i3, i3, i3);
        LayoutParams layoutParams8 = new LayoutParams(widthScreen, -1);
        layoutParams8.addRule(6, this.tvStatus.getId());
        layoutParams8.addRule(8, this.tvStatus.getId());
        layoutParams8.setMargins(0, 0, widthScreen / 6, 0);
        relativeLayout.addView(this.imSim, layoutParams8);
        LayoutParams layoutParams9 = new LayoutParams(-1, -2);
        layoutParams9.addRule(17, this.imSim.getId());
        relativeLayout.addView(this.tvStatus, layoutParams9);
    }

    
    
    public  boolean m85x48749e71(View view) {
        this.favOnItemClick.onLongClick();
        return true;
    }

    
    
    public  void m86x49aaf150(View view) {
        this.favOnItemClick.onDel();
    }

    
    
    public  void m87x4ae1442f(View view) {
        this.favOnItemClick.onInfo();
    }

    public void setItemRecent(ItemRecentGroup itemRecentGroup, int i, boolean z, boolean z2, boolean missedOnly) {
        long start = System.currentTimeMillis();
        if (z) {
            this.imDel.setVisibility(View.VISIBLE);
            this.imInfo.setVisibility(View.INVISIBLE);
            this.imStatus.setVisibility(View.GONE);
        } else {
            this.imDel.setVisibility(View.INVISIBLE);
            this.imInfo.setVisibility(View.VISIBLE);
            this.imStatus.setVisibility(View.VISIBLE);
        }
        int i2 = itemRecentGroup.arrRecent.get(0).type;
        if (missedOnly & i2 != 3) {
            for (int idx = 0; idx < itemRecentGroup.arrRecent.size(); idx++) {
                if (itemRecentGroup.arrRecent.get(idx).type == 3) {
                    i2 = 3;
                    break;
                }
            }
        }
//        if (i2 == 2) {
//            this.imStatus.setImageResource(R.drawable.ic_status_out_call);
//        } else {
//            this.imStatus.setImageResource(0);
//        }
        switch (i2) {

            case 1: // Incoming
                this.imStatus.setImageResource(R.drawable.ic_call_in_info);
                break;

            case 2: // Outgoing
                this.imStatus.setImageResource(R.drawable.ic_call_out_info);
                break;

            case 3: // Missed
                this.imStatus.setImageResource(R.drawable.ic_status_missed_call);
                break;

            default:
                this.imStatus.setImageResource(0);
                break;
        }
        if (i2 == 3) {
            this.tvName.setTextColor(Color.parseColor("#FF2828"));
        } else if (z2) {
            this.tvName.setTextColor(-16777216);
        } else {
            this.tvName.setTextColor(-1);
        }
        String str = itemRecentGroup.name;
        if (str == null || str.isEmpty()) {
            str = itemRecentGroup.arrRecent.get(0).number;
        }
        if (str == null) {
            str = "";
        }
//        if (itemRecentGroup.arrRecent.size() > 1) {
//            str = str + " (" + itemRecentGroup.arrRecent.size() + ")";
//        }
        this.tvName.setText(str);
        if (itemRecentGroup.nameType != null && !itemRecentGroup.nameType.isEmpty()) {
            this.tvStatus.setText(itemRecentGroup.nameType);
        } else {
            String str2 = itemRecentGroup.country;
            this.tvStatus.setText(str2 != null ? str2 : "");
        }
        long displayTime = itemRecentGroup.time;
        if (missedOnly) {
            for (int idx = 0; idx < itemRecentGroup.arrRecent.size(); idx++) {
                if (itemRecentGroup.arrRecent.get(idx).type == 3) {
                    displayTime = itemRecentGroup.arrRecent.get(idx).time;
                    break;
                }
            }
        }
        this.tvTime.setText(OtherUtils.longToTime(getContext(), displayTime));
        if (i == 0) {
            this.imSim.setVisibility(View.VISIBLE);
            this.imSim.setImageResource(R.drawable.ic_num_1);
        } else if (i == 1) {
            this.imSim.setVisibility(View.VISIBLE);
            this.imSim.setImageResource(R.drawable.ic_num_2);
        } else {
            this.imSim.setVisibility(View.GONE);
        }
        Log.d("ROW_BIND", "setItemRecent = " + (System.currentTimeMillis() - start) + " ms");
    }
}
