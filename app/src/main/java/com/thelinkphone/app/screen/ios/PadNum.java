package com.thelinkphone.app.screen.ios;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.screen.PadResult;
import com.thelinkphone.app.utils.OtherUtils;



public class PadNum extends RelativeLayout implements View.OnClickListener {
    private PadResult padResult;

    public void setPadResult(PadResult padResult) {
        this.padResult = padResult;
    }

    public PadNum(Context context) {
        super(context);
        float widthScreen = OtherUtils.getWidthScreen(context);
        int i = (int) ((21.2f * widthScreen) / 100.0f);
        int i2 = (int) ((5.2f * widthScreen) / 100.0f);
        int i3 = (int) ((4.3f * widthScreen) / 100.0f);
        View view = new View(context);
        view.setId(1);
        LayoutParams layoutParams = new LayoutParams(i, i);
        layoutParams.addRule(14);
        layoutParams.addRule(12);
        layoutParams.setMargins(i2, i3, i2, 0);
        addView(view, layoutParams);
        TextW textW = new TextW(context);
        textW.setId(72);
        textW.setOnClickListener(this);
        textW.setText(R.string.hide);
        textW.setTextColor(-1);
        textW.setTextSize(0, (widthScreen * 4.2f) / 100.0f);
        textW.setGravity(17);
        LayoutParams layoutParams2 = new LayoutParams(i, i);
        layoutParams2.addRule(12);
        layoutParams2.addRule(1, view.getId());
        addView(textW, layoutParams2);
        ImageView imNum = imNum(60);
        LayoutParams layoutParams3 = new LayoutParams(i, i);
        layoutParams3.addRule(14);
        layoutParams3.addRule(2, view.getId());
        addView(imNum, layoutParams3);
        ImageView imNum2 = imNum(70);
        LayoutParams layoutParams4 = new LayoutParams(i, i);
        layoutParams4.addRule(0, view.getId());
        layoutParams4.addRule(6, imNum.getId());
        addView(imNum2, layoutParams4);
        ImageView imNum3 = imNum(71);
        LayoutParams layoutParams5 = new LayoutParams(i, i);
        layoutParams5.addRule(1, view.getId());
        layoutParams5.addRule(6, imNum.getId());
        addView(imNum3, layoutParams5);
        ImageView imNum4 = imNum(68);
        LayoutParams layoutParams6 = new LayoutParams(i, i);
        layoutParams6.addRule(14);
        layoutParams6.addRule(2, imNum.getId());
        layoutParams6.setMargins(0, i3, 0, i3);
        addView(imNum4, layoutParams6);
        ImageView imNum5 = imNum(67);
        LayoutParams layoutParams7 = new LayoutParams(i, i);
        layoutParams7.addRule(6, imNum4.getId());
        layoutParams7.addRule(16, view.getId());
        addView(imNum5, layoutParams7);
        ImageView imNum6 = imNum(69);
        LayoutParams layoutParams8 = new LayoutParams(i, i);
        layoutParams8.addRule(1, view.getId());
        layoutParams8.addRule(6, imNum4.getId());
        addView(imNum6, layoutParams8);
        ImageView imNum7 = imNum(65);
        LayoutParams layoutParams9 = new LayoutParams(i, i);
        layoutParams9.addRule(14);
        layoutParams9.addRule(2, imNum4.getId());
        layoutParams9.setMargins(0, i3, 0, 0);
        addView(imNum7, layoutParams9);
        ImageView imNum8 = imNum(64);
        LayoutParams layoutParams10 = new LayoutParams(i, i);
        layoutParams10.addRule(0, view.getId());
        layoutParams10.addRule(6, imNum7.getId());
        addView(imNum8, layoutParams10);
        ImageView imNum9 = imNum(66);
        LayoutParams layoutParams11 = new LayoutParams(i, i);
        layoutParams11.addRule(1, view.getId());
        layoutParams11.addRule(6, imNum7.getId());
        addView(imNum9, layoutParams11);
        ImageView imNum10 = imNum(62);
        LayoutParams layoutParams12 = new LayoutParams(i, i);
        layoutParams12.addRule(14);
        layoutParams12.addRule(2, imNum7.getId());
        addView(imNum10, layoutParams12);
        ImageView imNum11 = imNum(61);
        LayoutParams layoutParams13 = new LayoutParams(i, i);
        layoutParams13.addRule(16, view.getId());
        layoutParams13.addRule(2, imNum7.getId());
        addView(imNum11, layoutParams13);
        ImageView imNum12 = imNum(63);
        LayoutParams layoutParams14 = new LayoutParams(i, i);
        layoutParams14.addRule(1, view.getId());
        layoutParams14.addRule(2, imNum7.getId());
        addView(imNum12, layoutParams14);
    }

    private ImageView imNum(int i) {
        ImageView imageView = new ImageView(getContext());
        switch (i) {
            case 60:
                imageView.setImageResource(R.drawable.num0);
                break;
            case 61:
                imageView.setImageResource(R.drawable.num1);
                break;
            case 62:
                imageView.setImageResource(R.drawable.num2);
                break;
            case 63:
                imageView.setImageResource(R.drawable.num3);
                break;
            case 64:
                imageView.setImageResource(R.drawable.num4);
                break;
            case 65:
                imageView.setImageResource(R.drawable.num5);
                break;
            case 66:
                imageView.setImageResource(R.drawable.num6);
                break;
            case 67:
                imageView.setImageResource(R.drawable.num7);
                break;
            case 68:
                imageView.setImageResource(R.drawable.num8);
                break;
            case 69:
                imageView.setImageResource(R.drawable.num9);
                break;
            case 70:
                imageView.setImageResource(R.drawable.num_star);
                break;
            case 71:
                imageView.setImageResource(R.drawable.num_th);
                break;
        }
        imageView.setId(i);
        imageView.setBackground(OtherUtils.selNum("#50ffffff", "#9affffff"));
        imageView.setOnClickListener(this);
        return imageView;
    }

    @Override 
    public void onClick(View view) {
        if (this.padResult == null) {
            return;
        }
        switch (view.getId()) {
            case 70:
                this.padResult.onViewClick(false, "*");
                return;
            case 71:
                this.padResult.onViewClick(false, "#");
                return;
            case 72:
                this.padResult.onViewClick(true, "");
                return;
            default:
                PadResult padResult = this.padResult;
                StringBuilder sb = new StringBuilder();
                sb.append(view.getId() - 60);
                sb.append("");
                padResult.onViewClick(false, sb.toString());
                return;
        }
    }
}
