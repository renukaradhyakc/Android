package com.thelinkphone.app.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.ActivityShowVideo;
import com.thelinkphone.app.R;
import com.thelinkphone.app.adapter.AdapterTheme;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.dialog.DialogChooseGallery;
import com.thelinkphone.app.dialog.LoadingDialog;
import com.thelinkphone.app.item.ItemTheme;
import com.thelinkphone.app.service.FileDownloadService;
import com.thelinkphone.app.utils.FileUltils;
import com.thelinkphone.app.utils.MyConst;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class FragmentWallpaper extends Fragment {
    private static final int REQUEST_IMAGE = 1;
    private static final int REQUEST_VIDEO = 2;
    private AdapterTheme adapterTheme;
    private ArrayList<ItemTheme> arrTheme;
    private LoadingDialog dialogLoading;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() { 
        @Override 
        public final void onActivityResult(Object obj) {
            try {
                FragmentWallpaper.this.m174x81ac195b((ActivityResult) obj);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
    private String name;
    private String path;
    private String pathImage;
    private int requestGallery;
    private ViewWallpaper viewWallpaper;

    @Override 
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        if (this.viewWallpaper == null) {
            this.viewWallpaper = new ViewWallpaper(layoutInflater.getContext());
        }

        return this.viewWallpaper;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Remove AdMob initialization
    }

    @Override 
    public void onResume() {
        super.onResume();
        this.adapterTheme.updatePath(MyShare.getPhoto(getContext()));
    }

    
    
    public  void m174x81ac195b(ActivityResult activityResult) throws IOException {
        if (activityResult.getResultCode() != -1 || activityResult.getData() == null || activityResult.getData().getData() == null || getActivity() == null) {
            return;
        }
        int i = this.requestGallery;
        if (i != 1) {
            if (i == 2) {
                MyShare.putPhoto(getActivity(), FileUltils.getPath(getActivity(), activityResult.getData().getData()));
                Toast.makeText(getActivity(), (int) R.string.done, Toast.LENGTH_SHORT).show();
                this.adapterTheme.updatePath("");
                return;
            }
            return;
        }
        try {
            Point point = new Point();
            ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
            String pathImage = OtherUtils.getPathImage(getActivity());
            File file = new File(pathImage);
            if (!file.exists()) {
                file.mkdir();
            }
            this.name = System.currentTimeMillis() + "";
            this.pathImage = pathImage + this.name + ".jpg";
            if (getContext() != null) {
                UCrop.of(activityResult.getData().getData(), Uri.fromFile(new File(this.pathImage))).withAspectRatio(point.x, point.y).withMaxResultSize(1500, 1500).start(getContext(), this);
            } else {
                Toast.makeText(getActivity(), (int) R.string.error_load_image, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception unused) {
            Toast.makeText(getActivity(), (int) R.string.error_load_image, Toast.LENGTH_SHORT).show();
        }
    }

    @Override 
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i2 == -1 && i == 69 && getContext() != null) {
            try {
                MyShare.putPhoto(getContext(), this.pathImage);
                this.adapterTheme.updatePath(this.pathImage);
                Toast.makeText(getContext(), (int) R.string.done, Toast.LENGTH_SHORT).show();
                new Thread(new Runnable() { 
                    @Override 
                    public final void run() {
                        FragmentWallpaper.this.m175x6a2d6961();
                    }
                }).start();
            } catch (Exception unused) {
                OtherUtils.deleteFile(this.pathImage);
                Toast.makeText(getContext(), (int) R.string.error_load_image, Toast.LENGTH_SHORT).show();
            }
        }
    }

    
    
    public  void m175x6a2d6961() {
        File[] listFiles = new File(OtherUtils.getPathImage(getActivity())).listFiles();
        if (listFiles == null || listFiles.length <= 0) {
            return;
        }
        for (File file : listFiles) {
            if (!file.getName().contains(this.name)) {
                file.delete();
            }
        }
    }

    
    
    public class ViewWallpaper extends RelativeLayout implements AdapterTheme.OnThemeItemClick {
        public ViewWallpaper(Context context) {
            super(context);
            FragmentWallpaper.this.dialogLoading = new LoadingDialog(context);
            int widthScreen = OtherUtils.getWidthScreen(context);
            int i = widthScreen / 25;
            boolean theme = MyShare.getTheme(context);
            int sizeNotification = MyShare.getSizeNotification(context);
            TextW textW = new TextW(getContext());
            textW.setId(6666);
            textW.setupText(600, 8.0f);
            textW.setText(R.string.wallpaper);
            textW.setPadding(i, sizeNotification, 0, 0);
            addView(textW, -2, -2);
            TextW textW2 = new TextW(context);
            textW2.setGravity(16);
            textW2.setTextColor(Color.parseColor("#007AFF"));
            textW2.setTextSize(0, (widthScreen * 4.2f) / 100.0f);
            textW2.setPadding(i, sizeNotification, i, 0);
            textW2.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewWallpaper.this.m178xa9d2c534(view);
                }
            });
            textW2.setText(R.string.custom);
            LayoutParams layoutParams = new LayoutParams(-2, -1);
            layoutParams.addRule(21);
            layoutParams.addRule(6, textW.getId());
            layoutParams.addRule(8, textW.getId());
            addView(textW2, layoutParams);
            FragmentWallpaper.this.path = OtherUtils.getPathTheme(context);
            makePath();
            loadAllThemeOffline();
            FragmentWallpaper.this.adapterTheme = new AdapterTheme(FragmentWallpaper.this.arrTheme, MyShare.getPhoto(context), this);
            RecyclerView recyclerView = new RecyclerView(context);
            recyclerView.setAdapter(FragmentWallpaper.this.adapterTheme);
            LayoutParams layoutParams2 = new LayoutParams(-1, -1);
            int i2 = widthScreen / 50;
            layoutParams2.setMargins(i2, i / 2, i2, 0);
            layoutParams2.addRule(3, textW.getId());
            addView(recyclerView, layoutParams2);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, RecyclerView.VERTICAL, false);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() { 
                @Override 
                public int getSpanSize(int i3) {
                    return FragmentWallpaper.this.adapterTheme.getItemViewType(i3) == 0 ? 2 : 1;
                }
            });
            recyclerView.setLayoutManager(gridLayoutManager);
            loadThemeOnline(context);
            if (theme) {
                textW.setTextColor(-16777216);
                setBackgroundColor(-1);
                return;
            }
            textW.setTextColor(-1);
            setBackgroundColor(Color.parseColor("#2C2C2C"));
        }

        
        
        public  void m178xa9d2c534(View view) {
            onCustomClick();
        }

        private void onCustomClick() {
            String[] strArr;
            if (Build.VERSION.SDK_INT >= 29) {
                strArr = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
            } else {
                strArr = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
            }
            int length = strArr.length;
            boolean z = false;
            int i = 0;
            while (true) {
                if (i >= length) {
                    z = true;
                    break;
                }
                if (!OtherUtils.checkPer(getContext(), strArr[i])) {
                    break;
                }
                i++;
            }
            if (!z) {
                FragmentWallpaper.this.requestPermissions(strArr, 1);
                return;
            }
            makePath();
            new DialogChooseGallery(getContext(), R.string.image, R.string.video, new DialogChooseGallery.GalleryResult() { 
                @Override 
                public void onImageClick() {
                    try {
                        FragmentWallpaper.this.requestGallery = 1;
                        OtherUtils.pickImage(FragmentWallpaper.this.launcher);
                    } catch (Exception unused) {
                        Toast.makeText(ViewWallpaper.this.getContext(), (int) R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override 
                public void onVideoClick() {
                    try {
                        FragmentWallpaper.this.requestGallery = 2;
                        Intent addCategory = new Intent("android.intent.action.OPEN_DOCUMENT").setType("video/*").addCategory("android.intent.category.OPENABLE");
                        addCategory.putExtra("android.intent.extra.MIME_TYPES", new String[]{"video/*"});
                        FragmentWallpaper.this.launcher.launch(addCategory);
                    } catch (Exception unused) {
                        Toast.makeText(ViewWallpaper.this.getContext(), (int) R.string.error, Toast.LENGTH_SHORT).show();
                    }
                }
            }).show();
        }

        private void loadAllThemeOffline() {
            String[] list;
            FragmentWallpaper.this.arrTheme = new ArrayList();
            if (!OtherUtils.checkPer(getContext(), "android.permission.READ_EXTERNAL_STORAGE") || (list = new File(FragmentWallpaper.this.path).list()) == null || list.length <= 0) {
                return;
            }
            for (String str : list) {
                FragmentWallpaper.this.arrTheme.add(new ItemTheme(str, FragmentWallpaper.this.path + str, ""));
            }
        }

        private void loadThemeOnline(final Context context) {
            final Handler handler = new Handler(new Handler.Callback() { 
                @Override 
                public final boolean handleMessage(Message message) {
                    return ViewWallpaper.this.m176x54f423cb(message);
                }
            });
            new Thread(new Runnable() { 
                @Override 
                public final void run() {
                    ViewWallpaper.this.m177x55c2a24c(context, handler);
                }
            }).start();
        }

        
        
        public  boolean m176x54f423cb(Message message) {
            FragmentWallpaper.this.adapterTheme.notifyDataSetChanged();
            return true;
        }

        
        
        public  void m177x55c2a24c(Context context, Handler handler) {
            try {
                ArrayList arrayList = (ArrayList) new Gson().fromJson(OtherUtils.getTextInAssets(context, "th.json"), new TypeToken<ArrayList<ItemTheme>>() { 
                }.getType());
                if (arrayList != null) {
                    if (FragmentWallpaper.this.arrTheme.size() > 0) {
                        Iterator it = FragmentWallpaper.this.arrTheme.iterator();
                        while (it.hasNext()) {
                            ItemTheme itemTheme = (ItemTheme) it.next();
                            Iterator it2 = arrayList.iterator();
                            while (true) {
                                if (it2.hasNext()) {
                                    ItemTheme itemTheme2 = (ItemTheme) it2.next();
                                    if (itemTheme != null && itemTheme.getName().contains(itemTheme2.getName())) {
                                        arrayList.remove(itemTheme2);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    FragmentWallpaper.this.arrTheme.addAll(arrayList);
                }
            } catch (Exception unused) {
            }
            handler.sendEmptyMessage(1);
        }

        @Override 
        public void onDownloadClick(final ItemTheme itemTheme) {
            String[] strArr;
            boolean z;
            if (itemTheme.getThumb() == null || itemTheme.getThumb().isEmpty()) {
                onItemClick(itemTheme);
                return;
            }
            if (Build.VERSION.SDK_INT >= 29) {
                strArr = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
            } else {
                strArr = new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
            }
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    z = true;
                    break;
                }
                if (!OtherUtils.checkPer(getContext(), strArr[i])) {
                    z = false;
                    break;
                }
                i++;
            }
            if (!z) {
                FragmentWallpaper.this.requestPermissions(strArr, 1);
                return;
            }
            makePath();
            if (getContext() == null || isNetworkAvailable(getContext())) {
                FragmentWallpaper.this.dialogLoading.show();
                final String str = FragmentWallpaper.this.path + itemTheme.getName() + itemTheme.getLink().substring(itemTheme.getLink().lastIndexOf(FileUltils.HIDDEN_PREFIX));
                FileDownloadService.DownloadRequest downloadRequest = new FileDownloadService.DownloadRequest(itemTheme.getLink(), str);
                downloadRequest.setRequiresUnzip(false);
                downloadRequest.setDeleteZipAfterExtract(false);
                FileDownloadService.FileDownloader.getInstance(downloadRequest, new FileDownloadService.OnDownloadStatusListener() { 
                    @Override 
                    public void onDownloadStarted() {
                    }

                    @Override 
                    public void onDownloadCompleted() {
                        if (FragmentWallpaper.this.dialogLoading != null && FragmentWallpaper.this.dialogLoading.isShowing()) {
                            FragmentWallpaper.this.dialogLoading.cancel();
                        }
                        itemTheme.setThumb(null);
                        itemTheme.setLink(str);
                        FragmentWallpaper.this.adapterTheme.notifyItemChanged(FragmentWallpaper.this.arrTheme.indexOf(itemTheme));
                    }

                    @Override 
                    public void onDownloadFailed() {
                        if (FragmentWallpaper.this.dialogLoading != null && FragmentWallpaper.this.dialogLoading.isShowing()) {
                            FragmentWallpaper.this.dialogLoading.cancel();
                        }
                        Toast.makeText(ViewWallpaper.this.getContext(), (int) R.string.error, Toast.LENGTH_SHORT).show();
                    }

                    @Override 
                    public void onDownloadProgress(int i2) {
                        if (FragmentWallpaper.this.dialogLoading == null || !FragmentWallpaper.this.dialogLoading.isShowing()) {
                            return;
                        }
                        LoadingDialog loadingDialog = FragmentWallpaper.this.dialogLoading;
                        loadingDialog.setText(i2 + "%");
                    }
                }).download(getContext());
                return;
            }
            Toast.makeText(getContext(), (int) R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
        boolean isNetworkAvailable(Context cc) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) cc.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        @Override 
        public void onItemClick(final ItemTheme itemTheme) {
            if (itemTheme.getThumb() == null || itemTheme.getThumb().isEmpty()) {
                if (FragmentWallpaper.this.getActivity() instanceof ActivityHome) {

                            ViewWallpaper.this.m179xbbde98ad(itemTheme);

                } else {
                    m179xbbde98ad(itemTheme);
                }
            }
        }

        
        
        public void m179xbbde98ad(ItemTheme itemTheme) {
            Intent intent = new Intent(getContext(), ActivityShowVideo.class);
            intent.putExtra(MyConst.DATA_NAME, itemTheme.getLink());
            FragmentWallpaper.this.startActivity(intent);
        }

        private void makePath() {
            File file = new File(FragmentWallpaper.this.path);
            if (file.exists()) {
                return;
            }
            file.mkdirs();
        }
    }
}
