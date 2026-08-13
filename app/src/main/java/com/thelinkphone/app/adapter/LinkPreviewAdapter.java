package com.thelinkphone.app.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.MyConst;

import java.util.ArrayList;
import java.util.List;

public class LinkPreviewAdapter extends RecyclerView.Adapter<LinkPreviewAdapter.ViewHolder> {
    private List<LinkData> links = new ArrayList<>();

    public static class LinkData {
        public String url;
        public String title;
        public String imageUrl;

        public LinkData(String url) {
            this.url = normalizeUrl(url);
            this.title = extractDomain(this.url);
        }
        
        private static String normalizeUrl(String url) {
            if (url == null || url.isEmpty()) return "";
            url = url.trim();
            url = url.replaceAll("^(https?:\\/\\/)+", "https://");

            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            return url;
        }
        
        private static String extractDomain(String url) {
            try {
                java.net.URL u = new java.net.URL(url);
                return u.getHost().replace("www.", "");
            } catch (Exception e) {
                return url;
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_link_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LinkData link = links.get(position);
        
        // Restrict title to 10 characters with ellipsis
        String displayTitle = link.title;
        if (displayTitle.length() > 10) {
            displayTitle = displayTitle.substring(0, 10) + "...";
        }
        holder.tvTitle.setText(displayTitle);

        String faviconUrl = String.format(MyConst.FAVICON_URL_TEMPLATE, link.url);
        
        Glide.with(holder.itemView.getContext())
            .load(faviconUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(R.drawable.ic_link)
            .error(R.drawable.ic_link)
            .into(holder.ivThumbnail);
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), com.thelinkphone.app.WebViewActivity.class);
            intent.putExtra(MyConst.DATA_URL, link.url);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            v.getContext().startActivity(intent);
        });
        
        fetchMetadata(link, position);
    }

    @Override
    public int getItemCount() {
        return links.size();
    }

    public void addLink(String url) {
        for (LinkData existing : links) {
            if (existing.url.equals(LinkData.normalizeUrl(url))) return;
        }
        LinkData linkData = new LinkData(url);
        links.add(0, linkData);
        notifyItemInserted(0);
    }

    private void fetchMetadata(LinkData linkData, int position) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(linkData.url);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(3000);
                conn.setReadTimeout(3000);
                conn.setRequestProperty("User-Agent", MyConst.USER_AGENT);
                
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder html = new StringBuilder();
                String line;
                int lines = 0;
                while ((line = reader.readLine()) != null && lines++ < 100) {
                    html.append(line);
                }
                reader.close();
                
                String title = extractTitle(html.toString());
                String imageUrl = extractImage(html.toString(), linkData.url);
                
                if (title != null && !title.isEmpty()) {
                    linkData.title = title;
                    linkData.imageUrl = imageUrl;
                    
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> notifyItemChanged(position));
                }
            } catch (Exception e) {
                android.util.Log.e("LinkPreviewAdapter", "Error: " + e.getMessage());
            }
        }).start();
    }

    private String extractTitle(String html) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<meta[^>]*property=[\"']og:title[\"'][^>]*content=[\"']([^\"']*)[\"']", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(html);
        if (matcher.find()) return matcher.group(1);
        
        pattern = java.util.regex.Pattern.compile("<title[^>]*>([^<]+)</title>", java.util.regex.Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(html);
        if (matcher.find()) return matcher.group(1).trim();
        
        return null;
    }

    private String extractImage(String html, String baseUrl) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']*)[\"']", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String img = matcher.group(1);
            if (img != null && img.startsWith("http")) return img;
        }
        return null;
    }

    public void clearLinks() {
        links.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvTitle;

        ViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.favicon);
            tvTitle = itemView.findViewById(R.id.domain);
        }
    }
}
