package com.thelinkphone.app.custom;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewItemThemeNull extends RelativeLayout {
    public ViewItemThemeNull(Context context) {
        super(context);
        addView(new View(context), -1, (OtherUtils.getWidthScreen(context) * 15) / 100);
    }
}
