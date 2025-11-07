package com.thelinkphone.app.service;

import android.media.AudioRecord;
import android.os.Build;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


public final class StreamAudioRecorder {
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    public static final int DEFAULT_SAMPLE_RATE = 44100;
    private static final String TAG = "StreamAudioRecorder";
    private ExecutorService mExecutorService;
    private final AtomicBoolean mIsRecording;


    public interface AudioDataCallback {
        void onAudioData(byte[] bArr, int i);

        void onError();
    }

    private StreamAudioRecorder() {
        this.mIsRecording = new AtomicBoolean(false);
    }

    public static StreamAudioRecorder getInstance() {
        return StreamAudioRecorderHolder.INSTANCE;
    }

    public synchronized boolean start(AudioDataCallback audioDataCallback) {
        return start(DEFAULT_SAMPLE_RATE, 16, 2, 2048, audioDataCallback);
    }

    public synchronized boolean start(int i, int i2, int i3, int i4, AudioDataCallback audioDataCallback) {
        stop();
        this.mExecutorService = Executors.newSingleThreadExecutor();
        if (this.mIsRecording.compareAndSet(false, true)) {
            this.mExecutorService.execute(new AudioRecordRunnable(i, i2, i3, i4, audioDataCallback));
            return true;
        }
        return false;
    }

    public synchronized void stop() {
        this.mIsRecording.compareAndSet(true, false);
        ExecutorService executorService = this.mExecutorService;
        if (executorService != null) {
            executorService.shutdown();
            this.mExecutorService = null;
        }
    }


    private static final class StreamAudioRecorderHolder {
        private static final StreamAudioRecorder INSTANCE = new StreamAudioRecorder();

        private StreamAudioRecorderHolder() {
        }
    }



    public class AudioRecordRunnable implements Runnable {
        private final AudioDataCallback mAudioDataCallback;
        private final int mAudioFormat;
        private final AudioRecord mAudioRecord;
        private final byte[] mByteBuffer;
        private final int mByteBufferSize;
        private final short[] mShortBuffer;
        private final int mShortBufferSize;

        AudioRecordRunnable(int i, int i2, int i3, int i4, AudioDataCallback audioDataCallback) {
            this.mAudioFormat = i3;
            int minBufferSize = AudioRecord.getMinBufferSize(i, i2, i3);
            this.mByteBufferSize = i4;
            int i5 = i4 / 2;
            this.mShortBufferSize = i5;
            this.mByteBuffer = new byte[i4];
            this.mShortBuffer = new short[i5];
            if (Build.VERSION.SDK_INT < 29) {
                this.mAudioRecord = new AudioRecord(1, i, i2, i3, Math.max(minBufferSize, i4));
            } else {
                this.mAudioRecord = new AudioRecord(7, i, i2, i3, Math.max(minBufferSize, i4));
            }
            this.mAudioDataCallback = audioDataCallback;
        }

        @Override 
        public void run() {
            if (this.mAudioRecord.getState() == 1) {
                try {
                    this.mAudioRecord.startRecording();
                    while (true) {
                        if (!StreamAudioRecorder.this.mIsRecording.get()) {
                            break;
                        } else if (this.mAudioFormat == 2) {
                            int read = this.mAudioRecord.read(this.mShortBuffer, 0, this.mShortBufferSize);
                            if (read > 0) {
                                this.mAudioDataCallback.onAudioData(short2byte(this.mShortBuffer, read, this.mByteBuffer), read * 2);
                            } else {
                                onError(read);
                                break;
                            }
                        } else {
                            int read2 = this.mAudioRecord.read(this.mByteBuffer, 0, this.mByteBufferSize);
                            if (read2 > 0) {
                                this.mAudioDataCallback.onAudioData(this.mByteBuffer, read2);
                            } else {
                                onError(read2);
                                break;
                            }
                        }
                    }
                } catch (IllegalStateException e) {
                    Log.w(StreamAudioRecorder.TAG, "startRecording fail: " + e.getMessage());
                    this.mAudioDataCallback.onError();
                    return;
                }
            }
            this.mAudioRecord.release();
        }

        private byte[] short2byte(short[] sArr, int i, byte[] bArr) {
            if (i > sArr.length || i * 2 > bArr.length) {
                Log.w(StreamAudioRecorder.TAG, "short2byte: too long short data array");
            }
            for (int i2 = 0; i2 < i; i2++) {
                int i3 = i2 * 2;
                bArr[i3] = (byte) (sArr[i2] & 255);
                bArr[i3 + 1] = (byte) (sArr[i2] >> 8);
            }
            return bArr;
        }

        private void onError(int i) {
            if (i == -3) {
                Log.w(StreamAudioRecorder.TAG, "record fail: ERROR_INVALID_OPERATION");
                this.mAudioDataCallback.onError();
            } else if (i == -2) {
                Log.w(StreamAudioRecorder.TAG, "record fail: ERROR_BAD_VALUE");
                this.mAudioDataCallback.onError();
            }
        }
    }
}
