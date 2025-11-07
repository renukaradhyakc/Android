package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemFavorites;
import com.thelinkphone.app.utils.OtherUtils;



public class LayoutItemFav extends RelativeLayout {
    private AvatarPeople av;
    private FavOnItemClick favOnItemClick;
    private ImageView imDel;
    private ImageView imInfo;
    private ImageView imType;
    private TextW tvName;
    private TextW tvType;

    public LayoutItemFav(Context context) {
        super(context);
        init(context);
    }

    public LayoutItemFav(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public LayoutItemFav(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    public void setFavOnItemClick(FavOnItemClick favOnItemClick) {
        this.favOnItemClick = favOnItemClick;
    }

    private void init(Context context) {
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 25;
        float f = i;
        int i2 = (int) (3.5f * f);
        int i3 = i / 2;
        setPadding(i3, 0, 0, 0);
        setOnLongClickListener(new OnLongClickListener() { 
            @Override 
            public final boolean onLongClick(View view) {
                return LayoutItemFav.this.m70x3bf05b2(view);
            }
        });
        ImageView imageView = new ImageView(context);
        this.imDel = imageView;
        imageView.setId(100);
        this.imDel.setVisibility(View.GONE);
        this.imDel.setImageResource(R.drawable.ic_del);
        this.imDel.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutItemFav.this.m71x29530eb3(view);
            }
        });
        this.imDel.setPadding(i3, 0, i3, 0);
        addView(this.imDel, (int) (f * 2.3f), i2);
        AvatarPeople avatarPeople = new AvatarPeople(context);
        this.av = avatarPeople;
        avatarPeople.setTextSize(5.0f);
        this.av.setId(101);
        int i4 = i2 - i;
        LayoutParams layoutParams = new LayoutParams(i4, i4);
        layoutParams.setMargins(i3, i3, i3, i3);
        layoutParams.addRule(17, this.imDel.getId());
        addView(this.av, layoutParams);
        ImageView imageView2 = new ImageView(context);
        this.imInfo = imageView2;
        imageView2.setId(102);
        this.imInfo.setPadding(i3, i, i3, i);
        this.imInfo.setImageResource(R.drawable.ic_info_fav);
        this.imInfo.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutItemFav.this.m72x4ee717b4(view);
            }
        });
        LayoutParams layoutParams2 = new LayoutParams(i * 3, i2);
        layoutParams2.addRule(21);
        addView(this.imInfo, layoutParams2);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setId(103);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(16);
        linearLayout.setPadding(i3, 0, 0, 0);
        LayoutParams layoutParams3 = new LayoutParams(-1, i2);
        layoutParams3.addRule(17, this.av.getId());
        layoutParams3.addRule(16, this.imInfo.getId());
        addView(linearLayout, layoutParams3);
        TextW textW = new TextW(context);
        this.tvName = textW;
        textW.setupText(500, 4.1f);
        this.tvName.setSingleLine();
        this.tvName.setEllipsize(TextUtils.TruncateAt.END);
        this.tvName.setPadding(0, 0, 0, i / 8);
        linearLayout.addView(this.tvName, -1, -2);
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout2.setGravity(16);
        linearLayout.addView(linearLayout2, -1, -2);
        ImageView imageView3 = new ImageView(context);
        this.imType = imageView3;
        imageView3.setColorFilter(Color.parseColor("#B8B8B8"));
        int i5 = widthScreen / 30;
        linearLayout2.addView(this.imType, i5, i5);
        TextW textW2 = new TextW(context);
        this.tvType = textW2;
        textW2.setupText(400, 2.9f);
        this.tvType.setSingleLine();
        this.tvType.setTextColor(Color.parseColor("#B8B8B8"));
        LayoutParams layoutParams4 = new LayoutParams(-1, -2);
        layoutParams4.setMargins(i / 4, 0, 0, 0);
        linearLayout2.addView(this.tvType, layoutParams4);
        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams5 = new LayoutParams(-1, 1);
        layoutParams5.addRule(17, this.av.getId());
        addView(view, layoutParams5);
        View view2 = new View(context);
        view2.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams6 = new LayoutParams(-1, 1);
        layoutParams6.addRule(17, this.av.getId());
        layoutParams6.addRule(8, linearLayout.getId());
        addView(view2, layoutParams6);
    }

    
    
    public  boolean m70x3bf05b2(View view) {
        this.favOnItemClick.onLongClick();
        return true;
    }

    
    
    public  void m71x29530eb3(View view) {
        this.favOnItemClick.onDel();
    }

    
    
    public  void m72x4ee717b4(View view) {
        this.favOnItemClick.onInfo();
    }

    public void setFav(ItemFavorites itemFavorites, boolean z, boolean z2) {
        String str;
        this.av.setImage(itemFavorites.photo, itemFavorites.name);
        if (itemFavorites.name != null) {
            str = itemFavorites.name;
        } else {
            str = itemFavorites.number != null ? itemFavorites.number : null;
        }
        if (str == null) {
            str = "";
        }
        this.tvName.setText(str);
        if (itemFavorites.type == 0) {
            this.tvType.setText(R.string.message_up);
            this.imType.setImageResource(R.drawable.ic_message);
        } else {
            this.imType.setImageResource(R.drawable.ic_call_info);
            this.tvType.setText(R.string.call_up);
        }
        if (z) {
            this.imDel.setVisibility(View.VISIBLE);
            this.imInfo.setVisibility(View.GONE);
        } else {
            this.imDel.setVisibility(View.GONE);
            this.imInfo.setVisibility(View.VISIBLE);
        }
        if (z2) {
            this.tvName.setTextColor(-16777216);
        } else {
            this.tvName.setTextColor(-1);
        }
    }
}
