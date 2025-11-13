package com.flam.edgedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    
    private PreviewView previewView;
    private GLSurfaceView glSurfaceView;
    private Button toggleButton;
    private TextView fpsTextView;
    private TextView resolutionTextView;
    
    private EdgeDetectionRenderer renderer;
    private FrameProcessor frameProcessor;
    private ExecutorService cameraExecutor;
    
    private boolean isProcessingEnabled = true;
    private Camera camera;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        previewView = findViewById(R.id.previewView);
        glSurfaceView = findViewById(R.id.glSurfaceView);
        toggleButton = findViewById(R.id.toggleButton);
        fpsTextView = findViewById(R.id.fpsTextView);
        resolutionTextView = findViewById(R.id.resolutionTextView);
        
        // Initialize OpenGL renderer
        renderer = new EdgeDetectionRenderer(this);
        renderer.setGLSurfaceView(glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        // Initialize frame processor
        frameProcessor = new FrameProcessor();
        frameProcessor.setRenderer(renderer);
        frameProcessor.setFpsCallback(fps -> runOnUiThread(() -> fpsTextView.setText("FPS: " + fps)));
        frameProcessor.setResolutionCallback((width, height) -> 
            runOnUiThread(() -> resolutionTextView.setText("Resolution: " + width + "x" + height)));
        
        cameraExecutor = Executors.newSingleThreadExecutor();
        
        toggleButton.setOnClickListener(v -> {
            isProcessingEnabled = !isProcessingEnabled;
            toggleButton.setText(isProcessingEnabled ? "Show Raw" : "Show Processed");
            frameProcessor.setProcessingEnabled(isProcessingEnabled);
        });
        
        if (checkCameraPermission()) {
            startCamera();
        } else {
            requestCameraPermission();
        }
    }
    
    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED;
    }
    
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, 
                CAMERA_PERMISSION_REQUEST_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                
                imageAnalysis.setAnalyzer(cameraExecutor, frameProcessor);
                
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);
                
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (frameProcessor != null) {
            frameProcessor.release();
        }
        if (renderer != null) {
            renderer.release();
        }
    }
}

