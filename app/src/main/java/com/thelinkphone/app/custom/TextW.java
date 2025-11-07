package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;
import com.thelinkphone.app.utils.OtherUtils;


public class TextW extends AppCompatTextView {
    public TextW(Context context) {
        super(context);
        setTextColor(-16777216);
    }

    public TextW(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setTextColor(-16777216);
    }

    public TextW(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setTextColor(-16777216);
    }

    public void setupText(int i, float f) {
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        if (Build.VERSION.SDK_INT >= 28) {
            setTypeface(Typeface.create(Typeface.SANS_SERIF, i, false));
        } else {
            setTypeface(Typeface.create(Typeface.SANS_SERIF, 0));
        }
        setTextSize(0, (widthScreen * f) / 100.0f);
    }
}
