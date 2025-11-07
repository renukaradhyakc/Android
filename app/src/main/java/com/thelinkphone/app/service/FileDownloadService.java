package com.thelinkphone.app.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FileDownloadService extends IntentService {
    static final  boolean $assertionsDisabled = false;
    private static final String DOWNLOADER_RECEIVER = "downloader_receiver";
    private static final String DOWNLOAD_COMPLETED = "download_completed";
    private static final String DOWNLOAD_DETAILS = "download_details";
    private static final String DOWNLOAD_FAILED = "download_failed";
    private static final String DOWNLOAD_PROGRESS = "download_progress";
    private static final String DOWNLOAD_STARTED = "download_started";
    private static int STATUS_FAILED = 200;
    private static int STATUS_OK = 100;

    
    public interface OnDownloadStatusListener {
        void onDownloadCompleted();

        void onDownloadFailed();

        void onDownloadProgress(int i);

        void onDownloadStarted();
    }

    public FileDownloadService() {
        super("");
    }

    @Override 
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(DOWNLOADER_RECEIVER) && extras.containsKey(DOWNLOAD_DETAILS)) {
            ResultReceiver resultReceiver = (ResultReceiver) extras.getParcelable(DOWNLOADER_RECEIVER);
            DownloadRequest downloadRequest = (DownloadRequest) extras.getParcelable(DOWNLOAD_DETAILS);
            try {
                URL url = new URL(downloadRequest.getServerFilePath());
                URLConnection openConnection = url.openConnection();
                openConnection.connect();
                int contentLength = openConnection.getContentLength();
                Log.d("FileDownloaderService", "Length of file: " + contentLength);
                downloadStarted(resultReceiver);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(url.openStream());
                String localFilePath = downloadRequest.getLocalFilePath();
                FileOutputStream fileOutputStream = new FileOutputStream(localFilePath);
                byte[] bArr = new byte[1024];
                long j = 0;
                while (true) {
                    int read = bufferedInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    j += read;
                    sendProgress((int) ((100 * j) / contentLength), resultReceiver);
                    fileOutputStream.write(bArr, 0, read);
                }
                fileOutputStream.flush();
                fileOutputStream.close();
                bufferedInputStream.close();
                if (downloadRequest.isRequiresUnzip()) {
                    String unzipAtFilePath = downloadRequest.getUnzipAtFilePath();
                    if (unzipAtFilePath == null) {
                        unzipAtFilePath = new File(localFilePath).getParentFile().getAbsolutePath();
                    }
                    unzip(localFilePath, unzipAtFilePath);
                }
                downloadCompleted(resultReceiver);
                if (downloadRequest.isDeleteZipAfterExtract()) {
                    new File(localFilePath).delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                downloadFailed(resultReceiver);
            }
        }
    }

    public void sendProgress(int i, ResultReceiver resultReceiver) {
        Bundle bundle = new Bundle();
        bundle.putInt(DOWNLOAD_PROGRESS, i);
        resultReceiver.send(STATUS_OK, bundle);
    }

    public void downloadStarted(ResultReceiver resultReceiver) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(DOWNLOAD_STARTED, true);
        resultReceiver.send(STATUS_OK, bundle);
    }

    public void downloadCompleted(ResultReceiver resultReceiver) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(DOWNLOAD_COMPLETED, true);
        resultReceiver.send(STATUS_OK, bundle);
    }

    public void downloadFailed(ResultReceiver resultReceiver) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(DOWNLOAD_FAILED, true);
        resultReceiver.send(STATUS_FAILED, bundle);
    }

    private void unzip(String str, String str2) throws Exception {
        try {
            ZipFile zipFile = new ZipFile(new File(str));
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                unzipEntry(zipFile, entries.nextElement(), str2);
            }
        } catch (Exception e) {
            Log.e("Unzip zip", "Unzip exception", e);
        }
    }

    private void ensureZipPathSafety(File file, String str) throws Exception {
        String canonicalPath = new File(str).getCanonicalPath();
        if (!file.getCanonicalPath().startsWith(canonicalPath)) {
            throw new Exception(String.format("Found Zip Path Traversal Vulnerability with %s", canonicalPath));
        }
    }

    private void unzipEntry(ZipFile zipFile, ZipEntry zipEntry, String str) throws IOException {
        if (zipEntry.isDirectory()) {
            createDir(new File(str, zipEntry.getName()));
            return;
        }
        File file = new File(str, zipEntry.getName());
        if (!file.getParentFile().exists()) {
            createDir(file.getParentFile());
        }
        try {
            ensureZipPathSafety(file, str);
            Log.v("ZIP E", "Extracting: " + zipEntry);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(zipFile.getInputStream(zipEntry));
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            while (true) {
                try {
                    int read = bufferedInputStream.read();
                    if (read != -1) {
                        bufferedOutputStream.write(read);
                    } else {
                        bufferedOutputStream.close();
                        return;
                    }
                } finally {
                    bufferedOutputStream.close();
                    bufferedInputStream.close();
                }
            }
        } catch (Exception unused) {
        }
    }

    private void createDir(File file) {
        if (file.exists()) {
            return;
        }
        Log.v("ZIP E", "Creating dir " + file.getName());
        if (file.mkdirs()) {
            return;
        }
        throw new RuntimeException("Can not create dir " + file);
    }

    
    public static class FileDownloader extends ResultReceiver {
        private DownloadRequest downloadDetails;
        private OnDownloadStatusListener onDownloadStatusListener;

        public static FileDownloader getInstance(DownloadRequest downloadRequest, OnDownloadStatusListener onDownloadStatusListener) {
            FileDownloader fileDownloader = new FileDownloader(new Handler(Looper.getMainLooper()));
            fileDownloader.downloadDetails = downloadRequest;
            fileDownloader.onDownloadStatusListener = onDownloadStatusListener;
            return fileDownloader;
        }

        public void download(Context context) {
            if (FileDownloadService.isOnline(context)) {
                Intent intent = new Intent(context, FileDownloadService.class);
                intent.putExtra(FileDownloadService.DOWNLOADER_RECEIVER, this);
                intent.putExtra(FileDownloadService.DOWNLOAD_DETAILS, this.downloadDetails);
                context.startService(intent);
            }
        }

        private FileDownloader(Handler handler) {
            super(handler);
        }

        @Override 
        protected void onReceiveResult(int i, Bundle bundle) {
            super.onReceiveResult(i, bundle);
            if (this.onDownloadStatusListener == null) {
                return;
            }
            if (i != FileDownloadService.STATUS_OK) {
                if (i == FileDownloadService.STATUS_FAILED) {
                    this.onDownloadStatusListener.onDownloadFailed();
                }
            } else if (bundle.containsKey(FileDownloadService.DOWNLOAD_STARTED) && bundle.getBoolean(FileDownloadService.DOWNLOAD_STARTED)) {
                this.onDownloadStatusListener.onDownloadStarted();
            } else if (bundle.containsKey(FileDownloadService.DOWNLOAD_COMPLETED) && bundle.getBoolean(FileDownloadService.DOWNLOAD_COMPLETED)) {
                this.onDownloadStatusListener.onDownloadCompleted();
            } else if (bundle.containsKey(FileDownloadService.DOWNLOAD_PROGRESS)) {
                this.onDownloadStatusListener.onDownloadProgress(bundle.getInt(FileDownloadService.DOWNLOAD_PROGRESS));
            }
        }

        public DownloadRequest getDownloadDetails() {
            return this.downloadDetails;
        }

        public void setDownloadDetails(DownloadRequest downloadRequest) {
            this.downloadDetails = downloadRequest;
        }

        public OnDownloadStatusListener getOnDownloadStatusListener() {
            return this.onDownloadStatusListener;
        }

        public void setOnDownloadStatusListener(OnDownloadStatusListener onDownloadStatusListener) {
            this.onDownloadStatusListener = onDownloadStatusListener;
        }
    }

    
    public static class DownloadRequest implements Parcelable {
        public static final Creator<DownloadRequest> CREATOR = new Creator<DownloadRequest>() { 
            
            @Override 
            public DownloadRequest createFromParcel(Parcel parcel) {
                return new DownloadRequest(parcel);
            }

            
            @Override 
            public DownloadRequest[] newArray(int i) {
                return new DownloadRequest[i];
            }
        };
        private boolean deleteZipAfterExtract;
        private String localFilePath;
        private boolean requiresUnzip;
        private String serverFilePath;
        private String tag;
        private String unzipAtFilePath;

        @Override 
        public int describeContents() {
            return 0;
        }

        public DownloadRequest(String str, String str2) {
            this.deleteZipAfterExtract = true;
            this.serverFilePath = str;
            this.localFilePath = str2;
            this.requiresUnzip = this.requiresUnzip;
        }

        protected DownloadRequest(Parcel parcel) {
            this.deleteZipAfterExtract = true;
            this.requiresUnzip = parcel.readByte() != 0;
            this.serverFilePath = parcel.readString();
            this.localFilePath = parcel.readString();
            this.unzipAtFilePath = parcel.readString();
            this.deleteZipAfterExtract = parcel.readByte() != 0;
        }

        @Override 
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte(this.requiresUnzip ? (byte) 1 : (byte) 0);
            parcel.writeString(this.serverFilePath);
            parcel.writeString(this.localFilePath);
            parcel.writeString(this.unzipAtFilePath);
            parcel.writeByte(this.deleteZipAfterExtract ? (byte) 1 : (byte) 0);
        }

        public boolean isRequiresUnzip() {
            return this.requiresUnzip;
        }

        public void setRequiresUnzip(boolean z) {
            this.requiresUnzip = z;
        }

        public String getServerFilePath() {
            return this.serverFilePath;
        }

        public void setServerFilePath(String str) {
            this.serverFilePath = str;
        }

        public String getLocalFilePath() {
            return this.localFilePath;
        }

        public void setLocalFilePath(String str) {
            this.localFilePath = str;
        }

        public static Creator<DownloadRequest> getCreator() {
            return CREATOR;
        }

        public String getUnzipAtFilePath() {
            return this.unzipAtFilePath;
        }

        public void setUnzipAtFilePath(String str) {
            this.unzipAtFilePath = str;
        }

        public String getTag() {
            return this.tag;
        }

        public void setTag(String str) {
            this.tag = str;
        }

        public boolean isDeleteZipAfterExtract() {
            return this.deleteZipAfterExtract;
        }

        public void setDeleteZipAfterExtract(boolean z) {
            this.deleteZipAfterExtract = z;
        }
    }

    
    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting() && connectivityManager.getActiveNetworkInfo().isAvailable() && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
