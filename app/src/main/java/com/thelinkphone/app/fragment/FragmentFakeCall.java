package com.thelinkphone.app.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.thelinkphone.app.ActivityFakeRingCall;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.EditW;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.custom.ViewSeekbar;
import com.thelinkphone.app.utils.MyConst;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;

import com.yalantis.ucrop.UCrop;
import java.io.File;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class FragmentFakeCall extends Fragment {
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() { 
        @Override 
        public final void onActivityResult(Object obj) {
            FragmentFakeCall.this.m110x348b8da((ActivityResult) obj);
        }
    });
    private String pathImage;
    private ViewFakeCall viewFakeCall;

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        ViewFakeCall viewFakeCall = new ViewFakeCall(getContext());
        this.viewFakeCall = viewFakeCall;

        return viewFakeCall;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Remove AdMob initialization
    }

    
    
    public  void m110x348b8da(ActivityResult activityResult) {
        if (activityResult.getResultCode() != -1 || activityResult.getData() == null || activityResult.getData().getData() == null || getContext() == null) {
            return;
        }
        try {
            String pathFakeCall = OtherUtils.getPathFakeCall(getContext());
            File file = new File(pathFakeCall);
            if (!file.exists()) {
                file.mkdir();
            }
            OtherUtils.deleteAllFileInFolder(pathFakeCall, false);
            this.pathImage = pathFakeCall + System.currentTimeMillis() + ".jpg";
            UCrop.of(activityResult.getData().getData(), Uri.fromFile(new File(this.pathImage))).withAspectRatio(1.0f, 1.0f).withMaxResultSize(300, 300).start(getContext(), this);
        } catch (Exception unused) {
            Toast.makeText(getContext(), (int) R.string.error_load_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override 
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 == -1 && i == 69) {
            try {
                this.viewFakeCall.setPhoto();
                Toast.makeText(getContext(), (int) R.string.done, Toast.LENGTH_SHORT).show();
            } catch (Exception unused) {
                OtherUtils.deleteFile(this.pathImage);
                Toast.makeText(getContext(), (int) R.string.error_load_image, Toast.LENGTH_SHORT).show();
            }
        }
    }

    
    
    public class ViewFakeCall extends RelativeLayout {
        private final EditW edtName;
        private final Handler handler;
        private final ImageView imPhoto;
        private String name;
        private final Runnable runnable;
        private int sec;
        private final TextW tvSec;

        @SuppressLint("ResourceType")
        public ViewFakeCall(Context context) {
            super(context);
            this.sec = 0;
            this.runnable = new Runnable() { 
                @Override 
                public final void run() {
                    ViewFakeCall.this.m114x9a4ff505();
                }
            };
            int widthScreen = OtherUtils.getWidthScreen(context);
            int i = (widthScreen * 15) / 100;
            int i2 = widthScreen / 25;
            TextW textW = new TextW(context);
            textW.setId(1212);
            textW.setText(R.string.fake_call);
            textW.setupText(600, 8.0f);
            LayoutParams layoutParams = new LayoutParams(-1, -2);
            int i3 = i2 / 4;
            layoutParams.setMargins(i2, MyShare.getSizeNotification(context), i2, i3);
            addView(textW, layoutParams);
            TextW textW2 = new TextW(context);
            textW2.setId(1213);
            textW2.setText(R.string.fake_content);
            textW2.setupText(400, 3.2f);
            LayoutParams layoutParams2 = new LayoutParams(-1, -2);
            layoutParams2.addRule(3, textW.getId());
            layoutParams2.setMargins(i2, 0, i2, 0);
            addView(textW2, layoutParams2);
            TextW textW3 = new TextW(context);
            textW3.setId(5642);
            textW3.setText(R.string.done);
            textW3.setTextColor(Color.parseColor("#FF2828"));
            textW3.setGravity(1);
            textW3.setupText(600, 4.6f);
            textW3.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewFakeCall.this.m111xff4574c2(view);
                }
            });
            textW3.setPadding(i2, i2, i2, i2);
            LayoutParams layoutParams3 = new LayoutParams(-1, -2);
            layoutParams3.addRule(12);
            int i4 = i2 * 4;
            layoutParams3.setMargins(i4, i2, i4, i2);
            addView(textW3, layoutParams3);
            EditW editW = new EditW(getContext());
            this.edtName = editW;
            editW.setId(223);
            editW.setTypeface(textW2.getTypeface());
            editW.setHintTextColor(Color.parseColor("#D4D4D4"));
            editW.setupText(400, 4.4f);
            editW.setPadding(i2, i2, i2, i2);
            editW.setGravity(1);
            editW.setHint(R.string.enter_name);
            LayoutParams layoutParams4 = new LayoutParams(-1, -2);
            int i5 = i2 * 3;
            layoutParams4.setMargins(i2, i5, i2, i2);
            layoutParams4.addRule(3, textW2.getId());
            addView(editW, layoutParams4);
            ImageView imageView = new ImageView(context);
            this.imPhoto = imageView;
            imageView.setId(1214);
            imageView.setImageResource(R.drawable.im_add_photo);
            imageView.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewFakeCall.this.m112x32f39f83(view);
                }
            });
            LayoutParams layoutParams5 = new LayoutParams(i, i);
            layoutParams5.setMargins(0, i2, 0, i2 * 2);
            layoutParams5.addRule(3, editW.getId());
            layoutParams5.addRule(14);
            addView(imageView, layoutParams5);
            TextW textW4 = new TextW(context);
            textW4.setId(224);
            textW4.setText(R.string.fake_call_notification);
            textW4.setupText(400, 4.4f);
            LayoutParams layoutParams6 = new LayoutParams(-2, -2);
            int i6 = i2 / 2;
            layoutParams6.setMargins(i2, 0, 0, i6);
            layoutParams6.addRule(3, imageView.getId());
            addView(textW4, layoutParams6);
            TextW textW5 = new TextW(context);
            this.tvSec = textW5;
            textW5.setId(225);
            updateText();
            textW5.setTextColor(Color.parseColor("#FF2828"));
            textW5.setupText(600, 4.4f);
            LayoutParams layoutParams7 = new LayoutParams(-2, -2);
            layoutParams7.setMargins(i6, 0, i2, 0);
            layoutParams7.addRule(6, textW4.getId());
            layoutParams7.addRule(17, textW4.getId());
            addView(textW5, layoutParams7);
            ViewSeekbar viewSeekbar = new ViewSeekbar(context);
            viewSeekbar.setSecResult(new ViewSeekbar.SecResult() { 
                @Override 
                public final void onSecResult(int i7) {
                    ViewFakeCall.this.m113x66a1ca44(i7);
                }
            });
            LayoutParams layoutParams8 = new LayoutParams(-1, widthScreen / 10);
            int i7 = i5 / 2;
            layoutParams8.setMargins(i7, i3, i7, 0);
            layoutParams8.addRule(3, textW4.getId());
            addView(viewSeekbar, layoutParams8);
            this.handler = new Handler();
            if (MyShare.getTheme(context)) {
                textW.setTextColor(-16777216);
                textW2.setTextColor(Color.parseColor("#B8B8B8"));
                textW4.setTextColor(-16777216);
                editW.setBackgroundResource(R.drawable.sel_tv_done);
                textW3.setBackgroundResource(R.drawable.sel_tv_done);
                editW.setTextColor(-16777216);
                setBackgroundColor(-1);
                return;
            }
            textW.setTextColor(-1);
            textW2.setTextColor(Color.parseColor("#B8B8B8"));
            textW4.setTextColor(-1);
            editW.setBackgroundResource(R.drawable.sel_tv_done_dark);
            textW3.setBackgroundResource(R.drawable.sel_tv_done_dark);
            editW.setTextColor(-1);
            setBackgroundColor(Color.parseColor("#2C2C2C"));
        }

        
        
        public  void m111xff4574c2(View view) {
            onDone();
        }

        
        
        public  void m112x32f39f83(View view) {
            onPickPhoto();
        }

        
        
        public  void m113x66a1ca44(int i) {
            this.sec = i;
            updateText();
        }

        private void onPickPhoto() {
            String[] strArr;
            if (Build.VERSION.SDK_INT >= 29) {
                strArr = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
            } else {
                strArr = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
            }
            int length = strArr.length;
            boolean z = false;
            int i = 0;
            while (true) {
                if (i >= length) {
                    z = true;
                    break;
                }
                if (!OtherUtils.checkPer(getContext(), strArr[i])) {
                    break;
                }
                i++;
            }
            if (z || FragmentFakeCall.this.getActivity() == null) {
                OtherUtils.pickImage(FragmentFakeCall.this.launcher);
            } else {
                ActivityCompat.requestPermissions(FragmentFakeCall.this.getActivity(), strArr, 1);
            }
        }

        private void updateText() {
            this.tvSec.setText(this.sec + "s");
        }

        private void onDone() {
            String obj = this.edtName.getText().toString();
            this.name = obj;
            if (obj.isEmpty()) {
                this.name = "Zee Avi";
            }
            this.handler.postDelayed(this.runnable, this.sec * 1000);
            if (FragmentFakeCall.this.getActivity() != null) {
                FragmentFakeCall.this.getActivity().moveTaskToBack(true);
            }
        }

        
        
        @SuppressLint("WrongConstant")
        public  void m114x9a4ff505() {
            if (FragmentFakeCall.this.getActivity() != null) {
                FragmentFakeCall.this.getActivity().finish();
            }
            Intent intent = new Intent(getContext(), ActivityFakeRingCall.class);
            intent.putExtra(MyConst.DATA_NAME, this.name);
            intent.setFlags(272760832);
            if (FragmentFakeCall.this.pathImage != null) {
                intent.putExtra(MyConst.DATA_PHOTO, FragmentFakeCall.this.pathImage);
            }
            FragmentFakeCall.this.startActivity(intent);
        }

        public void setPhoto() {
            Glide.with(this).load(FragmentFakeCall.this.pathImage).apply((BaseRequestOptions<?>) new RequestOptions().circleCrop()).into(this.imPhoto);
        }
    }
}
