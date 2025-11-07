package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.ActionUtils;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewSearch extends LinearLayout {
    private final EditW edtSearch;
    private final ImageView imDel;

    
    public interface TextResult {
        void onTextChange(String str);
    }

    public ViewSearch(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 50;
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(16);
        setBackground(OtherUtils.bgIcon(Color.parseColor("#1f767680"), i * 1.6f));
        EditW editW = new EditW(context);
        this.edtSearch = editW;
        editW.setBackgroundColor(0);
        editW.setupText(400, 4.2f);
        editW.setHint(R.string.search);
        editW.setHintTextColor(Color.parseColor("#878789"));
        editW.setSingleLine();
        int i2 = widthScreen / 40;
        editW.setPadding(i, i2, i, i2);
        if (Build.VERSION.SDK_INT >= 28) {
            editW.setTypeface(Typeface.create(Typeface.SANS_SERIF, 400, false));
        } else {
            editW.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        }
        ImageView imageView = new ImageView(context);
        imageView.setPadding(i, i, 0, i);
        imageView.setImageResource(R.drawable.im_search);
        int i3 = (widthScreen * 8) / 100;
        addView(imageView, i3, i3);
        addView(editW, new LayoutParams(0, -2, 1.0f));
        ImageView imageView2 = new ImageView(context);
        this.imDel = imageView2;
        imageView2.setImageResource(R.drawable.ic_close);
        imageView2.setVisibility(View.INVISIBLE);
        imageView2.setPadding(i, 0, i, 0);
        imageView2.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewSearch.this.m95x6adb2fc3(view);
            }
        });
        int i4 = (widthScreen * 9) / 100;
        addView(imageView2, i4, i4);
        if (MyShare.getTheme(context)) {
            editW.setTextColor(Color.parseColor("#333333"));
        } else {
            editW.setTextColor(-1);
        }
    }

    
    
    public  void m95x6adb2fc3(View view) {
        clearText();
    }

    public void clearText() {
        this.edtSearch.setText("");
    }

    public void setListenerTextChange(final TextResult textResult) {
        this.edtSearch.addTextChangedListener(new TextWatcher() { 
            @Override 
            public void afterTextChanged(Editable editable) {
            }

            @Override 
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override 
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (charSequence.length() == 0) {
                    ViewSearch.this.imDel.setVisibility(View.GONE);
                } else {
                    ViewSearch.this.imDel.setVisibility(View.VISIBLE);
                }
                textResult.onTextChange(charSequence.toString().toLowerCase());
            }
        });
    }

    public void onShow(boolean z) {
        if (z) {
            setVisibility(View.VISIBLE);
            this.edtSearch.requestFocus();
            new Handler().postDelayed(new Runnable() { 
                @Override 
                public final void run() {
                    ViewSearch.this.m96xaa92bfec();
                }
            }, 500L);
            return;
        }
        setVisibility(View.GONE);
        ActionUtils.hideKeyboard(getContext(), this.edtSearch);
        this.edtSearch.clearFocus();
    }

    
    
    public  void m96xaa92bfec() {
        ActionUtils.showKeyboard(getContext(), this.edtSearch);
    }
}
