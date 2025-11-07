package com.thelinkphone.app.adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.custom.ViewItemContact;
import com.thelinkphone.app.custom.ViewItemThemeNull;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemPhone;
import java.util.ArrayList;
import java.util.Iterator;


public class AdapterContact extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ItemContact> arrContact;
    private final ArrayList<ItemContact> arrFilter = new ArrayList<>();
    private final ContactResult contactResult;
    private final boolean theme;

    
    public interface ContactResult {
        void onItemContactClick(ItemContact itemContact);
    }

    public AdapterContact(ArrayList<ItemContact> arrayList, boolean z, ContactResult contactResult) {
        this.arrContact = arrayList;
        this.contactResult = contactResult;
        this.theme = z;
    }

    public void updateData() {
        if (this.arrContact.isEmpty()) {
            return;
        }
        this.arrFilter.addAll(this.arrContact);
        notifyDataSetChanged();
    }

    @Override 
    public int getItemViewType(int i) {
        return i == this.arrFilter.size() ? 2 : 0;
    }

    @Override 
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 2) {
            return new HolderBot(new ViewItemThemeNull(viewGroup.getContext()));
        }
        return new Holder(new ViewItemContact(viewGroup.getContext()));
    }

    @Override 
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof Holder) {
            ((Holder) viewHolder).v.setItemContact(this.arrFilter.get(i), this.theme);
        }
    }

    @Override 
    public int getItemCount() {
        return this.arrFilter.size() + 1;
    }

    public void filter(String str) {
        this.arrFilter.clear();
        if (str.isEmpty()) {
            this.arrFilter.addAll(this.arrContact);
        } else {
            Iterator<ItemContact> it = this.arrContact.iterator();
            while (it.hasNext()) {
                ItemContact next = it.next();
                if (next.getName() != null && next.getName().toLowerCase().contains(str)) {
                    this.arrFilter.add(next);
                } else if (next.getArrPhone() != null && !next.getArrPhone().isEmpty()) {
                    Iterator<ItemPhone> it2 = next.getArrPhone().iterator();
                    while (it2.hasNext()) {
                        ItemPhone next2 = it2.next();
                        if (next2.getNumber() != null && (next2.getNumber().replace(" ", "").toLowerCase().contains(str) || next2.getNumber().toLowerCase().contains(str))) {
                            this.arrFilter.add(next);
                            break;
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    
    
    public class Holder extends RecyclerView.ViewHolder {
        ViewItemContact v;

        public Holder(ViewItemContact viewItemContact) {
            super(viewItemContact);
            this.v = viewItemContact;
            viewItemContact.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    Holder.this.m54x71e00bfb(view);
                }
            });
        }

        
        
        public  void m54x71e00bfb(View view) {
            AdapterContact.this.contactResult.onItemContactClick((ItemContact) AdapterContact.this.arrFilter.get(getLayoutPosition()));
        }
    }
}
