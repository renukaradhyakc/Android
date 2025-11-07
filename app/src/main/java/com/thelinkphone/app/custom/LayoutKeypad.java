package com.thelinkphone.app.custom;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.telecom.PhoneAccountHandle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.exifinterface.media.ExifInterface;

import com.thelinkphone.app.R;
import com.thelinkphone.app.dialog.DialogShowSim;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.SimUtils;

import java.util.ArrayList;
import java.util.Iterator;


public class LayoutKeypad extends RelativeLayout {
    private ArrayList<ItemContact> arrAllContact;
    private final ArrayList<ItemSimInfo> arrSim;
    private ItemContact contactChoose;
    private LayoutChooseSim layoutChooseSim;
    private int mode;
    private String num;
    private PadResult padResult;
    private int posSim;
    private boolean theme;
    private final ToneGenerator toneGenerator;
    private TextW tvAddNumber;
    private TextW tvKey;


    public interface PadResult {
        void onAddNewNumber(String str);

        void onShowInfo(ItemContact itemContact);
    }

    public void setPadResult(PadResult padResult) {
        this.padResult = padResult;
    }

    public LayoutKeypad(Context context) {
        super(context);
        this.num = "";
        this.toneGenerator = new ToneGenerator(3, 100);
        this.arrSim = SimUtils.getAvailableSIMCardLabels(context);
        this.posSim = MyShare.getPosSim(context);
        this.mode = MyShare.getSoundPad(getContext());
    }

    public void initView(boolean z) {
        this.theme = z;
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        int i = (widthScreen * 21) / 360;
        int i2 = (widthScreen * 16) / 360;
        boolean pay = true;
        if (!pay) {
            i2 = (widthScreen * 12) / 360;
        }
        int i3 = (int) ((widthScreen * 18.6f) / 100.0f);
        ImageView imageView = new ImageView(getContext());
        imageView.setId(655);
        imageView.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutKeypad.this.m73x248c1545(view);
            }
        });
        imageView.setImageResource(R.drawable.im_accept);
        LayoutParams layoutParams = new LayoutParams(i3, i3);
        layoutParams.addRule(12);
        layoutParams.addRule(14);
        layoutParams.setMargins(i, i2, i, (widthScreen * 10) / 100);
        addView(imageView, layoutParams);
        int i4 = (i3 * 12) / 50;
        ImageView imageView2 = new ImageView(getContext());
        imageView2.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutKeypad.this.m74x25c26824(view);
            }
        });
        imageView2.setOnLongClickListener(new OnLongClickListener() { 
            @Override 
            public final boolean onLongClick(View view) {
                return LayoutKeypad.this.m75x26f8bb03(view);
            }
        });
        imageView2.setPadding(i4, i4, i4, i4);
        LayoutParams layoutParams2 = new LayoutParams(i3, i3);
        layoutParams2.addRule(6, imageView.getId());
        layoutParams2.addRule(17, imageView.getId());
        addView(imageView2, layoutParams2);
        ImageView makeIm = makeIm(R.drawable.num_0);
        makeIm.setOnLongClickListener(new OnLongClickListener() { 
            @Override 
            public final boolean onLongClick(View view) {
                return LayoutKeypad.this.m76x282f0de2(view);
            }
        });
        LayoutParams layoutParams3 = new LayoutParams(i3, i3);
        layoutParams3.addRule(14);
        layoutParams3.addRule(2, imageView.getId());
        addView(makeIm, layoutParams3);
        ImageView makeIm2 = makeIm(R.drawable.num_8);
        LayoutParams layoutParams4 = new LayoutParams(i3, i3);
        layoutParams4.addRule(14);
        layoutParams4.addRule(2, makeIm.getId());
        layoutParams4.setMargins(i, i2, i, i2);
        addView(makeIm2, layoutParams4);
        ImageView makeIm3 = makeIm(R.drawable.num_5);
        LayoutParams layoutParams5 = new LayoutParams(i3, i3);
        layoutParams5.addRule(14);
        layoutParams5.addRule(2, makeIm2.getId());
        layoutParams5.setMargins(i, i2, i, 0);
        addView(makeIm3, layoutParams5);
        ImageView makeIm4 = makeIm(R.drawable.num_2);
        LayoutParams layoutParams6 = new LayoutParams(i3, i3);
        layoutParams6.addRule(14);
        layoutParams6.addRule(2, makeIm3.getId());
        addView(makeIm4, layoutParams6);
        ImageView makeIm5 = makeIm(R.drawable.num_1);
        LayoutParams layoutParams7 = new LayoutParams(i3, i3);
        layoutParams7.addRule(6, makeIm4.getId());
        layoutParams7.addRule(16, makeIm2.getId());
        addView(makeIm5, layoutParams7);
        ImageView makeIm6 = makeIm(R.drawable.num_3);
        LayoutParams layoutParams8 = new LayoutParams(i3, i3);
        layoutParams8.addRule(6, makeIm4.getId());
        layoutParams8.addRule(17, makeIm2.getId());
        addView(makeIm6, layoutParams8);
        ImageView makeIm7 = makeIm(R.drawable.num_4);
        LayoutParams layoutParams9 = new LayoutParams(i3, i3);
        layoutParams9.addRule(6, makeIm3.getId());
        layoutParams9.addRule(16, makeIm2.getId());
        addView(makeIm7, layoutParams9);
        ImageView makeIm8 = makeIm(R.drawable.num_6);
        LayoutParams layoutParams10 = new LayoutParams(i3, i3);
        layoutParams10.addRule(6, makeIm3.getId());
        layoutParams10.addRule(17, makeIm2.getId());
        addView(makeIm8, layoutParams10);
        ImageView makeIm9 = makeIm(R.drawable.num_7);
        LayoutParams layoutParams11 = new LayoutParams(i3, i3);
        layoutParams11.addRule(6, makeIm2.getId());
        layoutParams11.addRule(16, makeIm2.getId());
        addView(makeIm9, layoutParams11);
        ImageView makeIm10 = makeIm(R.drawable.num_9);
        LayoutParams layoutParams12 = new LayoutParams(i3, i3);
        layoutParams12.addRule(6, makeIm2.getId());
        layoutParams12.addRule(17, makeIm2.getId());
        addView(makeIm10, layoutParams12);
        ImageView makeIm11 = makeIm(R.drawable.num_s);
        LayoutParams layoutParams13 = new LayoutParams(i3, i3);
        layoutParams13.addRule(6, makeIm.getId());
        layoutParams13.addRule(16, makeIm2.getId());
        addView(makeIm11, layoutParams13);
        ImageView makeIm12 = makeIm(R.drawable.num_t);
        LayoutParams layoutParams14 = new LayoutParams(i3, i3);
        layoutParams14.addRule(6, makeIm.getId());
        layoutParams14.addRule(17, makeIm2.getId());
        addView(makeIm12, layoutParams14);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(17);
        linearLayout.setPadding(i4, 0, i4, 0);
        LayoutParams layoutParams15 = new LayoutParams(-1, -1);
        layoutParams15.addRule(2, makeIm4.getId());
        layoutParams15.setMargins(0, MyShare.getSizeNotification(getContext()), 0, 0);
        addView(linearLayout, layoutParams15);
        LayoutChooseSim layoutChooseSim = new LayoutChooseSim(getContext());
        this.layoutChooseSim = layoutChooseSim;
        layoutChooseSim.init(z);
        this.layoutChooseSim.setSim(this.posSim);
        this.layoutChooseSim.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutKeypad.this.m77x296560c1(view);
            }
        });
        linearLayout.addView(this.layoutChooseSim, -2, -2);
        if (this.arrSim.size() < 2) {
            this.layoutChooseSim.setVisibility(View.INVISIBLE);
        }
        TextW textW = new TextW(getContext());
        this.tvKey = textW;
        textW.setGravity(17);
        int i5 = widthScreen / 20;
        this.tvKey.setPadding(i5, 0, i5, 0);
        this.tvKey.setSingleLine();
        this.tvKey.setupText(400, 8.0f);
        this.tvKey.setOnLongClickListener(new OnLongClickListener() { 
            @Override 
            public final boolean onLongClick(View view) {
                return LayoutKeypad.this.m78x2a9bb3a0(view);
            }
        });
        LinearLayout.LayoutParams layoutParams16 = new LinearLayout.LayoutParams(-1, -2);
        if (pay) {
            layoutParams16.setMargins(0, i2, 0, i2);
        } else {
            layoutParams16.setMargins(0, i2 / 2, 0, i2 / 4);
        }
        linearLayout.addView(this.tvKey, layoutParams16);
        TextW textW2 = new TextW(getContext());
        this.tvAddNumber = textW2;
        textW2.setGravity(17);
        int i6 = widthScreen / 100;
        this.tvAddNumber.setPadding(i5, i6, i5, i6);
        this.tvAddNumber.setSingleLine();
        this.tvAddNumber.setupText(400, 4.3f);
        this.tvAddNumber.setTextColor(Color.parseColor("#007AFF"));
        linearLayout.addView(this.tvAddNumber, -2, -2);
        this.tvAddNumber.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutKeypad.this.m79x2bd2067f(view);
            }
        });
        if (z) {
            setBackgroundColor(-1);
            imageView2.setImageResource(R.drawable.im_del_keypad);
            this.tvKey.setTextColor(-16777216);
            return;
        }
        imageView2.setImageResource(R.drawable.im_del_keypad_dark);
        this.tvKey.setTextColor(-1);
        setBackgroundColor(Color.parseColor("#2C2C2C"));
    }



    public  void m73x248c1545(View view) {
        onCall();
    }



    public  void m74x25c26824(View view) {
        String str = this.num;
        if (str == null || str.isEmpty()) {
            return;
        }
        String str2 = this.num;
        String substring = str2.substring(0, str2.length() - 1);
        this.num = substring;
        this.tvKey.setText(substring);
        checkNum();
    }



    public  boolean m75x26f8bb03(View view) {
        this.num = "";
        this.tvKey.setText("");
        checkNum();
        return true;
    }



    public  boolean m76x282f0de2(View view) {
        speedDial("+");
        return true;
    }



    public  void m77x296560c1(View view) {
        onChooseSimClick();
    }



    public  boolean m78x2a9bb3a0(View view) {
        ClipData.Item itemAt = ((ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE)).getPrimaryClip().getItemAt(0);
        if (itemAt == null || itemAt.getText() == null) {
            return true;
        }
        setNumber(itemAt.getText().toString());
        return true;
    }



    public  void m79x2bd2067f(View view) {
        ItemContact itemContact = this.contactChoose;
        if (itemContact != null) {
            this.padResult.onShowInfo(itemContact);
        } else {
            this.padResult.onAddNewNumber(this.num);
        }
    }

    private ImageView makeIm(int i) {
        ImageView imageView = new ImageView(getContext());
        imageView.setId(i);
        imageView.setImageResource(i);
        if (this.theme) {
            imageView.clearColorFilter();
            imageView.setBackground(OtherUtils.selNum("#E5E5E5", "#BFBFBF"));
        } else {
            imageView.setColorFilter(-1);
            imageView.setBackground(OtherUtils.selNum("#555555", "#444444"));
        }
        imageView.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                LayoutKeypad.this.onClick(view);
            }
        });
        return imageView;
    }


    public void onClick(View view) {
        int id = view.getId();
        int i = -1;
        
        if (id == R.drawable.num_0) {
            speedDial("0");
            i = 0;
        } else if (id == R.drawable.num_1) {
            speedDial("1");
            i = 1;
        } else if (id == R.drawable.num_2) {
            speedDial(ExifInterface.GPS_MEASUREMENT_2D);
            i = 2;
        } else if (id == R.drawable.num_3) {
            speedDial(ExifInterface.GPS_MEASUREMENT_3D);
            i = 3;
        } else if (id == R.drawable.num_4) {
            speedDial("4");
            i = 4;
        } else if (id == R.drawable.num_5) {
            speedDial("5");
            i = 5;
        } else if (id == R.drawable.num_6) {
            speedDial("6");
            i = 6;
        } else if (id == R.drawable.num_7) {
            speedDial("7");
            i = 7;
        } else if (id == R.drawable.num_8) {
            speedDial("8");
            i = 8;
        } else if (id == R.drawable.num_9) {
            speedDial("9");
            i = 9;
        } else if (id == R.drawable.num_s) {
            speedDial("*");
            i = 10;
        } else if (id == R.drawable.num_t) {
            speedDial("#");
            i = 11;
        }

        if (i != -1) {
            int i2 = this.mode;
            if (i2 == 1) {
                OtherUtils.vibrator(getContext());
                this.toneGenerator.startTone(i, 200);
            } else if (i2 == 2) {
                OtherUtils.vibrator(getContext());
            }
        }
    }

    private void speedDial(String str) {
        String str2 = this.num + str;
        this.num = str2;
        this.tvKey.setText(str2);
        checkNum();
    }

    public void checkNum() {
        if (this.num.isEmpty()) {
            this.tvAddNumber.setText("");
            this.tvAddNumber.setVisibility(View.INVISIBLE);
            return;
        }
        this.tvAddNumber.setVisibility(View.VISIBLE);
        boolean z = true;
        Iterator<ItemContact> it = this.arrAllContact.iterator();
        while (it.hasNext()) {
            ItemContact next = it.next();
            Iterator<ItemPhone> it2 = next.getArrPhone().iterator();
            while (it2.hasNext()) {
                if (PhoneNumberUtils.compare(it2.next().getNumber(), this.num)) {
                    this.contactChoose = next;
                    this.tvAddNumber.setText(next.getName());
                    z = false;
                }
            }
        }
        if (z) {
            this.contactChoose = null;
            this.tvAddNumber.setText(R.string.add_number);
        }
    }

    private void onCall() {
        PhoneAccountHandle phoneAccountHandle;
        String str = this.num;
        if (str == null || str.isEmpty() || this.arrSim.size() == 0) {
            return;
        }
        if (this.posSim < this.arrSim.size()) {
            phoneAccountHandle = this.arrSim.get(this.posSim).handle;
        } else {
            phoneAccountHandle = this.arrSim.get(0).handle;
        }
        OtherUtils.call(getContext(), this.num, phoneAccountHandle);
    }

    public void setArrAllContact(ArrayList<ItemContact> arrayList) {
        this.arrAllContact = arrayList;
    }

    private void onChooseSimClick() {
        int[] iArr = new int[2];
        this.layoutChooseSim.getLocationInWindow(iArr);
        new DialogShowSim(getContext(), this.arrSim, iArr[1], new LayoutListSim.SimItemClick() { 
            @Override 
            public final void onSimClick(int i) {
                LayoutKeypad.this.m80x250cb08c(i);
            }
        }).show();
    }



    public  void m80x250cb08c(int i) {
        this.posSim = i;
        MyShare.putPosSim(getContext(), i);
        this.layoutChooseSim.setSim(i);
    }

    public void updateMode() {
        this.mode = MyShare.getSoundPad(getContext());
        int posSim = MyShare.getPosSim(getContext());
        this.posSim = posSim;
        this.layoutChooseSim.setSim(posSim);
    }

    public void setNumber(String str) {
        if (str == null) {
            this.num = "";
        } else {
            this.num = str;
        }
        this.tvKey.setText(this.num);
        checkNum();
    }
}
