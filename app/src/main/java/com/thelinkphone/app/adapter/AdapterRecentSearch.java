package com.thelinkphone.app.adapter;

import android.content.Context;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.custom.FavOnItemClick;
import com.thelinkphone.app.custom.LayoutRecent;
import com.thelinkphone.app.item.ItemRecentGroup;
import com.thelinkphone.app.utils.CallBlockReasonResolver;

import java.util.ArrayList;

public class AdapterRecentSearch extends RecyclerView.Adapter<AdapterRecentSearch.HolderResult> {

    public interface OnResultClick {
        void onResultClick(ItemRecentGroup group);
    }

    private final ArrayList<ItemRecentGroup> results;
    private final boolean theme;
    private final int mode;
    private final OnResultClick onResultClick;
    private String currentQuery = "";
    private CallBlockReasonResolver blockReasonResolver;

    public AdapterRecentSearch(ArrayList<ItemRecentGroup> results, boolean theme, int mode, OnResultClick onResultClick) {
        this.results = results;
        this.theme = theme;
        this.mode = mode;
        this.onResultClick = onResultClick;
    }

    public void setBlockReasonResolver(CallBlockReasonResolver resolver) {
        this.blockReasonResolver = resolver;
    }

    public void updateResults(ArrayList<ItemRecentGroup> newResults, String query) {
        this.results.clear();
        this.results.addAll(newResults);
        this.currentQuery = query;
        notifyDataSetChanged();
    }

    @Override
    public HolderResult onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutRecent layoutRecent = new LayoutRecent(context);
        layoutRecent.setLayoutParams(new RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        return new HolderResult(layoutRecent);
    }

    @Override
    public void onBindViewHolder(HolderResult holder, int position) {
        final ItemRecentGroup group = results.get(position);

        holder.layoutRecent.setItemRecent(group, -1, false, theme, mode, blockReasonResolver);
        holder.layoutRecent.applySearchHighlight(currentQuery);

        holder.layoutRecent.setFavOnItemClick(new FavOnItemClick() {
            @Override
            public void onLongClick() {}

            @Override
            public void onItemClick() {
                if (onResultClick != null) onResultClick.onResultClick(group);
            }

            @Override
            public void onDel() {}

            @Override
            public void onInfo() {
                if (onResultClick != null) onResultClick.onResultClick(group);
            }
        });


        holder.layoutRecent.setOnClickListener(v -> {
            if (onResultClick != null) onResultClick.onResultClick(group);
        });
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    static class HolderResult extends RecyclerView.ViewHolder {
        LayoutRecent layoutRecent;
        HolderResult(LayoutRecent layoutRecent) {
            super(layoutRecent);
            this.layoutRecent = layoutRecent;
        }
    }
}