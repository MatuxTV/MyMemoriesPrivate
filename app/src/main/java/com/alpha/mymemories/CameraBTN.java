/*package com.alpha.mymemories;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton Camera;
    private BottomAppBar bottomAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Camera = findViewById(R.id.btnCamera);
        bottomAppBar = findViewById(R.id.bottomAppBar);

        Camera.setOnClickListener(new View.OnClickListener() {
           //com.alpha.mymemories.MainCamera mainCamera = new MainCamera();
            @Override
            public void onClick(View view) {
               // Intent CameraIntent = new Intent(MainActivity.this,com.alpha.mymemories.MainCamera.class);
                //startActivity(CameraIntent);
              new com.alpha.mymemories.MainCamera().start;
            }
        });

    }
}
*/
package com.alpha.mymemories;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class CameraBTN extends AppCompatActivity implements View.OnClickListener {


    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private static final int MY_STORAGE_REQUEST_CODE = 100;
    private static final int MY_MICROPHONE_REQUEST_CODE = 100;
    Button bTakePicture, bRecording;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);

        Button button = findViewById(R.id.btnClick);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_STORAGE_REQUEST_CODE);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MY_MICROPHONE_REQUEST_CODE);
            }
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent back = new Intent(CameraBTN.this, MainActivity.class);
                startActivity(back);
            }
        });

        bTakePicture = findViewById(R.id.image_capture_button);
        bRecording = findViewById(R.id.video_capture_button);
        previewView = findViewById(R.id.viewFinder);

        bTakePicture.setOnClickListener(this);
        bRecording.setOnClickListener(this);

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startCameraX(cameraProvider);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, getExecutor());
    }
    private Executor getExecutor() {
        return ContextCompat.getMainExecutor(this);
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        Preview preview = new Preview.Builder().build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build();

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
    }
    @SuppressLint("RestrictedApi")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_capture_button: {
                if (bRecording.getText() == "RECORD") {
                    bRecording.setText("STOP");
                    recordVideo();
                } else {
                    bRecording.setText("RECORD");
                    videoCapture.stopRecording();
                }
                break;
            }
            case R.id.image_capture_button: {
                capturePhoto();
                break;
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void recordVideo() {
        if (videoCapture != null) {
            long timeStamp = System.currentTimeMillis();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            videoCapture.startRecording(
                    new VideoCapture.OutputFileOptions.Builder(
                            getContentResolver(),
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                            contentValues
                    ).build(),
                    getExecutor(),
                    new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                            Toast.makeText(CameraBTN.this,"Saving...",Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            Toast.makeText(CameraBTN.this,"Error: "+ message ,Toast.LENGTH_SHORT).show();
                        }
                    }

            );


        }
    }

    private void capturePhoto() {
        long timeStamp = System.currentTimeMillis();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, timeStamp);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");


        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(
                        getContentResolver(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                ).build(),
                getExecutor(),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Toast.makeText(CameraBTN.this,"Saving...",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(CameraBTN.this,"Error: "+exception.getMessage(),Toast.LENGTH_SHORT).show();


                    }
                });

    }
}
