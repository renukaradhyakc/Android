package com.thelinkphone.app.service;

import android.content.Context;
import android.widget.Toast;
import com.thelinkphone.app.utils.OtherUtils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class MyRecorder {
    private final Context c;
    private boolean isRecording;
    private FileOutputStream mFileOutputStream;
    private File mOutputFile;
    private final StreamAudioRecorder mStreamAudioRecorder = StreamAudioRecorder.getInstance();
    private final String num;
    private final int status;

    public MyRecorder(Context context, String str, int i) {
        this.c = context;
        this.num = str;
        this.status = i;
    }

    public void startRecord() {
        if (this.isRecording) {
            return;
        }
        this.isRecording = true;
        try {
            File file = new File(OtherUtils.getStore(this.c) + "/data");
            if (!file.exists()) {
                file.mkdirs();
            }
            File file2 = new File(file, "data.pcm");
            this.mOutputFile = file2;
            file2.createNewFile();
            this.mFileOutputStream = new FileOutputStream(this.mOutputFile);
            this.mStreamAudioRecorder.start(new StreamAudioRecorder.AudioDataCallback() { 
                @Override 
                public void onAudioData(byte[] bArr, int i) {
                    if (MyRecorder.this.mFileOutputStream != null) {
                        try {
                            MyRecorder.this.mFileOutputStream.write(bArr, 0, i);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override 
                public void onError() {
                    try {
                        Toast.makeText(MyRecorder.this.c, "Record fail", Toast.LENGTH_SHORT).show();
                    } catch (RuntimeException unused) {
                    }
                }
            });
        } catch (IOException unused) {
            Toast.makeText(this.c, "Record fail.", Toast.LENGTH_SHORT).show();
        }
    }

    public void stopRecord() {
        if (this.isRecording) {
            this.isRecording = false;
            this.mStreamAudioRecorder.stop();
            FileOutputStream fileOutputStream = this.mFileOutputStream;
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                    this.mFileOutputStream = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            new Thread(new Runnable() { 
                @Override 
                public final void run() {
                    MyRecorder.this.m238x74407295();
                }
            }).start();
        }
    }

    
    
    public  void m238x74407295() {
        File file = new File(OtherUtils.getPathSave(this.c));
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file, this.num + "_" + this.status + "_" + System.currentTimeMillis() + ".m4a");
        try {
            file2.createNewFile();
            rawToWave(this.mOutputFile, file2);
            this.mOutputFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rawToWave(File file, File file2) throws IOException {
        int length = (int) file.length();
        byte[] bArr = new byte[length];
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
        try {
            dataInputStream.read(bArr);
            dataInputStream.close();
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file2));
            try {
                writeString(dataOutputStream, "RIFF");
                writeInt(dataOutputStream, length + 36);
                writeString(dataOutputStream, "WAVE");
                writeString(dataOutputStream, "fmt ");
                writeInt(dataOutputStream, 16);
                writeShort(dataOutputStream, (short) 1);
                writeShort(dataOutputStream, (short) 1);
                writeInt(dataOutputStream, StreamAudioRecorder.DEFAULT_SAMPLE_RATE);
                writeInt(dataOutputStream, 2048);
                writeShort(dataOutputStream, (short) 2);
                writeShort(dataOutputStream, (short) 16);
                writeString(dataOutputStream, "data");
                writeInt(dataOutputStream, length);
                int i = length / 2;
                short[] sArr = new short[i];
                ByteBuffer.wrap(bArr).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sArr);
                ByteBuffer allocate = ByteBuffer.allocate(i * 2);
                for (int i2 = 0; i2 < i; i2++) {
                    allocate.putShort(sArr[i2]);
                }
                dataOutputStream.write(fullyReadFileToBytes(file));
                dataOutputStream.close();
            } catch (Throwable th) {
                try {
                    dataOutputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        } catch (Throwable th3) {
            try {
                dataInputStream.close();
            } catch (Throwable th4) {
                th3.addSuppressed(th4);
            }
            throw th3;
        }
    }

    private byte[] fullyReadFileToBytes(File file) throws IOException {
        int length = (int) file.length();
        byte[] bArr = new byte[length];
        byte[] bArr2 = new byte[length];
        FileInputStream fileInputStream = new FileInputStream(file);
        try {
            int read = fileInputStream.read(bArr, 0, length);
            if (read < length) {
                int i = length - read;
                while (i > 0) {
                    int read2 = fileInputStream.read(bArr2, 0, i);
                    System.arraycopy(bArr2, 0, bArr, length - i, read2);
                    i -= read2;
                }
            }
            fileInputStream.close();
            return bArr;
        } catch (Throwable th) {
            try {
                fileInputStream.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    private void writeInt(DataOutputStream dataOutputStream, int i) throws IOException {
        dataOutputStream.write(i);
        dataOutputStream.write(i >> 8);
        dataOutputStream.write(i >> 16);
        dataOutputStream.write(i >> 24);
    }

    private void writeShort(DataOutputStream dataOutputStream, short s) throws IOException {
        dataOutputStream.write(s);
        dataOutputStream.write(s >> 8);
    }

    private void writeString(DataOutputStream dataOutputStream, String str) throws IOException {
        for (int i = 0; i < str.length(); i++) {
            dataOutputStream.write(str.charAt(i));
        }
    }
}
