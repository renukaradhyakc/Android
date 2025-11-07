package com.thelinkphone.app.adapter;

import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.custom.ViewAlphaB;
import com.thelinkphone.app.custom.ViewListContact;
import com.thelinkphone.app.item.ItemContact;
import com.thelinkphone.app.item.ItemPhone;
import com.thelinkphone.app.item.ItemShowContact;
import com.thelinkphone.app.utils.OtherUtils;
import java.util.ArrayList;
import java.util.Iterator;


public class AdapterContactHome extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<ItemContact> arrContact;
    private final ArrayList<ItemShowContact> arrFilter = new ArrayList<>();
    private final ContactResult contactResult;
    private final boolean theme;

    
    public interface ContactResult {
        void onItemClick(ItemContact itemContact);

        void onLongClick(ItemContact itemContact);
    }

    public AdapterContactHome(ArrayList<ItemContact> arrayList, boolean z, ContactResult contactResult) {
        this.arrContact = arrayList;
        this.contactResult = contactResult;
        this.theme = z;
        makeArr("");
    }

    @Override 
    public int getItemViewType(int i) {
        return this.arrFilter.get(i).alphaB != null ? 1 : 0;
    }

    @Override 
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == 1) {
            return new HolderAlphaB(new ViewAlphaB(viewGroup.getContext()));
        }
        return new HolderContact(new ViewListContact(viewGroup.getContext()));
    }

    @Override 
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof HolderAlphaB) {
            ((HolderAlphaB) viewHolder).viewAlphaB.setAlphaB(this.arrFilter.get(i).alphaB);
        } else {
            ((HolderContact) viewHolder).vContact.setContact(this.arrFilter.get(i).itemContact, this.theme);
        }
    }

    @Override 
    public int getItemCount() {
        return this.arrFilter.size();
    }

    public void filter(String str) {
        makeArr(str);
        notifyDataSetChanged();
    }

    private void makeArr(String str) {
        if (str.isEmpty()) {
            addData(this.arrContact);
            return;
        }
        ArrayList<ItemContact> arrayList = new ArrayList<>();
        Iterator<ItemContact> it = this.arrContact.iterator();
        while (it.hasNext()) {
            ItemContact next = it.next();
            if (next.getName() != null && next.getName().toLowerCase().contains(str)) {
                arrayList.add(next);
            } else if (next.getArrPhone() != null && !next.getArrPhone().isEmpty()) {
                Iterator<ItemPhone> it2 = next.getArrPhone().iterator();
                while (true) {
                    if (it2.hasNext()) {
                        ItemPhone next2 = it2.next();
                        if (next2.getNumber() != null && next2.getNumber().toLowerCase().contains(str)) {
                            arrayList.add(next);
                            break;
                        }
                    }
                }
            }
        }
        addData(arrayList);
    }

    private void addData(ArrayList<ItemContact> arrayList) {
        this.arrFilter.clear();
        if (arrayList == null || arrayList.isEmpty()) {
            return;
        }
        String[] arrAlphaB = OtherUtils.arrAlphaB();
        int i = -1;
        Iterator<ItemContact> it = arrayList.iterator();
        while (it.hasNext()) {
            ItemContact next = it.next();
            int posAlphaB = getPosAlphaB(arrAlphaB, next.getName());
            if (i != posAlphaB) {
                this.arrFilter.add(new ItemShowContact(arrAlphaB[posAlphaB]));
                i = posAlphaB;
            }
            this.arrFilter.add(new ItemShowContact(next));
        }
    }

    private int getPosAlphaB(String[] strArr, String str) {
        String substring = (str == null || str.isEmpty()) ? "#" : str.substring(0, 1);
        for (int i = 0; i < strArr.length; i++) {
            if (strArr[i].equalsIgnoreCase(substring)) {
                return i;
            }
        }
        return strArr.length - 1;
    }

    public int getLocationAlphaB(String str) {
        Iterator<ItemShowContact> it = this.arrFilter.iterator();
        while (it.hasNext()) {
            ItemShowContact next = it.next();
            if (next.alphaB != null && next.alphaB.equalsIgnoreCase(str)) {
                return this.arrFilter.indexOf(next);
            }
        }
        return -1;
    }

    
    class HolderAlphaB extends RecyclerView.ViewHolder {
        ViewAlphaB viewAlphaB;

        public HolderAlphaB(ViewAlphaB viewAlphaB) {
            super(viewAlphaB);
            this.viewAlphaB = viewAlphaB;
        }
    }

    
    
    public class HolderContact extends RecyclerView.ViewHolder {
        ViewListContact vContact;

        public HolderContact(ViewListContact viewListContact) {
            super(viewListContact);
            this.vContact = viewListContact;
            viewListContact.setOnClickListener(new View.OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    HolderContact.this.m55x8cab3be4(view);
                }
            });
            viewListContact.setOnLongClickListener(new View.OnLongClickListener() { 
                @Override 
                public final boolean onLongClick(View view) {
                    return HolderContact.this.m56x8d79ba65(view);
                }
            });
        }

        
        
        public  void m55x8cab3be4(View view) {
            AdapterContactHome.this.contactResult.onItemClick(((ItemShowContact) AdapterContactHome.this.arrFilter.get(getLayoutPosition())).itemContact);
        }

        
        
        public  boolean m56x8d79ba65(View view) {
            AdapterContactHome.this.contactResult.onLongClick(((ItemShowContact) AdapterContactHome.this.arrFilter.get(getLayoutPosition())).itemContact);
            return true;
        }
    }
}
