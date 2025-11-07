package com.thelinkphone.app.adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.custom.ViewItemTheme;
import com.thelinkphone.app.item.ItemTheme;

import java.util.ArrayList;


public class AdapterTheme extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ItemTheme> arrTheme;
    private final OnThemeItemClick onThemeItemClick;
    private String path;

    
    public interface OnThemeItemClick {
        void onDownloadClick(ItemTheme itemTheme);

        void onItemClick(ItemTheme itemTheme);
    }

    public AdapterTheme(ArrayList<ItemTheme> arrayList, String str, OnThemeItemClick onThemeItemClick) {
        this.arrTheme = arrayList;
        this.onThemeItemClick = onThemeItemClick;
        this.path = str;
    }

    public void updatePath(String str) {
        if (this.path.equals(str)) {
            return;
        }
        this.path = str;
        notifyDataSetChanged();
    }

    @Override 
    public int getItemViewType(int i) {
        return this.arrTheme.get(i) == null ? 0 : 1;
    }

    @Override 
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 0) {
            return null;
        }
        return new Holder(new ViewItemTheme(viewGroup.getContext()));
    }

    @Override 
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof Holder) {
            ((Holder) viewHolder).v.setItemTheme(this.arrTheme.get(i), this.path);
        }
    }

    @Override 
    public int getItemCount() {
        return this.arrTheme.size();
    }

    
    
    public class Holder extends RecyclerView.ViewHolder {
        ViewItemTheme v;

        public Holder(ViewItemTheme viewItemTheme) {
            super(viewItemTheme);
            this.v = viewItemTheme;
            viewItemTheme.setDownloadClick(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    Holder.this.m63x78e288f2(view);
                }
            });
            viewItemTheme.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    Holder.this.m64xf7438cd1(view);
                }
            });
        }

        
        
        public  void m63x78e288f2(View view) {
            AdapterTheme.this.onThemeItemClick.onDownloadClick((ItemTheme) AdapterTheme.this.arrTheme.get(getLayoutPosition()));
        }

        
        
        public  void m64xf7438cd1(View view) {
            AdapterTheme.this.onThemeItemClick.onItemClick((ItemTheme) AdapterTheme.this.arrTheme.get(getLayoutPosition()));
        }
    }

}
