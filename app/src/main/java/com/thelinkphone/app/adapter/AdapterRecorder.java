package com.thelinkphone.app.adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.custom.ViewItemRecorder;
import com.thelinkphone.app.item.ItemRecorder;
import java.util.ArrayList;
import java.util.Iterator;


public class AdapterRecorder extends RecyclerView.Adapter<AdapterRecorder.Holder> {
    private final ArrayList<ItemRecorder> arrRecorder;
    private final OnItemClick onItemClick;
    private final boolean theme;
    private final ArrayList<Integer> arrChoose = new ArrayList<>();
    private boolean modeDefault = true;

    
    public interface OnItemClick {
        void onItemClick(int i);
    }

    public AdapterRecorder(ArrayList<ItemRecorder> arrayList, boolean z, OnItemClick onItemClick) {
        this.arrRecorder = arrayList;
        this.onItemClick = onItemClick;
        this.theme = z;
    }

    public void setModeDefault(boolean z) {
        this.modeDefault = z;
        this.arrChoose.clear();
        notifyDataSetChanged();
    }

    public boolean isModeDefault() {
        return this.modeDefault;
    }

    public ArrayList<Integer> getArrChoose() {
        return this.arrChoose;
    }

    @Override 
    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new Holder(new ViewItemRecorder(viewGroup.getContext()));
    }

    @Override 
    public void onBindViewHolder(Holder holder, int i) {
        boolean z;
        if (!this.modeDefault) {
            Iterator<Integer> it = this.arrChoose.iterator();
            while (it.hasNext()) {
                if (it.next().intValue() == i) {
                    z = true;
                    break;
                }
            }
        }
        z = false;
        holder.v.setItemRecorder(this.arrRecorder.get(i), this.modeDefault, z, this.theme);
    }

    @Override 
    public int getItemCount() {
        return this.arrRecorder.size();
    }

    
    
    public class Holder extends RecyclerView.ViewHolder {
        ViewItemRecorder v;

        public Holder(ViewItemRecorder viewItemRecorder) {
            super(viewItemRecorder);
            this.v = viewItemRecorder;
            viewItemRecorder.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    Holder.this.m61xf08c54a7(view);
                }
            });
        }

        
        
        public  void m61xf08c54a7(View view) {
            int layoutPosition = getLayoutPosition();
            if (AdapterRecorder.this.modeDefault) {
                AdapterRecorder.this.onItemClick.onItemClick(layoutPosition);
                return;
            }
            boolean z = true;
            Iterator it = AdapterRecorder.this.arrChoose.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Integer num = (Integer) it.next();
                if (num.intValue() == layoutPosition) {
                    z = false;
                    AdapterRecorder.this.arrChoose.remove(num);
                    break;
                }
            }
            if (z) {
                AdapterRecorder.this.arrChoose.add(Integer.valueOf(layoutPosition));
            }
            AdapterRecorder.this.notifyItemChanged(layoutPosition);
        }
    }
}
