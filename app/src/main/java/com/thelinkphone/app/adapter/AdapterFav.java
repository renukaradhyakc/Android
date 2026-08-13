package com.thelinkphone.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.aitsuki.swipe.SwipeLayout;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.FavOnItemClick;
import com.thelinkphone.app.custom.LayoutItemFav;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.item.ItemFavorites;


import java.util.ArrayList;


public class AdapterFav extends RecyclerView.Adapter<AdapterFav.Holder> {
    private final ArrayList<ItemFavorites> arrFav;
    private final FavItemClick favItemClick;
    private boolean isChoose = false;
    private final boolean theme;

    
    public interface FavItemClick {
        void onDel(ItemFavorites itemFavorites);

        void onInfo(ItemFavorites itemFavorites);

        void onItemClick(ItemFavorites itemFavorites);

        void onLongClick(ItemFavorites itemFavorites);
    }

    public AdapterFav(ArrayList<ItemFavorites> arrayList, boolean z, FavItemClick favItemClick) {
        this.arrFav = arrayList;
        this.favItemClick = favItemClick;
        this.theme = z;
    }

    @Override 
    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new Holder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_fav, viewGroup, false));
    }

    @Override 
    public void onBindViewHolder(Holder holder, int i) {
        holder.layoutItemFav.setFav(this.arrFav.get(i), this.isChoose, this.theme);
        if (this.isChoose) {
            holder.sw.setSwipeFlags(0);
            holder.sw.closeRightMenu(true);
            return;
        }
        holder.sw.setSwipeFlags(1);
    }

    public void setChoose(boolean z) {
        this.isChoose = z;
        notifyItemRangeChanged(0, getItemCount(), true);
    }

    public boolean isChoose() {
        return this.isChoose;
    }

    @Override 
    public int getItemCount() {
        return this.arrFav.size();
    }

    
    
    public class Holder extends RecyclerView.ViewHolder {
        LayoutItemFav layoutItemFav;
        SwipeLayout sw;

        public Holder(View view) {
            super(view);
            this.sw = (SwipeLayout) view;
            LayoutItemFav layoutItemFav = (LayoutItemFav) view.findViewById(R.id.content);
            this.layoutItemFav = layoutItemFav;
            layoutItemFav.setFavOnItemClick(new FavOnItemClick() { 
                @Override 
                public void onLongClick() {
                    if (AdapterFav.this.isChoose) {
                        return;
                    }
                    AdapterFav.this.favItemClick.onLongClick((ItemFavorites) AdapterFav.this.arrFav.get(Holder.this.getLayoutPosition()));
                }

                @Override 
                public void onItemClick() {
                    if (AdapterFav.this.isChoose) {
                        return;
                    }
                    AdapterFav.this.favItemClick.onItemClick((ItemFavorites) AdapterFav.this.arrFav.get(Holder.this.getLayoutPosition()));
                }

                @Override 
                public void onDel() {
                    AdapterFav.this.favItemClick.onDel((ItemFavorites) AdapterFav.this.arrFav.get(Holder.this.getLayoutPosition()));
                }

                @Override 
                public void onInfo() {
                    AdapterFav.this.favItemClick.onInfo((ItemFavorites) AdapterFav.this.arrFav.get(Holder.this.getLayoutPosition()));
                }
            });
            TextW textW = (TextW) view.findViewById(R.id.right_menu);
            textW.setupText(400, 4.0f);
            textW.setTextColor(ContextCompat.getColor(view.getContext(), R.color.white));
            textW.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    Holder.this.m57x528d3220(view2);
                }
            });
            this.layoutItemFav.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view2) {
                    Holder.this.m58xe6cba1bf(view2);
                }
            });
        }

        
        
        public  void m57x528d3220(View view) {
            AdapterFav.this.favItemClick.onDel((ItemFavorites) AdapterFav.this.arrFav.get(getLayoutPosition()));
        }

        
        
        public  void m58xe6cba1bf(View view) {
            if (this.sw.isRightMenuOpened()) {
                return;
            }
            AdapterFav.this.favItemClick.onItemClick((ItemFavorites) AdapterFav.this.arrFav.get(getLayoutPosition()));
        }
    }
}
