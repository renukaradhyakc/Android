package com.thelinkphone.app.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FileUltils {
    public static final String AUTHORITY = "YOUR_AUTHORITY.provider";
    private static final boolean DEBUG = false;
    public static final String DOCUMENTS_DIR = "documents";
    public static final String HIDDEN_PREFIX = ".";
    static final String TAG = "FileUtils";

    private static void logDir(File file) {
    }

    private FileUltils() {
    }

    public static String getExtension(String str) {
        if (str == null) {
            return null;
        }
        int lastIndexOf = str.lastIndexOf(HIDDEN_PREFIX);
        return lastIndexOf >= 0 ? str.substring(lastIndexOf) : "";
    }

    public static boolean isLocal(String str) {
        return (str == null || str.startsWith("http://") || str.startsWith("https://")) ? false : true;
    }

    public static boolean isMediaUri(Uri uri) {
        return "media".equalsIgnoreCase(uri.getAuthority());
    }

    public static Uri getUri(File file) {
        if (file != null) {
            return Uri.fromFile(file);
        }
        return null;
    }

    public static String getMimeType(File file) {
        String extension = getExtension(file.getName());
        return extension.length() > 0 ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1)) : "application/octet-stream";
    }

    public static String getMimeType(Context context, Uri uri) throws IOException {
        return getMimeType(new File(getPath(context, uri)));
    }

    public static String getMimeType(Context context, String str) {
        String type = context.getContentResolver().getType(Uri.parse(str));
        return type == null ? "application/octet-stream" : type;
    }

    public static boolean isLocalStorageDocument(Uri uri) {
        return AUTHORITY.equals(uri.getAuthority());
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static boolean isGoogleDriveUri(Uri uri) {
        return "com.google.android.apps.docs.storage.legacy".equals(uri.getAuthority()) || "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    
    
    
    
    
    
    public static String getDataColumn(Context context, Uri uri, String str, String[] strArr) {
        Cursor cursor;
        Cursor cursor2 = null;
        try {
            cursor = context.getContentResolver().query(uri, new String[]{"_data"}, str, strArr, null);
            if (cursor != null) {
                try {
                    try {
                        if (cursor.moveToFirst()) {
                            String string = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                            if (cursor != null) {
                                cursor.close();
                            }
                            return string;
                        }
                    } catch (Exception e) {
                        e = e;
                        e.printStackTrace();
                    }
                } catch (Throwable th) {
                    th = th;
                    cursor2 = cursor;
                    if (cursor2 != null) {
                        cursor2.close();
                    }
                    throw th;
                }
            }
        } catch (Exception e2) {
           
            cursor = null;
        } catch (Throwable th2) {
           
            if (cursor2 != null) {
            }
            
        }
        return null;
    }

    public static String getPath(Context context, Uri uri) throws IOException {
        String localPath = getLocalPath(context, uri);
        return localPath != null ? localPath : uri.toString();
    }

    private static String getLocalPath(Context context, Uri uri) throws IOException {
        String dataColumn = null;
        Uri uri2 = null;
        if ((Build.VERSION.SDK_INT >= 19) && DocumentsContract.isDocumentUri(context, uri)) {
            if (isLocalStorageDocument(uri)) {
                return DocumentsContract.getDocumentId(uri);
            }
            if (isExternalStorageDocument(uri)) {
                String[] split = DocumentsContract.getDocumentId(uri).split(":");
                String str = split[0];
                if ("primary".equalsIgnoreCase(str)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else if ("home".equalsIgnoreCase(str)) {
                    return Environment.getExternalStorageDirectory() + "/documents/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                String documentId = DocumentsContract.getDocumentId(uri);
                if (documentId != null && documentId.startsWith("raw:")) {
                    return documentId.substring(4);
                }
                String[] strArr = {"content://downloads/public_downloads", "content://downloads/my_downloads"};
                for (int i = 0; i < 2; i++) {
                    try {
                        dataColumn = getDataColumn(context, ContentUris.withAppendedId(Uri.parse(strArr[i]), Long.valueOf(documentId).longValue()), null, null);
                    } catch (Exception unused) {
                    }
                    if (dataColumn != null) {
                        return dataColumn;
                    }
                }
                File generateFileName = generateFileName(getFileName(context, uri), getDocumentCacheDir(context));
                if (generateFileName != null) {
                    String absolutePath = generateFileName.getAbsolutePath();
                    saveFileFromUri(context, uri, absolutePath);
                    return absolutePath;
                }
                return null;
            } else if (isMediaDocument(uri)) {
                String[] split2 = DocumentsContract.getDocumentId(uri).split(":");
                String str2 = split2[0];
                if ("image".equals(str2)) {
                    uri2 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(str2)) {
                    uri2 = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(str2)) {
                    uri2 = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                return getDataColumn(context, uri2, "_id=?", new String[]{split2[1]});
            } else if (isGoogleDriveUri(uri)) {
                return getGoogleDriveFilePath(uri, context);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }
            if (isGoogleDriveUri(uri)) {
                return getGoogleDriveFilePath(uri, context);
            }
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static File getFile(Context context, Uri uri) throws IOException {
        String path;
        if (uri == null || (path = getPath(context, uri)) == null || !isLocal(path)) {
            return null;
        }
        return new File(path);
    }

    public static File getDownloadsDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static File getDocumentCacheDir(Context context) {
        File file = new File(context.getCacheDir(), DOCUMENTS_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }
        logDir(context.getCacheDir());
        logDir(file);
        return file;
    }

    public static File generateFileName(String str, File file) {
        String str2;
        if (str == null) {
            return null;
        }
        File file2 = new File(file, str);
        if (file2.exists()) {
            int lastIndexOf = str.lastIndexOf(46);
            int i = 0;
            if (lastIndexOf > 0) {
                String substring = str.substring(0, lastIndexOf);
                str2 = str.substring(lastIndexOf);
                str = substring;
            } else {
                str2 = "";
            }
            while (file2.exists()) {
                i++;
                file2 = new File(file, str + '(' + i + ')' + str2);
            }
        }
        try {
            if (file2.createNewFile()) {
                logDir(file);
                return file2;
            }
            return null;
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    
    
    
    public static void saveFileFromUri(Context context, Uri uri, String str) throws IOException {
        BufferedOutputStream bufferedOutputStream;
        InputStream openInputStream = null;
        InputStream inputStream = null;
        try {
            try {
                openInputStream = context.getContentResolver().openInputStream(uri);
                try {
                    bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(str, false));
                } catch (IOException e) {
                    e = e;
                    bufferedOutputStream = null;
                } catch (Throwable th) {
                    th = th;
                    bufferedOutputStream = null;
                }
            } catch (IOException e2) {
                e2.printStackTrace();
                return;
            }
        } catch (Exception e3) {

            bufferedOutputStream = null;
        } catch (Throwable th2) {

            bufferedOutputStream = null;
        }
        try {
            byte[] bArr = new byte[1024];
            openInputStream.read(bArr);
            do {
                bufferedOutputStream.write(bArr);
            } while (openInputStream.read(bArr) != -1);
            if (openInputStream != null) {
                openInputStream.close();
            }
            bufferedOutputStream.close();
        } catch (IOException e4) {

            inputStream = openInputStream;
            try {

                if (inputStream != null) {
                    inputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
            } catch (Throwable th3) {

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                        e5.printStackTrace();

                    }
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }

            }
        } catch (Throwable th4) {

            inputStream = openInputStream;
            if (inputStream != null) {
            }
            if (bufferedOutputStream != null) {
            }

        }
    }


    public static String getFileName(Context context, Uri uri) throws IOException {
        if (context.getContentResolver().getType(uri) == null && context != null) {
            String path = getPath(context, uri);
            if (path == null) {
                return getName(uri.toString());
            }
            return new File(path).getName();
        }
        Cursor query = context.getContentResolver().query(uri, null, null, null, null);
        if (query != null) {
            int columnIndex = query.getColumnIndex("_display_name");
            query.moveToFirst();
            String string = query.getString(columnIndex);
            query.close();
            return string;
        }
        return null;
    }

    public static String getName(String str) {
        if (str == null) {
            return null;
        }
        return str.substring(str.lastIndexOf(47) + 1);
    }

    private static String getGoogleDriveFilePath(Uri uri, Context context) {
        Cursor query = context.getContentResolver().query(uri, null, null, null, null);
        int columnIndex = query.getColumnIndex("_display_name");
        int columnIndex2 = query.getColumnIndex("_size");
        query.moveToFirst();
        String string = query.getString(columnIndex);
        Long.toString(query.getLong(columnIndex2));
        File file = new File(context.getCacheDir(), string);
        try {
            InputStream openInputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            byte[] bArr = new byte[Math.min(openInputStream.available(), 1048576)];
            while (true) {
                int read = openInputStream.read(bArr);
                if (read == -1) {
                    break;
                }
                fileOutputStream.write(bArr, 0, read);
            }
            openInputStream.close();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }
}
