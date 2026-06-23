package com.thelinkphone.app.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.aitsuki.swipe.SwipeLayout;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.FavOnItemClick;
import com.thelinkphone.app.custom.LayoutItemTopRecent;
import com.thelinkphone.app.custom.LayoutRecent;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.item.ItemSimInfo;


import java.util.ArrayList;
import java.util.Iterator;


public class AdapterRecent extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ItemRecentGroup> arrGroup;
    private final ArrayList<ItemRecentGroup> arrShow;
    private final ArrayList<ItemSimInfo> arrSim;
    private boolean isChoose;
    private boolean isMiss;
    private final RecentItemClick recentItemClick;
    private final boolean theme;

    
    public interface RecentItemClick {
        void onDel(ItemRecentGroup itemRecentGroup);

        void onInfo(ItemRecentGroup itemRecentGroup);

        void onItemClick(ItemRecentGroup itemRecentGroup);

        void onLongClick(ItemRecentGroup itemRecentGroup);
    }

    @Override 
    public int getItemViewType(int i) {
        return i == 0 ? 0 : 1;
    }

    public AdapterRecent(ArrayList<ItemRecentGroup> arrayList, ArrayList<ItemSimInfo> arrayList2, boolean z, RecentItemClick recentItemClick) {
        this.arrGroup = arrayList;
        this.arrShow = new ArrayList<>(arrayList);
        this.recentItemClick = recentItemClick;
        this.theme = z;
        this.arrSim = arrayList2;
    }

    public void addNewData() {
        long start = System.currentTimeMillis();
        Log.d("ADAPTER_PERF", "addNewData itemCount=" + getItemCount());
        if (this.isMiss) {
            showMiss(true);
            return;
        }
        this.arrShow.clear();
        this.arrShow.addAll(this.arrGroup);
        Log.d("ADAPTER_PERF", "addNewData arrShow=" + arrShow.size());
        Log.d("ADAPTER_PERF", "before notifyDataSetChanged = " + (System.currentTimeMillis() - start) + " ms");
        notifyDataSetChanged();
        Log.d("ADAPTER_PERF", "after notifyDataSetChanged");
    }

    public void setChoose(boolean z) {
        this.isChoose = z;
        notifyItemRangeChanged(0, getItemCount(), true);
    }

    public void showMiss(boolean z) {
        this.isMiss = z;
        this.arrShow.clear();
        if (z) {
            Iterator<ItemRecentGroup> it = this.arrGroup.iterator();
            while (it.hasNext()) {
                ItemRecentGroup next = it.next();
                if (next.arrRecent.get(0).type == 3) {
                    this.arrShow.add(next);
                }
            }
        } else {
            this.arrShow.addAll(this.arrGroup);
        }
        notifyDataSetChanged();
    }

    public void removeRecent(ItemRecentGroup itemRecentGroup) {
        int position = arrShow.indexOf(itemRecentGroup);
        this.arrGroup.remove(itemRecentGroup);
        this.arrShow.remove(itemRecentGroup);
        notifyItemRemoved(position+1);
    }

    public void removeAll() {
        this.arrShow.clear();
        this.arrGroup.clear();
        this.isChoose = false;
        notifyDataSetChanged();
    }

    public boolean isChoose() {
        return this.isChoose;
    }

    @Override 
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        long start = System.currentTimeMillis();
        if (i == 0) {
            return new HolderTop(new LayoutItemTopRecent(viewGroup.getContext()));
        }
        Log.d("VH_CREATE", "onCreateViewHolder = " + (System.currentTimeMillis()-start) + " ms");
        return new HolderItem(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_recent, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof HolderItem) {
            HolderItem holderItem = (HolderItem) viewHolder;
            int i2 = -1;
            ItemRecentGroup itemRecentGroup = this.arrShow.get(i - 1);
            String str = itemRecentGroup.arrRecent.get(0).simId;
            if (this.arrSim.size() > 1 && str != null && !str.isEmpty()) {
                int i3 = 0;
                while (true) {
                    if (i3 >= this.arrSim.size()) {
                        break;
                    } else if (str.equals(this.arrSim.get(i3).handle.getId())) {
                        i2 = i3;
                        break;
                    } else {
                        i3++;
                    }
                }
            }
            holderItem.layoutRecent.setItemRecent(itemRecentGroup, i2, this.isChoose, this.theme);
            if (this.isChoose) {
                holderItem.sw.setSwipeFlags(0);
                holderItem.sw.closeRightMenu(true);
                return;
            }
            holderItem.sw.setSwipeFlags(1);
        }
    }

    @Override 
    public int getItemCount() {
        return this.arrShow.size() + 1;

    }

    
    class HolderTop extends RecyclerView.ViewHolder {
        public HolderTop(LayoutItemTopRecent layoutItemTopRecent) {
            super(layoutItemTopRecent);
        }
    }

    
    
    public class HolderItem extends RecyclerView.ViewHolder {
        LayoutRecent layoutRecent;
        SwipeLayout sw;

        public HolderItem(View view) {
            super(view);

            long start = System.currentTimeMillis();
            this.sw = (SwipeLayout) view;
            LayoutRecent layoutRecent = (LayoutRecent) view.findViewById(R.id.content);
            this.layoutRecent = layoutRecent;
            layoutRecent.setFavOnItemClick(new FavOnItemClick() { 
                @Override 
                public void onLongClick() {
                    if (AdapterRecent.this.isChoose) {
                        return;
                    }
                    AdapterRecent.this.recentItemClick.onLongClick((ItemRecentGroup) AdapterRecent.this.arrShow.get(HolderItem.this.getLayoutPosition() - 1));
                }

                @Override 
                public void onItemClick() {
                    if (AdapterRecent.this.isChoose) {
                        return;
                    }
                    AdapterRecent.this.recentItemClick.onItemClick((ItemRecentGroup) AdapterRecent.this.arrShow.get(HolderItem.this.getLayoutPosition() - 1));
                }

                @Override 
                public void onDel() {
                    AdapterRecent.this.recentItemClick.onDel((ItemRecentGroup) AdapterRecent.this.arrShow.get(HolderItem.this.getLayoutPosition() - 1));
                }

                @Override 
                public void onInfo() {
                    AdapterRecent.this.recentItemClick.onInfo((ItemRecentGroup) AdapterRecent.this.arrShow.get(HolderItem.this.getLayoutPosition() - 1));
                }
            });
            TextW textW = (TextW) view.findViewById(R.id.right_menu);
            textW.setupText(400, 4.0f);
            textW.setTextColor(-1);
            textW.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    HolderItem.this.m59xc5a92fdd(view2);
                }
            });
            this.layoutRecent.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    HolderItem.this.m60xb6fabf5e(view2);
                }
            });
            Log.d("VH_CREATE", "HolderItem ctor = " + (System.currentTimeMillis()-start) + " ms");
        }

        
        
        public  void m59xc5a92fdd(View view) {
            AdapterRecent.this.recentItemClick.onDel((ItemRecentGroup) AdapterRecent.this.arrShow.get(getLayoutPosition() - 1));
        }

        
        
        public  void m60xb6fabf5e(View view) {
            if (this.sw.isRightMenuOpened()) {
                return;
            }
            AdapterRecent.this.recentItemClick.onItemClick((ItemRecentGroup) AdapterRecent.this.arrShow.get(getLayoutPosition() - 1));
        }
    }
}
