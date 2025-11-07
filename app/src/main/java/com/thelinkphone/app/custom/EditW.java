package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.EditText;
import com.thelinkphone.app.utils.OtherUtils;


public class EditW extends EditText {
    public EditW(Context context) {
        super(context);
    }

    public EditW(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public EditW(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
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
