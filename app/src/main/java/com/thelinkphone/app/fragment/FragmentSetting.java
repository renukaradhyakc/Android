package com.thelinkphone.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.fragment.app.Fragment;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.ActivityPreview;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.custom.ViewItemSetting;
import com.thelinkphone.app.dialog.DialogRate;
import com.thelinkphone.app.utils.ActionUtils;
import com.thelinkphone.app.utils.MyConst;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class FragmentSetting extends Fragment {
    private ViewSetting viewSetting;

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (this.viewSetting == null) {
            ViewSetting viewSetting = new ViewSetting(layoutInflater.getContext());
            this.viewSetting = viewSetting;
            viewSetting.initView(MyShare.getTheme(layoutInflater.getContext()));
        }
        return this.viewSetting;
    }

    @Override 
    public void onResume() {
        super.onResume();

    }

    
    
    public class ViewSetting extends LinearLayout {
        private FragmentWallpaper fragmentWallpaper;
        private ImageView imModeSound;

        private int getImageModeSound(int i) {
            return i != 0 ? i != 1 ? R.drawable.pad_vibration : R.drawable.pad_sound : R.drawable.pad_mute;
        }

        public ViewSetting(Context context) {
            super(context);
            setOrientation(LinearLayout.VERTICAL);
        }

        public void initView(boolean z) {
            int widthScreen = OtherUtils.getWidthScreen(getContext());
            int i = widthScreen / 25;
            LinearLayout linearLayout = new LinearLayout(getContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(16);
            LayoutParams layoutParams = new LayoutParams(-1, -2);
            layoutParams.setMargins(0, MyShare.getSizeNotification(getContext()), 0, 0);
            addView(linearLayout, layoutParams);
            TextW textW = new TextW(getContext());
            textW.setupText(600, 8.0f);
            textW.setText(R.string.setting);
            textW.setPadding(i, 0, 0, 0);
            linearLayout.addView(textW, new LayoutParams(0, -2, 1.0f));


            ScrollView scrollView = new ScrollView(getContext());
            scrollView.setFillViewport(true);
            LayoutParams layoutParams2 = new LayoutParams(-1, -1);
            layoutParams2.setMargins(0, i, 0, 0);
            addView(scrollView, layoutParams2);
            LinearLayout linearLayout2 = new LinearLayout(getContext());
            linearLayout2.setOrientation(LinearLayout.VERTICAL);
            scrollView.addView(linearLayout2, -1, -2);
            TextW textW2 = new TextW(getContext());
            textW2.setupText(450, 4.2f);
            textW2.setTextColor(Color.parseColor("#B8B8B8"));
            textW2.setText(R.string.title_setting);
            int i3 = i / 2;
            textW2.setPadding(i, i / 4, 0, i3);
            linearLayout2.addView(textW2, -1, -2);
            addDivider(linearLayout2);
            int i4 = (widthScreen * 15) / 100;
            ViewItemSetting viewItemSetting = new ViewItemSetting(getContext());
            viewItemSetting.setItem(R.string.style, z);
            viewItemSetting.addNext();
            viewItemSetting.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewSetting.this.m160xb3b584a0(view);
                }
            });
            linearLayout2.addView(viewItemSetting, -1, i4);
            addDivider(linearLayout2);
            ViewItemSetting viewItemSetting2 = new ViewItemSetting(getContext());
            viewItemSetting2.setItem(R.string.change_wallpaper, z);
            viewItemSetting2.addNext();
            viewItemSetting2.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewSetting.this.m165x7ac16ba1(view);
                }
            });
            linearLayout2.addView(viewItemSetting2, -1, i4);
            addDivider(linearLayout2);
            ViewItemSetting viewItemSetting3 = new ViewItemSetting(getContext());
            viewItemSetting3.setItem(R.string.dark_themes, z);
            viewItemSetting3.addSwitch(!z, new ViewItemSetting.SwitchListener() { 
                @Override 
                public final void onSwitchChange(ViewItemSetting viewItemSetting4, boolean z2) {
                    ViewSetting.this.m166x41cd52a2(viewItemSetting4, z2);
                }
            });
            linearLayout2.addView(viewItemSetting3, -1, i4);
            addDivider(linearLayout2);
            ViewItemSetting viewItemSetting4 = new ViewItemSetting(getContext());
            viewItemSetting4.setItem(R.string.keyboard_sound, z);
            this.imModeSound = viewItemSetting4.addMode(getImageModeSound(MyShare.getSoundPad(getContext())), new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewSetting.this.m167x8d939a3(view);
                }
            });
            linearLayout2.addView(viewItemSetting4, -1, i4);
            addDivider(linearLayout2);
            ViewItemSetting viewItemSetting5 = new ViewItemSetting(getContext());
            viewItemSetting5.setItem(R.string.recorder_list, z);
            viewItemSetting5.addNext();
            viewItemSetting5.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewSetting.this.m168xcfe520a4(view);
                }
            });
            linearLayout2.addView(viewItemSetting5, -1, i4);
            addDivider(linearLayout2);

            ViewItemSetting viewItemSetting7 = new ViewItemSetting(getContext());
            viewItemSetting7.setItem(R.string.fake_call, z);
            viewItemSetting7.addNext();
            viewItemSetting7.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewSetting.this.m170x5dfceea6(view);
                }
            });
            linearLayout2.addView(viewItemSetting7, -1, i4);
            addDivider(linearLayout2);

            ViewItemSetting viewItemSetting6 = new ViewItemSetting(getContext());
            viewItemSetting6.setItem(R.string.block_number, z);
            viewItemSetting6.addNext();
            viewItemSetting6.setOnClickListener(new OnClickListener() {
                @Override
                public final void onClick(View view) {
                    ViewSetting.this.m169x96f107a5(view);
                }
            });
            linearLayout2.addView(viewItemSetting6, -1, i4);
            addDivider(linearLayout2);



            TextW textW3 = new TextW(getContext());
            textW3.setupText(450, 4.2f);
            textW3.setTextColor(Color.parseColor("#B8B8B8"));
            textW3.setText(R.string.other);
            textW3.setPadding(i, (i * 5) / 4, 0, i3);
            linearLayout2.addView(textW3, -1, -2);
            addDivider(linearLayout2);
            ViewItemSetting viewItemSetting8 = new ViewItemSetting(getContext());
            viewItemSetting8.setItem(R.string.rate_app, z);
            viewItemSetting8.addNext();
            viewItemSetting8.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewSetting.this.m171x2508d5a7(view);
                }
            });
            linearLayout2.addView(viewItemSetting8, -1, i4);

            addDivider(linearLayout2);
            ViewItemSetting viewItemSetting13 = new ViewItemSetting(getContext());
            viewItemSetting13.setItem(R.string.policy, z);
            viewItemSetting13.addNext();
            viewItemSetting13.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewSetting.this.m164x62196375(view);
                }
            });
            linearLayout2.addView(viewItemSetting13, -1, i4);
            if (z) {
                textW.setTextColor(-16777216);
                setBackgroundColor(-1);
                return;
            }
            textW.setTextColor(-1);
            setBackgroundColor(Color.parseColor("#2C2C2C"));
        }

        
        
        public  void m159xeca99d9f(View view) {
            FragmentSetting.this.startActivity(new Intent(getContext(), ActivityPreview.class));
        }

        
        
        public  void m160xb3b584a0(View view) {
            showFragment(new FragmentStyle());
        }

        
        
        public  void m165x7ac16ba1(View view) {
            if (this.fragmentWallpaper == null) {
                this.fragmentWallpaper = new FragmentWallpaper();
            }
            showFragment(this.fragmentWallpaper);
        }

        
        
        public  void m166x41cd52a2(ViewItemSetting viewItemSetting, boolean z) {
            MyShare.putTheme(getContext(), !z);
            if (FragmentSetting.this.getActivity() instanceof ActivityHome) {
                ((ActivityHome) FragmentSetting.this.getActivity()).onChangeTheme();
            }
        }

        
        
        public  void m167x8d939a3(View view) {
            int soundPad = MyShare.getSoundPad(getContext());
            if (soundPad == 0) {
                MyShare.putSoundPad(getContext(), 1);
            } else if (soundPad == 1) {
                MyShare.putSoundPad(getContext(), 2);
            } else if (soundPad == 2) {
                MyShare.putSoundPad(getContext(), 0);
            }
            this.imModeSound.setImageResource(getImageModeSound(MyShare.getSoundPad(getContext())));
        }

        
        
        public  void m168xcfe520a4(View view) {
            showFragment(new FragmentRecorder());
        }

        
        
        public  void m169x96f107a5(View view) {

            Log.e("block123","number");
            showFragment(new FragmentBlock());
        }

        
        
        public  void m170x5dfceea6(View view) {
            showFragment(new FragmentFakeCall());
        }

        
        
        public  void m171x2508d5a7(View view) {
            new DialogRate(getContext()).show();
        }


        
        public  void m164x62196375(View view) {
            ActionUtils.openLink(getContext(), MyConst.POLICY);
        }

        private void addDivider(LinearLayout linearLayout) {
            View view = new View(getContext());
            view.setBackgroundColor(Color.parseColor("#8A8A8E"));
            LayoutParams layoutParams = new LayoutParams(-1, 1);
            layoutParams.setMargins(((OtherUtils.getWidthScreen(getContext()) / 25) * 3) / 2, 0, 0, 0);
            linearLayout.addView(view, layoutParams);
        }

        private void showFragment(final Fragment fragment) {
            if (FragmentSetting.this.getActivity() instanceof ActivityHome) {

                        ViewSetting.this.m173x69e6e95e(fragment);

            }
        }

        
        
        public  void m173x69e6e95e(Fragment fragment) {
            ((ActivityHome) FragmentSetting.this.getActivity()).showFragment(fragment, true);
        }
    }
}
