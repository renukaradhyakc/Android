package com.thelinkphone.app.custom;

import static androidx.core.util.TypedValueCompat.dpToPx;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.content.ContextCompat;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemRecent;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.utils.CallBlockReason;
import com.thelinkphone.app.utils.CallBlockReasonResolver;
import com.thelinkphone.app.utils.CallDisplayMode;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.SearchHighlightUtils;


public class LayoutRecent extends RelativeLayout {
    private FavOnItemClick favOnItemClick;
    private ImageView imDel;
    private ImageView imInfo;
    private ImageView imSim;
    private ImageView imStatus;
    private TextW tvName;
    private TextW tvStatus;
    private TextW tvTime;
    private ImageView imCallalinkBadge;
    private Runnable pendingBadgeSpinRunnable;
    private ObjectAnimator badgeSpinAnimator;

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
        this.tvName.setSingleLine(true);
        this.tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setId(955);
        TextW textW2 = new TextW(context);
        this.tvTime = textW2;
        textW2.setId(140);
        this.tvTime.setupText(400, 3.6f);
        this.tvTime.setTextColor(Color.parseColor("#a8a8a8"));
        this.tvTime.setGravity(16);
        this.tvTime.setPadding(widthScreen, 0, 0, 0);
        int badgeSize = (int) (widthScreen * 1.5f);
        ImageView imCallalinkBadge = new ImageView(context);
        this.imCallalinkBadge = imCallalinkBadge;
        imCallalinkBadge.setId(157);
        imCallalinkBadge.setImageResource(R.drawable.ic_callalink_user_badge);
        imCallalinkBadge.setVisibility(View.GONE);
        imCallalinkBadge.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        imCallalinkBadge.setClipToOutline(true);
        imCallalinkBadge.setElevation(dpToPx(8));
        LayoutParams layoutParamsBadge = new LayoutParams(badgeSize, badgeSize);
        layoutParamsBadge.addRule(RelativeLayout.ALIGN_START, this.tvName.getId());
        layoutParamsBadge.addRule(RelativeLayout.ALIGN_TOP, this.tvName.getId());
        addView(this.imCallalinkBadge, layoutParamsBadge);
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

    public void applySearchHighlight(String query) {
        CharSequence current = this.tvName.getText();
        if (current != null) {
            this.tvName.setText(SearchHighlightUtils.highlight(current.toString(), query));
        }
    }

    public  void m86x49aaf150(View view) {
        this.favOnItemClick.onDel();
    }



    public  void m87x4ae1442f(View view) {
        this.favOnItemClick.onInfo();
    }

    public void setItemRecent(ItemRecentGroup itemRecentGroup, int i, boolean z, boolean z2, int displayMode, CallBlockReasonResolver resolver) {
        if (this.pendingBadgeSpinRunnable != null) {
            this.imCallalinkBadge.removeCallbacks(this.pendingBadgeSpinRunnable);
            this.pendingBadgeSpinRunnable = null;
        }
        if (this.badgeSpinAnimator != null) {
            this.badgeSpinAnimator.cancel();
            this.badgeSpinAnimator = null;
        }
        this.imCallalinkBadge.setRotation(0f);

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
        int targetType = (displayMode == CallDisplayMode.MISSED) ? 3
                : (displayMode == CallDisplayMode.BLOCKED) ? 6 : -1;
//        Log.d("ROW_BIND", "name=" + itemRecentGroup.name + " type=" + i2);

        ItemRecent matchedEntry = itemRecentGroup.arrRecent.get(0);
        if (targetType != -1 & i2 != targetType) {
            for (int idx = 0; idx < itemRecentGroup.arrRecent.size(); idx++) {
                if (itemRecentGroup.arrRecent.get(idx).type == targetType) {
                    i2 = targetType;
                    matchedEntry = itemRecentGroup.arrRecent.get(idx);
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

            case 5: // Rejected
                this.imStatus.setImageResource(R.drawable.ic_status_missed_call);
                break;

            case 6: // Blocked
                int reason = resolver != null ? resolver.getReason(matchedEntry.number, matchedEntry.time) : CallBlockReason.NONE;
                if (reason == CallBlockReason.OUTSIDE_SCHEDULE || reason == CallBlockReason.API_FAILURE) {
                    this.imStatus.setImageResource(R.drawable.ic_outside_schedule);
                } else {
                    this.imStatus.setImageResource(R.drawable.ic_call_block);
                }
                break;

            default:
                this.imStatus.setImageResource(0);
                break;
        }
        if (i2 == 3 || i2 == 6 || i2==5) {
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
        if (matchedEntry.repeatCount >= 2) {
            str = str + " (" + matchedEntry.repeatCount + ")";
        }
        this.tvName.setText(str);
        if (itemRecentGroup.nameType != null && !itemRecentGroup.nameType.isEmpty()) {
            this.tvStatus.setText(itemRecentGroup.nameType);
        } else {
            String str2 = itemRecentGroup.country;
            this.tvStatus.setText(str2 != null ? str2 : "");
        }
        long displayTime = itemRecentGroup.time;
        if (targetType != -1) {
            displayTime = matchedEntry.time;
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
//        Log.d("ROW_BIND", "setItemRecent = " + (System.currentTimeMillis() - start) + " ms");

        if (itemRecentGroup.isCallalinkUser) {
            this.imCallalinkBadge.setVisibility(View.VISIBLE);

            if (itemRecentGroup.badgeAnimationPlayed) {
                this.imCallalinkBadge.setImageResource(R.drawable.ic_verified);
            } else {
                this.imCallalinkBadge.setImageResource(R.drawable.ic_callalink_user_badge);

                final ItemRecentGroup groupRef = itemRecentGroup;
                Runnable spinRunnable = new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator spin = ObjectAnimator.ofFloat(
                                LayoutRecent.this.imCallalinkBadge, View.ROTATION, 0f, 360f);
                        spin.setDuration(600);
                        spin.setInterpolator(new AccelerateDecelerateInterpolator());
                        spin.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            boolean swapped = false;
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                if (!swapped && animation.getAnimatedFraction() >= 0.5f) {
                                    swapped = true;
                                    LayoutRecent.this.imCallalinkBadge.setImageResource(
                                            R.drawable.ic_verified);
                                }
                            }
                        });
                        LayoutRecent.this.badgeSpinAnimator = spin;
                        spin.start();
                        groupRef.badgeAnimationPlayed = true;
                    }
                };
                this.pendingBadgeSpinRunnable = spinRunnable;
                this.imCallalinkBadge.postDelayed(spinRunnable, 1000);
            }

            final String finalStr = str;
            this.tvName.post(new Runnable() {
                @Override
                public void run() {
                    int nameBoxWidthPx = LayoutRecent.this.tvName.getWidth();
                    if (nameBoxWidthPx <= 0) {
                        nameBoxWidthPx = (int) (OtherUtils.getWidthScreen(getContext()) * 0.55f);
                    }

                    int badgeSizePx = (int) (OtherUtils.getWidthScreen(getContext()) / 25f * 1.6f);
                    int gapPx = OtherUtils.getWidthScreen(getContext()) / 25 / 3;
                    int reservedRightPx = badgeSizePx + gapPx;

                    LayoutRecent.this.tvName.setPadding(
                            LayoutRecent.this.tvName.getPaddingLeft(),
                            LayoutRecent.this.tvName.getPaddingTop(),
                            reservedRightPx,
                            LayoutRecent.this.tvName.getPaddingBottom());

                    int availableContentWidthPx = Math.max(0, nameBoxWidthPx - reservedRightPx);
                    float fullTextWidthPx = LayoutRecent.this.tvName.getPaint().measureText(finalStr);
                    int badgeLeftMargin = LayoutRecent.this.tvName.getPaddingLeft()
                            + (int) Math.min(fullTextWidthPx, availableContentWidthPx) + gapPx;

                    int paddingTopPx = LayoutRecent.this.tvName.getPaddingTop();
                    Paint.FontMetrics fm = LayoutRecent.this.tvName.getPaint().getFontMetrics();
                    int textCenterFromTopPx = (int) ((fm.descent - fm.ascent) / 2f);
                    int badgeTopMargin = paddingTopPx + textCenterFromTopPx - (badgeSizePx / 2);

                    LayoutParams badgeParams = (LayoutParams) LayoutRecent.this.imCallalinkBadge.getLayoutParams();
                    badgeParams.leftMargin = badgeLeftMargin;
                    badgeParams.topMargin = badgeTopMargin;
                    LayoutRecent.this.imCallalinkBadge.setLayoutParams(badgeParams);
                }
            });

        } else {
            this.imCallalinkBadge.setVisibility(View.GONE);
            this.tvName.setPadding(this.tvName.getPaddingLeft(), this.tvName.getPaddingTop(),
                    0, this.tvName.getPaddingBottom());
        }
    }

    private int dpToPx(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
