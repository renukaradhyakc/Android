package com.thelinkphone.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.model.ContextAndContent;
import com.thelinkphone.app.utils.LinkPreviewHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdapterContextAndContent extends RecyclerView.Adapter<AdapterContextAndContent.StoryViewHolder> {

    private final Context context;
    private final List<ContextAndContent> storyList;
    private static final int INFINITE_MULTIPLIER = 1000;
    private int avatarNameColor = -1;
    private final Set<Integer> loadingPositions = new HashSet<>();

    public AdapterContextAndContent(Context context, List<ContextAndContent> storyList) {
        this.context = context;
        this.storyList = storyList;
        setHasStableIds(true); // prevents flickering during animation
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.context_and_content_component, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        // Use modulo to loop infinitely
        int actualPosition = position % storyList.size();
        ContextAndContent story = storyList.get(actualPosition);

        Log.d("STORY_ADAPTER",
                "onBind -> pos=" + position +
                        ", actual=" + actualPosition +
                        ", title=" + story.getTitle() +
                        ", link=" + story.getLink());

        // Reset state
        holder.avatarName.setText("Loading...");
        holder.avatarImage.setScaleX(1f);
        holder.avatarImage.setScaleY(1f);
        holder.avatarImage.setTranslationY(0f);

        if (avatarNameColor != -1) {
            holder.avatarName.setTextColor(avatarNameColor);
        }

        // --- Keep image centered perfectly inside ring ---
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) holder.avatarImage.getLayoutParams();
        params.gravity = Gravity.CENTER;
        holder.avatarImage.setLayoutParams(params);

        // --- Load link preview safely ---
        synchronized (loadingPositions) {
            if (!loadingPositions.contains(actualPosition)) {
                loadingPositions.add(actualPosition);
                Log.d("STORY_ADAPTER", "Loading link preview for: " + story.getTitle());

                LinkPreviewHelper.loadLinkPreview(story.getLink(),
                        holder.avatarName,
                        holder.avatarImage,
                        () -> {
                            synchronized (loadingPositions) {
                                loadingPositions.remove(actualPosition);
                            }
                            Log.d("STORY_ADAPTER", "✅ Preview loaded for: " + story.getTitle());
                        });
            }
        }

        // --- Handle click event ---
        holder.itemView.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(story.getLink()));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(browserIntent);
        });
    }

    public void setAvatarNameColor(int color) {
        this.avatarNameColor = color;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        // Multiply count for infinite effect
        if(storyList.size()<=4) {
            return storyList.size();
        }
        return storyList.size() * INFINITE_MULTIPLIER;
    }

    @Override
    public long getItemId(int position) {
        // Use stable IDs based on modulo index
        return position % storyList.size();
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        FrameLayout avatarContainer;
        ImageView avatarImage;
        TextView avatarName;
        View avatarRing;


        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarContainer = itemView.findViewById(R.id.avatar_container);
            avatarImage = itemView.findViewById(R.id.avatar_image);
            avatarName = itemView.findViewById(R.id.avatar_name);
            avatarRing = itemView.findViewById(R.id.avatar_ring);
        }
    }

    private int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}

