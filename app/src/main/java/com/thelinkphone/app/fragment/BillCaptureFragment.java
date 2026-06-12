package com.thelinkphone.app.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.thelinkphone.app.BillProcessingActivity;
import com.thelinkphone.app.R;
import com.thelinkphone.app.model.BillUploadResponse;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.NetworkUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.media.MediaActionSound;
import android.os.VibrationEffect;
import android.os.Vibrator;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillCaptureFragment extends Fragment {

    private PreviewView previewView;
    private ImageButton btnCapture;
    private ImageView imgLastPhoto;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private View flashOverlay;
    private MediaActionSound shutterSound;
    private static final int CAMERA_PERMISSION_CODE = 10;
    private Uri lastCapturedUri = null;
    private boolean currentCropFromGallery = false;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;
    private ModeSwitchListener modeSwitchListener;
    public BillCaptureFragment() {}

    public interface ModeSwitchListener {
        void openQrScanner();
    }

    public void setModeSwitchListener(ModeSwitchListener listener) {
        this.modeSwitchListener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bill_capture, container, false);

        previewView = view.findViewById(R.id.previewView);
        btnCapture = view.findViewById(R.id.btnCapture);

        ImageButton btnSwitchMode = view.findViewById(R.id.btnSwitchMode);

        btnSwitchMode.setOnClickListener(v -> {
            if (modeSwitchListener != null) {
                modeSwitchListener.openQrScanner();
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        currentCropFromGallery = true;
                        Log.d("Gallery", "Image selected: " + uri);
                        handleGalleryImage(uri);
                    }
                }
        );

        cropLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {

            if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {

                Uri croppedUri = UCrop.getOutput(result.getData());
                if (croppedUri != null) {
                    Log.d("Crop", "Crop success: " + croppedUri);
                    if (!isAdded()) return;
                    openPreview(croppedUri, currentCropFromGallery);
                } else {
                    Log.e("Crop", "Crop returned null URI");
                }
            }
            else if (result.getResultCode() == UCrop.RESULT_ERROR) {
                Throwable error = UCrop.getError(result.getData());
                Log.e("Crop", "Crop failed", error);
                Toast.makeText(getContext(), "Crop failed", Toast.LENGTH_SHORT).show();
            }
        });

        flashOverlay = view.findViewById(R.id.flashOverlay);

        shutterSound = new MediaActionSound();
        shutterSound.load(MediaActionSound.SHUTTER_CLICK);

        imgLastPhoto = view.findViewById(R.id.imgLastPhoto);

        btnCapture.setOnClickListener(v -> takePhoto());

        if (hasCameraPermission()) {
            startCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        loadLastPhoto();

        imgLastPhoto.setOnClickListener(v -> {
            openGallery();
        });

        return view;
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Log.e("BillCapture", "Camera init failed", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) {
            Toast.makeText(getContext(), "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(System.currentTimeMillis());

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "BILL_" + name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(
                        requireContext().getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            animateCaptureEffect();
        }

        imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                        if (!isAdded()) return;
                        Uri savedUri = output.getSavedUri();

                        requireActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;
                            showThumbnail(savedUri);
                            Log.d("BillCapture", "Saved URI: " + savedUri);
                            currentCropFromGallery = false;
                            handleGalleryImage(savedUri);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("BillCapture", "Capture failed", exception);
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;
                            Toast.makeText(getContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
        );
    }

    private void uploadBill(Uri uri) {
        if (uri == null) return;

        // Get token from SharedPreferences
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);
        Log.d("BillCapture", "Using token = " + token);
        Log.d("BillCapture", "Token is null: " + (token == null));

        if (token == null) {
            Toast.makeText(getContext(), "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert URI to File
        File file = uriToFile(uri);
        if (file == null) {
            Toast.makeText(getContext(), "Failed to read image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build multipart body
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("image/jpeg"), file
        );
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "bill", file.getName(), requestBody
        );

        // Show uploading feedback
        requireActivity().runOnUiThread(() ->
                Toast.makeText(getContext(), "Uploading bill...", Toast.LENGTH_SHORT).show()
        );
        if (!NetworkUtils.isInternetAvailable(this)) {

            Log.d("BillCapture", "Internet available = "
                    + NetworkUtils.isInternetAvailable(this));
            new AlertDialog.Builder(this)
                    .setTitle("No Internet")
                    .setMessage("Please connect to the internet and try again.")
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.uploadBill("Bearer " + token, part)
                .enqueue(new Callback<BillUploadResponse>() {

                    @Override
                    public void onResponse(@NonNull Call<BillUploadResponse> call,
                                           @NonNull Response<BillUploadResponse> response) {
                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            if (!isAdded()) return;
                            Log.d("BillCapture", "Response code = " + response.code());
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().isSuccess()) {

                                long billId = response.body().getData().getBillId();
                                Log.d("BillCapture", "Bill uploaded, ID: " + billId);

                                Log.d("BillFlow", "Upload success");
                                Log.d("BillFlow", "Launching BillProcessingActivity");
                                Intent intent = new Intent(getActivity(), BillProcessingActivity.class);
                                intent.putExtra("bill_id", billId);
                                intent.putExtra("token", token);

                                startActivity(intent);
                            } else if (response.code() == 409) {
                                Toast.makeText(getContext(),
                                        "Duplicate bill already uploaded",
                                        Toast.LENGTH_LONG).show();

                            } else if (response.code() == 401) {
                                Toast.makeText(getContext(),
                                        "Session expired, please login again",
                                        Toast.LENGTH_LONG).show();

                            } else {
                                Log.e("BillCapture",
                                        "Upload failed. HTTP " + response.code());
                                Toast.makeText(getContext(),
                                        "Upload failed, try again",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<BillUploadResponse> call,
                                          @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Log.e("BillCapture", "Upload failed", t);
                        requireActivity().runOnUiThread(() ->{
                            if (!isAdded()) return;
                            String message;
                            if (t instanceof java.net.ConnectException) {
                                message = "Server unreachable. Please try again.";
                            } else if (t instanceof java.net.SocketTimeoutException) {
                                message = "Server unreachable. Please try again.";
                            } else if (t instanceof java.net.UnknownHostException) {
                                message = "No internet connection.";
                            } else {
                                message = "Upload failed.";
                            }
                            Log.e("BillCapture", "Showing upload failure toast");
                            Toast.makeText(getContext(),
                                    message,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);

            if (inputStream == null) return null;

            android.graphics.Bitmap bitmap = android.graphics.BitmapFactory
                    .decodeStream(inputStream);

            inputStream.close();

            if (bitmap == null) {
                Log.e("BillCapture", "Bitmap decode failed");
                return null;
            }

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            float scale = Math.min(1.0f, 1024f / Math.max(width, height));

            android.graphics.Bitmap scaledBitmap = bitmap;

            if (scale < 1.0f) {
                scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, (int) (width * scale), (int) (height * scale), true);
                bitmap.recycle();
            }

            File tempFile = File.createTempFile(
                    "BILL_", ".jpg",
                    requireContext().getCacheDir()
            );

            FileOutputStream outputStream = new FileOutputStream(tempFile);

            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream);

            outputStream.flush();
            outputStream.close();

            Log.d("BillCapture", "Final file size: " + tempFile.length() + " bytes");

            return tempFile;

        } catch (Exception e) {
            Log.e("BillCapture", "uriToFile failed", e);
            return null;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void animateCaptureEffect() {

        // Screen flash
        flashOverlay.setAlpha(0f);

        flashOverlay.animate()
                .alpha(0.8f)
                .setDuration(80)
                .withEndAction(() ->
                        flashOverlay.animate()
                                .alpha(0f)
                                .setDuration(120)
                                .start()
                ).start();

        btnCapture.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(80)
                .withEndAction(() ->
                        btnCapture.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(80)
                                .start()
                ).start();

        // Shutter sound
        shutterSound.play(MediaActionSound.SHUTTER_CLICK);

        // Vibration
        Vibrator vibrator =
                (Vibrator) requireContext().getSystemService(requireContext().VIBRATOR_SERVICE);

        if (vibrator != null) {
            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            40,
                            VibrationEffect.DEFAULT_AMPLITUDE
                    )
            );
        }
    }

    private void loadLastPhoto() {
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DISPLAY_NAME + " LIKE ?";
        String[] selectionArgs = {"BILL_%"};
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = requireContext().getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, sortOrder)) {

            Log.d("Thumbnail", "Cursor count: " + (cursor != null ? cursor.getCount() : 0));

            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                showThumbnail(uri);
            }
        } catch (Exception e) {
            Log.e("BillCapture", "Failed to load last photo", e);
        }
    }

    private void showThumbnail(Uri uri) {
        Log.d("Thumbnail", "showThumbnail called with: " + uri);
        if (uri == null) return;
        lastCapturedUri = uri;
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            Log.d("Thumbnail", "Setting image URI");
            if (!isAdded()) return;
            imgLastPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imgLastPhoto.setImageURI(uri);
            imgLastPhoto.setPadding(0, 0, 0, 0);
        });
    }

    private void openPreview(Uri uri, boolean fromGallery) {
        if (uri == null || !isAdded()) return;

        // Full screen preview dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        View previewView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_image_preview, null);

        ImageView fullImage = previewView.findViewById(R.id.fullImageView);
        fullImage.setImageURI(uri);

        builder.setView(previewView);
        String secondaryButtonText = fromGallery ? "Reselect" : "Retake";
        builder.setPositiveButton("Upload", (dialog, which) -> {
            uploadBill(uri);
            dialog.dismiss();
        });
        builder.setNegativeButton(secondaryButtonText, (dialog, which) -> {
            if (fromGallery) {
                Log.d("Gallery", "User clicked Reselect, reopening gallery");
                openGallery();
            }
            dialog.dismiss();
        });

        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(getContext(),
                        "Camera permission required",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openGallery() {
        Log.d("Gallery", "Opening gallery picker...");
        galleryLauncher.launch("image/*");
    }

    private void handleGalleryImage(Uri sourceUri) {

        if (!isAdded() || sourceUri == null) return;

        try {
            File destinationFile = new File(requireContext().getCacheDir(), "cropped_" + System.currentTimeMillis() + ".jpg");
            Uri destinationUri = Uri.fromFile(destinationFile);

            UCrop.Options options = new UCrop.Options();
            options.setToolbarTitle("Adjust Receipt");
            options.setHideBottomControls(false);
            options.setFreeStyleCropEnabled(true);
            options.setCircleDimmedLayer(false);
            options.setShowCropGrid(true);
            options.setShowCropFrame(true);
            options.setCompressionQuality(90);

            options.setToolbarColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            options.setStatusBarColor(ContextCompat.getColor(requireContext(), android.R.color.black));
            options.setRootViewBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black));
            options.setToolbarWidgetColor(ContextCompat.getColor(requireContext(), R.color.white));
            options.setActiveControlsWidgetColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            options.setCropGridColor(ContextCompat.getColor(requireContext(), R.color.white));
            options.setCropFrameColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
            options.setDimmedLayerColor(ContextCompat.getColor(requireContext(), R.color.crop_dim));

            Intent intent = UCrop.of(sourceUri, destinationUri)
                    .withOptions(options)
                    .withMaxResultSize(2000, 3000)
                    .getIntent(requireContext());

            cropLauncher.launch(intent);

        } catch (Exception e) {
            Log.e("Crop", "Failed to launch crop", e);
            Toast.makeText(getContext(), "Unable to open editor", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
