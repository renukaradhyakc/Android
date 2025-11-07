package com.thelinkphone.app.dialog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.cardview.widget.CardView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.custom.ViewItemNumber;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemFavorites;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.utils.OtherUtils;



public class DialogAddFav extends BaseDialog implements ViewItemNumber.OnItemNumberClick {
    private final FavResult favResult;
    private boolean isOpen;
    private final ItemContact itemContact;
    private final ItemFavorites itemFavorites;
    private LinearLayout llNumber;
    private final boolean showContent;
    private final boolean theme;
    private TextW tvTitle;
    private ViewItemDialog vCall;
    private View vD1;
    private View vD2;
    private ViewItemDialog vMessage;

    
    public static  void lambda$onCreate$1(View view) {
    }

    @Override 
    public void onLongClickNumber(ItemPhone itemPhone) {
    }

    public DialogAddFav(Context context, boolean z, ItemContact itemContact, FavResult favResult) {
        super(context);
        this.itemContact = itemContact;
        this.favResult = favResult;
        this.theme = z;
        setCancelable(true);
        ItemFavorites itemFavorites = new ItemFavorites();
        this.itemFavorites = itemFavorites;
        itemFavorites.id = itemContact.getId();
        this.showContent = true;
    }

    public DialogAddFav(Context context, boolean z, boolean z2, ItemContact itemContact, FavResult favResult) {
        super(context);
        this.itemContact = itemContact;
        this.favResult = favResult;
        this.theme = z;
        setCancelable(true);
        ItemFavorites itemFavorites = new ItemFavorites();
        this.itemFavorites = itemFavorites;
        itemFavorites.id = itemContact.getId();
        this.showContent = z2;
    }

    @Override 
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        int i = widthScreen / 25;
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setGravity(17);
        linearLayout.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DialogAddFav.this.m98x9ee8102c(view);
            }
        });
        CardView cardView = new CardView(getContext());
        cardView.setRadius(i);
        cardView.setCardElevation(i * 3);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) ((widthScreen * 6.5f) / 10.0f), -2);
        int i2 = (widthScreen * 3) / 10;
        layoutParams.setMargins(i2, i2, i2, i2);
        linearLayout.addView(cardView, layoutParams);
        LinearLayout linearLayout2 = new LinearLayout(getContext());
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setOnClickListener(DialogAddFav$$ExternalSyntheticLambda3.INSTANCE);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.enableTransitionType(4);
        layoutTransition.enableTransitionType(2);
        layoutTransition.enableTransitionType(0);
        layoutTransition.enableTransitionType(3);
        layoutTransition.enableTransitionType(1);
        layoutTransition.setDuration(350L);
        linearLayout2.setLayoutTransition(layoutTransition);
        cardView.addView(linearLayout2, -1, -2);
        setContentView(linearLayout);
        TextW textW = new TextW(getContext());
        this.tvTitle = textW;
        textW.setupText(400, 3.2f);
        this.tvTitle.setText(R.string.add_fav);
        this.tvTitle.setTextColor(Color.parseColor("#868686"));
        this.tvTitle.setPadding(i * 2, i, i, i);
        linearLayout2.addView(this.tvTitle, -1, -2);
        this.vD1 = makeDivider(linearLayout2);
        if (!this.showContent) {
            this.tvTitle.setVisibility(View.GONE);
            this.vD1.setVisibility(View.GONE);
        }
        ViewItemDialog viewItemDialog = new ViewItemDialog(getContext());
        this.vMessage = viewItemDialog;
        viewItemDialog.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DialogAddFav.this.m99xa154b5ea(view);
            }
        });
        this.vMessage.setData(R.drawable.ic_message_fav, R.string.message_up);
        linearLayout2.addView(this.vMessage, -1, -2);
        this.vD2 = makeDivider(linearLayout2);
        ViewItemDialog viewItemDialog2 = new ViewItemDialog(getContext());
        this.vCall = viewItemDialog2;
        viewItemDialog2.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DialogAddFav.this.m100xa28b08c9(view);
            }
        });
        this.vCall.setData(R.drawable.ic_call_fav, R.string.call_up);
        linearLayout2.addView(this.vCall, -1, -2);
        LinearLayout linearLayout3 = new LinearLayout(getContext());
        this.llNumber = linearLayout3;
        linearLayout3.setOrientation(LinearLayout.VERTICAL);
        this.llNumber.setVisibility(View.GONE);
        linearLayout2.addView(this.llNumber);
        makeDivider(this.llNumber);
        if (this.itemContact.getArrPhone().size() > 0) {
            for (int i3 = 0; i3 < this.itemContact.getArrPhone().size(); i3++) {
                ViewItemNumber viewItemNumber = new ViewItemNumber(getContext());
                viewItemNumber.setNumberInDialog(this.itemContact.getArrPhone().get(i3), this.theme, this);
                this.llNumber.addView(viewItemNumber, -1, -2);
                if (i3 < this.itemContact.getArrPhone().size() - 1) {
                    makeDivider(this.llNumber);
                }
            }
        }
        if (this.theme) {
            cardView.setCardBackgroundColor(-1);
        } else {
            cardView.setCardBackgroundColor(Color.parseColor("#424141"));
        }
    }

    
    
    public  void m98x9ee8102c(View view) {
        cancel();
    }

    
    
    public  void m99xa154b5ea(View view) {
        onMessageClick();
    }

    
    
    public  void m100xa28b08c9(View view) {
        onCallClick();
    }

    private View makeDivider(LinearLayout linearLayout) {
        View view = new View(getContext());
        if (this.theme) {
            view.setBackgroundColor(Color.parseColor("#b5b5b6"));
        } else {
            view.setBackgroundColor(Color.parseColor("#5c5c5c"));
        }
        linearLayout.addView(view, -1, 1);
        return view;
    }

    private void onMessageClick() {
        if (!this.isOpen) {
            this.itemFavorites.type = 0;
            this.isOpen = true;
            this.vMessage.setStatus(true);
            this.llNumber.setVisibility(View.VISIBLE);
            this.vCall.setVisibility(View.GONE);
            if (this.showContent) {
                this.tvTitle.setVisibility(View.GONE);
                this.vD1.setVisibility(View.GONE);
            }
            this.vD2.setVisibility(View.GONE);
            return;
        }
        this.isOpen = false;
        this.vMessage.setStatus(false);
        this.vCall.setVisibility(View.VISIBLE);
        if (this.showContent) {
            this.tvTitle.setVisibility(View.VISIBLE);
            this.vD1.setVisibility(View.VISIBLE);
        }
        this.vD2.setVisibility(View.VISIBLE);
        this.llNumber.setVisibility(View.GONE);
    }

    private void onCallClick() {
        if (!this.isOpen) {
            this.itemFavorites.type = 1;
            this.isOpen = true;
            this.vCall.setStatus(true);
            this.llNumber.setVisibility(View.VISIBLE);
            this.vMessage.setVisibility(View.GONE);
            if (this.showContent) {
                this.tvTitle.setVisibility(View.GONE);
                this.vD1.setVisibility(View.GONE);
            }
            this.vD2.setVisibility(View.GONE);
            return;
        }
        this.isOpen = false;
        this.vCall.setStatus(false);
        this.vMessage.setVisibility(View.VISIBLE);
        if (this.showContent) {
            this.tvTitle.setVisibility(View.VISIBLE);
            this.vD1.setVisibility(View.VISIBLE);
        }
        this.vD2.setVisibility(View.VISIBLE);
        this.llNumber.setVisibility(View.GONE);
    }

    @Override 
    public void onNumberResult(ItemPhone itemPhone) {
        this.itemFavorites.number = itemPhone.getNumber();
        this.favResult.onFavResult(this.itemFavorites);
        cancel();
    }

    
    
    public class ViewItemDialog extends LinearLayout {
        private final ImageView imNext;
        private final ImageView imType;
        private final TextW tv;

        public ViewItemDialog(Context context) {
            super(context);
            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(16);
            int widthScreen = OtherUtils.getWidthScreen(getContext()) / 25;
            ImageView imageView = new ImageView(context);
            this.imNext = imageView;
            imageView.setImageResource(R.drawable.im_next);
            int i = widthScreen / 2;
            imageView.setPadding(i, widthScreen, i, widthScreen);
            addView(imageView, widthScreen * 2, widthScreen * 3);
            float f = widthScreen;
            imageView.setPivotX(f);
            imageView.setPivotY(1.5f * f);
            TextW textW = new TextW(context);
            this.tv = textW;
            textW.setupText(400, 4.0f);
            addView(textW, new LayoutParams(0, -2, 1.0f));
            ImageView imageView2 = new ImageView(context);
            this.imType = imageView2;
            imageView2.setPadding(widthScreen, 0, widthScreen, 0);
            int i2 = (int) (f * 3.2f);
            addView(imageView2, i2, i2);
            if (DialogAddFav.this.theme) {
                imageView.setColorFilter(-16777216);
                textW.setTextColor(-16777216);
                imageView2.setColorFilter(-16777216);
                return;
            }
            imageView.setColorFilter(-1);
            textW.setTextColor(-1);
            imageView2.setColorFilter(-1);
        }

        public void setData(int i, int i2) {
            this.imType.setImageResource(i);
            this.tv.setText(i2);
        }

        public void setStatus(boolean z) {
            if (z) {
                this.imNext.animate().setDuration(500L).rotation(90.0f).start();
            } else {
                this.imNext.animate().setDuration(500L).rotation(0.0f).start();
            }
        }
    }
}
