package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import coil.Coil;
import coil.request.ImageRequest;
import coil.transform.CircleCropTransformation;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.util.LruCache;

import com.thelinkphone.app.R;

public class LinkPreviewHelper {

    private static final String TAG = "LinkPreviewHelper";
    private static final int JSOUP_TIMEOUT = 6000;
    private static final long CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000; // 24h
    private static final int MEMORY_CACHE_SIZE = 50; // Number of cached URLs

    private static final LruCache<String, LinkPreviewData> memoryCache =
            new LruCache<>(MEMORY_CACHE_SIZE);

    private static final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    public static void loadLinkPreview(String url, TextView titleView, ImageView imageView) {
        loadLinkPreview(url, titleView, imageView, null);
    }

    public static void loadLinkPreview(String url, TextView titleView, ImageView imageView, Runnable onComplete) {
        new Thread(() -> {
            LinkPreviewData data = fetchLinkPreview(url, titleView.getContext());

            titleView.post(() -> {
                titleView.setText(data.title.isEmpty() ? "No title available" : data.title);

                if (!data.imageUrl.isEmpty()) {
                    loadImageWithCoil(data.imageUrl, imageView, titleView.getContext());
                } else {
                    imageView.setImageResource(R.drawable.circle_avatar_background);
                }

                if (onComplete != null) onComplete.run();
            });
        }).start();
    }


    private static LinkPreviewData fetchLinkPreview(String url, Context context) {
        LinkPreviewData data;

        data = memoryCache.get(url);
        if (data != null && data.isValid()) {
            Log.d(TAG, "⚡ Cache hit (memory) for " + url);
            return data;
        }

        LinkPreviewData cached = getCachedFromDisk(url, context);
        if (cached != null && cached.isValid()) {
            Log.d(TAG, "💾 Cache hit (disk) for " + url);
            memoryCache.put(url, cached);
            return cached;
        }

        try {
            data = fetchWithMicrolink(url);
            if (!data.isValid()) data = fetchWithEnhancedJsoup(url);
            if (!data.isValid()) data = fetchWithOkHttp(url);
        } catch (Exception e) {
            Log.e(TAG, "All methods failed: " + e.getMessage());
            data = new LinkPreviewData();
        }

        if (data.isValid()) {
            memoryCache.put(url, data);
            cacheToDisk(url, data, context);
        }

        return data;
    }


    private static LinkPreviewData fetchWithMicrolink(String url) {
        Response response = null;
        try {
            String apiUrl = "https://api.microlink.io/?url=" + URLEncoder.encode(url, "UTF-8");

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .build();

            response = httpClient.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String jsonStr = response.body().string();
                JSONObject jsonResponse = new JSONObject(jsonStr);

                if (jsonResponse.getString("status").equals("success")) {
                    JSONObject data = jsonResponse.getJSONObject("data");

                    LinkPreviewData preview = new LinkPreviewData();
                    preview.title = data.optString("title", "");
                    preview.description = data.optString("description", "");

                    Object imageObj = data.opt("image");
                    if (imageObj instanceof JSONObject) {
                        preview.imageUrl = ((JSONObject) imageObj).optString("url", "");
                    } else if (imageObj instanceof String) {
                        preview.imageUrl = (String) imageObj;
                    }

                    if (preview.imageUrl.isEmpty() && data.has("logo")) {
                        Object logoObj = data.opt("logo");
                        if (logoObj instanceof JSONObject) {
                            preview.imageUrl = ((JSONObject) logoObj).optString("url", "");
                        } else if (logoObj instanceof String) {
                            preview.imageUrl = (String) logoObj;
                        }
                    }

                    response.close();
                    return preview;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Microlink API failed: " + e.getMessage());
        } finally {
            if (response != null) response.close();
        }

        return new LinkPreviewData();
    }


    private static LinkPreviewData fetchWithEnhancedJsoup(String url) {
        LinkPreviewData preview = new LinkPreviewData();

        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .referrer("https://www.google.com/")
                    .timeout(JSOUP_TIMEOUT)
                    .followRedirects(true)
                    .ignoreHttpErrors(false)
                    .get();

            preview.title = doc.select("meta[property=og:title]").attr("content");
            preview.description = doc.select("meta[property=og:description]").attr("content");
            preview.imageUrl = doc.select("meta[property=og:image]").attr("content");

            if (preview.title.isEmpty()) preview.title = doc.title();
            if (preview.imageUrl.isEmpty())
                preview.imageUrl = doc.select("meta[name=twitter:image]").attr("content");

        } catch (Exception e) {
            Log.e(TAG, "Jsoup failed: " + e.getMessage());
        }

        return preview;
    }

    private static LinkPreviewData fetchWithOkHttp(String url) {
        LinkPreviewData preview = new LinkPreviewData();
        Response response = null;

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .header("Accept", "text/html")
                    .build();

            response = httpClient.newCall(request).execute();

            if (response.isSuccessful() && response.body() != null) {
                String html = response.body().string();
                Document doc = Jsoup.parse(html, url);

                preview.title = doc.select("meta[property=og:title]").attr("content");
                if (preview.title.isEmpty()) preview.title = doc.title();

                preview.imageUrl = doc.select("meta[property=og:image]").attr("content");
                preview.description = doc.select("meta[property=og:description]").attr("content");
            }

        } catch (Exception e) {
            Log.e(TAG, "OkHttp failed: " + e.getMessage());
        } finally {
            if (response != null) response.close();
        }

        return preview;
    }


    private static void loadImageWithCoil(String imageUrl, ImageView imageView, Context context) {
        ImageRequest request = new ImageRequest.Builder(context)
                .data(imageUrl)
                .placeholder(R.drawable.circle_avatar_background)
                .error(R.drawable.circle_avatar_background)
                .transformations(new CircleCropTransformation())
                .target(imageView)
                .build();

        Coil.imageLoader(context).enqueue(request);
    }

    private static void cacheToDisk(String url, LinkPreviewData data, Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("link_preview_cache", Context.MODE_PRIVATE);
            JSONObject obj = new JSONObject();
            obj.put("title", data.title);
            obj.put("desc", data.description);
            obj.put("img", data.imageUrl);
            obj.put("ts", System.currentTimeMillis());
            prefs.edit().putString(url, obj.toString()).apply();
        } catch (Exception e) {
            Log.e(TAG, "Disk cache save failed: " + e.getMessage());
        }
    }

    private static LinkPreviewData getCachedFromDisk(String url, Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("link_preview_cache", Context.MODE_PRIVATE);
            String json = prefs.getString(url, null);
            if (json == null) return null;

            JSONObject obj = new JSONObject(json);
            long ts = obj.optLong("ts", 0);
            if (System.currentTimeMillis() - ts > CACHE_EXPIRY_MS) {
                prefs.edit().remove(url).apply();
                return null;
            }

            LinkPreviewData data = new LinkPreviewData();
            data.title = obj.optString("title", "");
            data.description = obj.optString("desc", "");
            data.imageUrl = obj.optString("img", "");
            return data;

        } catch (Exception e) {
            Log.e(TAG, "Disk cache read failed: " + e.getMessage());
            return null;
        }
    }

    private static class LinkPreviewData {
        String title = "";
        String description = "";
        String imageUrl = "";

        boolean isValid() {
            return !title.isEmpty() || !imageUrl.isEmpty();
        }
    }
}
