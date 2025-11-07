package com.thelinkphone.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;


public class BaseDialog extends Dialog {
    public BaseDialog(Context context) {
        super(context);
        requestWindowFeature(1);
        getWindow().setBackgroundDrawableResource(17170445);
        theme();
        setCancelable(false);
    }

    private void theme() {
        getWindow().getDecorView().setSystemUiVisibility(1536);
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() { 
            @Override 
            public final void onSystemUiVisibilityChange(int i) {
                BaseDialog.lambda$theme$0(decorView, i);
            }
        });
    }


    public static  void lambda$theme$0(View view, int i) {
        if ((i & 4) == 0) {
            view.setSystemUiVisibility(1536);
        }
    }
}
