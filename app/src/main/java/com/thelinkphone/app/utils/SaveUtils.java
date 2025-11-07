package com.thelinkphone.app.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class SaveUtils {




    public static Uri saveSound(Context context, byte[] bArr, String str) throws IOException {
        Uri uri;
        int r2 = 0;
        if (Build.VERSION.SDK_INT >= 29) {
            String str2 = Environment.DIRECTORY_MUSIC;
            ContentValues contentValues = new ContentValues();
            contentValues.put("_display_name", str + ".m4a");
            contentValues.put("mime_type", "audio/m4a");
            contentValues.put("relative_path", str2);
            ContentResolver contentResolver = context.getContentResolver();
            try {
                try {
                    try {
                        uri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
                        try {
                            if (uri == null) {
                                throw new IOException("Failed to create new MediaStore record.");
                            }
                            OutputStream openOutputStream = contentResolver.openOutputStream(uri);
                            try {
                                if (openOutputStream == null) {
                                    throw new IOException("Failed to get output stream.");
                                }
                                openOutputStream.write(bArr);
                                if (openOutputStream != null) {
                                    openOutputStream.close();
                                }
                                return uri;
                            } catch (IOException e) {
                                e = e;
                                if (uri != null) {
                                    contentResolver.delete(uri, null, null);
                                }
                                throw e;
                            }
                        } catch (IOException e2) {

                        }
                    } catch (Throwable th) {


                        throw th;
                    }
                } catch (Exception e3) {

                }
            } catch (Throwable th2) {

            }
        } else {
            String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
            File file2 = new File(file);
            if (!file2.exists()) {
                file2.mkdirs();
            }
            File file3 = new File(file, str + ".m4a");
            file3.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(file3);
            fileOutputStream.write(bArr);
            fileOutputStream.close();
            MediaScannerConnection.scanFile(context, new String[]{file3.getPath()}, null, null);
            return Uri.fromFile(file3);
        }
        return null;
    }
}
