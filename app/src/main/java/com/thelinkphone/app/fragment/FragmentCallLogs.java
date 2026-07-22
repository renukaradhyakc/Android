package com.thelinkphone.app.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.fragment.app.Fragment;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.utils.CallBlockReasonResolver;
import com.thelinkphone.app.utils.CallDisplayMode;
import com.thelinkphone.app.utils.CallLogGroupHelper;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FragmentCallLogs extends Fragment {

    private ItemRecentGroup itemRecentGroup;
    private int displayMode;
    private ContactResult contactResult;

    public void setContactResult(ContactResult contactResult) {
        this.contactResult = contactResult;
    }

    public static FragmentCallLogs newInstance(ItemRecentGroup itemRecentGroup, int displayMode) {
        FragmentCallLogs fragment = new FragmentCallLogs();
        Bundle bundle = new Bundle();
        bundle.putString("dataG", new Gson().toJson(itemRecentGroup));
        bundle.putInt("displayMode", displayMode);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) return;
        String json = getArguments().getString("dataG");
        if (json == null || json.isEmpty()) return;
        this.itemRecentGroup = new Gson().fromJson(json, new TypeToken<ItemRecentGroup>() {}.getType());
        this.displayMode = getArguments().getInt("displayMode", CallDisplayMode.ALL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new ViewCallLogs(inflater.getContext());
    }

    public void onBack() {
        try {
            if (contactResult != null) contactResult.onBack();
        } catch (IllegalStateException ignored) {
        }
    }

    private class ViewCallLogs extends RelativeLayout {
        ViewCallLogs(Context context) {
            super(context);
            boolean theme = MyShare.getTheme(context);
            int widthScreen = OtherUtils.getWidthScreen(context);
            int i = widthScreen / 25;
            int i3 = i / 2;
            int statusBarInset = widthScreen / 20;

            ImageView back = new ImageView(context);
            back.setId(View.generateViewId());
            back.setImageResource(R.drawable.ic_back);
            back.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            back.setOnClickListener(v -> onBack());
            LayoutParams backParams = new LayoutParams((int) (i * 1.5f), (int) (i * 1.3f));
            backParams.setMargins(i3, statusBarInset, 0, 0);
            addView(back, backParams);

            TextW tvBack = new TextW(context);
            tvBack.setId(View.generateViewId());
            tvBack.setText(R.string.back);
            tvBack.setGravity(Gravity.CENTER_VERTICAL);
            tvBack.setupText(400, 4.2f);
            tvBack.setOnClickListener(v -> onBack());
            tvBack.setTextColor(Color.parseColor("#007AFF"));
            LayoutParams tvBackParams = new LayoutParams(-2, -1);
            tvBackParams.addRule(6, back.getId());
            tvBackParams.addRule(8, back.getId());
            tvBackParams.addRule(17, back.getId());
            addView(tvBack, tvBackParams);

            TextW title = new TextW(context);
            title.setId(View.generateViewId());
            title.setText(R.string.call_history);
            title.setupText(700, 8.5f);
            title.setGravity(Gravity.CENTER_HORIZONTAL);
            LayoutParams titleParams = new LayoutParams(-1, -2);
            titleParams.addRule(3, back.getId());
            titleParams.setMargins(i, 0, i, i/2);
            addView(title, titleParams);

            View divider = new View(context);
            divider.setId(View.generateViewId());
            divider.setBackgroundColor(theme ? Color.parseColor("#dedede") : Color.parseColor("#5c5c5c"));
            LayoutParams dividerParams = new LayoutParams(-1, 1);
            dividerParams.addRule(3, title.getId());
            dividerParams.setMargins(i, 0, i, i / 6);
            addView(divider, dividerParams);

            ScrollView scrollView = new ScrollView(context);
            scrollView.setFillViewport(true);
            LayoutParams scrollParams = new LayoutParams(-1, -1);
            scrollParams.addRule(14);
            scrollParams.addRule(3, divider.getId());
            addView(scrollView, scrollParams);

            LinearLayout content = new LinearLayout(context);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setGravity(1);
            scrollView.addView(content, -1, -2);

            int cardWidth = (widthScreen * 342) / 360;
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(i3, i3, i3, i);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(cardWidth, -2);
            cardParams.setMargins(0, i/4, 0, i);
            content.addView(card, cardParams);

            if (itemRecentGroup != null) {
                CallBlockReasonResolver resolver = CallBlockReasonResolver.load(context);
                CallLogGroupHelper.addRecentsGroupedByDay(
                        card, context, itemRecentGroup.arrRecent, theme, displayMode, resolver);
            }

            if (theme) {
                setBackgroundColor(Color.parseColor("#F2F2F7"));
                title.setTextColor(-16777216);
            } else {
                setBackgroundColor(Color.parseColor("#2C2C2C"));
                title.setTextColor(-1);
            }
        }
    }
}