package com.thelinkphone.app.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentManager;

import com.thelinkphone.app.R;


public class ViewTabMode extends LinearLayout {
    private TabResult tabResult;
    private LayoutItemTab vCon;
    private LayoutItemTab vFar;
    private LayoutItemTab vPad;
    private LayoutItemTab vRec;
    private LayoutItemTab vCallLog;
    private LayoutItemTab vSet;

    private FragmentManager fragmentManager;
    private int containerId;
    public interface TabResult {
        void onTapClick(int i);
    }

    public void setTabResult(TabResult tabResult) {
        this.tabResult = tabResult;
    }

    public ViewTabMode(Context context) {
        super(context);
        init();
    }

    public ViewTabMode(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public ViewTabMode(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    private void init() {

        setOrientation(LinearLayout.HORIZONTAL);

       /* LayoutItemTab layoutItemTab;

        layoutItemTab = new LayoutItemTab(getContext());
        layoutItemTab.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                onTabClick(view, 0);
            }
        });
        layoutItemTab.setData(R.drawable.im_tab_rec, R.string.recents);
        vRec = layoutItemTab;
        addView(layoutItemTab, new LayoutParams(0, -2, 1.0f));

        layoutItemTab = new LayoutItemTab(getContext());
        layoutItemTab.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                onTabClick(view, 1);
            }
        });
        layoutItemTab.setData(R.drawable.im_tab_contact, R.string.contacts);
        vCon = layoutItemTab;
        addView(layoutItemTab, new LayoutParams(0, -2, 1.0f));

        layoutItemTab = new LayoutItemTab(getContext());
        layoutItemTab.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                onTabClick(view, 2);
            }
        });
        layoutItemTab.setData(R.drawable.im_tab_keypab, R.string.keypad);
        vPad = layoutItemTab;
        addView(layoutItemTab, new LayoutParams(0, -2, 1.0f));

        layoutItemTab = new LayoutItemTab(getContext());
        layoutItemTab.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                onTabClick(view, 3);
            }
        });
        layoutItemTab.setData(R.drawable.im_tab_setting, R.string.setting);
        vSet = layoutItemTab;
        addView(layoutItemTab, new LayoutParams(0, -2, 1.0f));*/
        LayoutItemTab layoutItemTab = new LayoutItemTab(getContext());
        this.vFar = layoutItemTab;
        layoutItemTab.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                ViewTabMode.this.onTabClick(view);
            }
        });
        this.vFar.setData(R.drawable.baseline_qr_code_scanner_24, R.string.favorites);
        addView(this.vFar, new LayoutParams(0, -2, 1.0f));
        LayoutItemTab layoutItemTab2 = new LayoutItemTab(getContext());
        this.vRec = layoutItemTab2;
        layoutItemTab2.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                ViewTabMode.this.onTabClick(view);
            }
        });
        this.vRec.setData(R.drawable.im_tab_rec, R.string.recents);
        addView(this.vRec, new LayoutParams(0, -2, 1.0f));
        LayoutItemTab layoutItemTab3 = new LayoutItemTab(getContext());
        this.vCon = layoutItemTab3;
        layoutItemTab3.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                ViewTabMode.this.onTabClick(view);
            }
        });
        this.vCon.setData(R.drawable.baseline_calendar_month_24, R.string.events);
        addView(this.vCon, new LayoutParams(0, -2, 1.0f));
        LayoutItemTab layoutItemTab4 = new LayoutItemTab(getContext());
        this.vCallLog = layoutItemTab4;
        layoutItemTab4.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                ViewTabMode.this.onTabClick(view);
            }
        });
        this.vCallLog.setData(R.drawable.baseline_call_log_24, R.string.call_log);
        addView(this.vCallLog, new LayoutParams(0, -2, 1.0f));
        LayoutItemTab layoutItemTab5 = new LayoutItemTab(getContext());
        this.vPad = layoutItemTab5;
        layoutItemTab5.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                ViewTabMode.this.onTabClick(view);
            }
        });
        this.vPad.setData(R.drawable.im_tab_keypab, R.string.keypad);
        addView(this.vPad, new LayoutParams(0, -2, 1.0f));
        LayoutItemTab layoutItemTab6 = new LayoutItemTab(getContext());
        this.vSet = layoutItemTab6;
        layoutItemTab6.setOnClickListener(new OnClickListener() {
            @Override
            public final void onClick(View view) {
                ViewTabMode.this.onTabClick(view);
            }
        });
        this.vSet.setData(R.drawable.im_tab_setting, R.string.setting);
        addView(this.vSet, new LayoutParams(0, -2, 1.0f));

        // Initialize all tabs as unselected
        this.vFar.setChoose(false);
        this.vRec.setChoose(false);
        this.vCon.setChoose(false);
        this.vCallLog.setChoose(false);
        this.vPad.setChoose(false);
        this.vSet.setChoose(false);
    }

    public void setFragmentManager(FragmentManager fragmentManager, int containerId) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
    }
   /* private void onTabClick(View view, int tabIndex) {
        vRec.setChoose(tabIndex == 0);
        vCon.setChoose(tabIndex == 1);
        vPad.setChoose(tabIndex == 2);
        vSet.setChoose(tabIndex == 3);

        TabResult tabResult = this.tabResult;
        if (tabResult != null) {
            tabResult.onTapClick(tabIndex);
        }

        // Replace fragment based on the tab index
        switch (tabIndex) {
            case 0:
                // Replace with ScanFrag
                if (fragmentManager != null) {
                    fragmentManager.beginTransaction().replace(containerId, new ScanFrag()).commit();
                }
                break;
            // Add cases for other tabs if needed
        }
    }*/
    public void onTabClick(View view) {
        LayoutItemTab layoutItemTab = this.vFar;
        layoutItemTab.setChoose(view == layoutItemTab);
        LayoutItemTab layoutItemTab2 = this.vRec;
        layoutItemTab2.setChoose(view == layoutItemTab2);
        LayoutItemTab layoutItemTab3 = this.vCon;
        layoutItemTab3.setChoose(view == layoutItemTab3);
        LayoutItemTab layoutItemTab4 = this.vCallLog;
        layoutItemTab4.setChoose(view == layoutItemTab4);
        LayoutItemTab layoutItemTab5 = this.vPad;
        layoutItemTab5.setChoose(view == layoutItemTab5);
        LayoutItemTab layoutItemTab6 = this.vSet;
        layoutItemTab6.setChoose(view == layoutItemTab6);
        TabResult tabResult = this.tabResult;
        if (tabResult != null) {
            if (view == this.vFar) {
                tabResult.onTapClick(0);
            } else if (view == this.vRec) {
                tabResult.onTapClick(1);
            } else if (view == this.vCon) {
                tabResult.onTapClick(2);
            } else if (view == this.vCallLog) {
                tabResult.onTapClick(3);
            } else if (view == this.vPad) {
                tabResult.onTapClick(4);
            } else {
                tabResult.onTapClick(5);
            }
        }
    }
  /*  public void setTabDefault(int i) {
        onTabClick(null, i);
    }*/
    public void setTabDefault(int i) {
        // Explicitly reset all tabs to unselected state
        if (this.vFar != null) this.vFar.setChoose(false);
        if (this.vRec != null) this.vRec.setChoose(false);
        if (this.vCon != null) this.vCon.setChoose(false);
        if (this.vCallLog != null) this.vCallLog.setChoose(false);
        if (this.vPad != null) this.vPad.setChoose(false);
        if (this.vSet != null) this.vSet.setChoose(false);

        // Set only the active tab to selected state
        if (i == 0 && this.vFar != null) {
            this.vFar.setChoose(true);
        } else if (i == 1 && this.vRec != null) {
            this.vRec.setChoose(true);
        } else if (i == 2 && this.vCon != null) {
            this.vCon.setChoose(true);
        } else if (i == 3 && this.vCallLog != null) {
            this.vCallLog.setChoose(true);
        } else if (i == 4 && this.vPad != null) {
            this.vPad.setChoose(true);
        } else if (this.vSet != null) {
            this.vSet.setChoose(true);
        }

        TabResult tabResult = this.tabResult;
        if (tabResult != null) {
            tabResult.onTapClick(i);
        }
    }
}
