package com.flam.edgedetection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
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
    private Button effectButton;
    private TextView fpsTextView;
    private TextView resolutionTextView;
    private TextView processingTimeTextView;
    
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
        effectButton = findViewById(R.id.effectButton);
        fpsTextView = findViewById(R.id.fpsTextView);
        resolutionTextView = findViewById(R.id.resolutionTextView);
        processingTimeTextView = findViewById(R.id.processingTimeTextView);
        
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
        frameProcessor.setProcessingTimeCallback(timeMs -> 
            runOnUiThread(() -> processingTimeTextView.setText("Processing: " + String.format("%.2f", timeMs) + " ms")));
        
        // Enable frame sending to web viewer (update IP address in FrameSender.java)
        FrameSender.setEnabled(true);
        
        cameraExecutor = Executors.newSingleThreadExecutor();
        
        toggleButton.setOnClickListener(v -> {
            isProcessingEnabled = !isProcessingEnabled;
            // When processing is enabled, show edge-detected output
            // When disabled, show raw camera feed
            toggleButton.setText(isProcessingEnabled ? "Edge Detection ON" : "Edge Detection OFF");
            frameProcessor.setProcessingEnabled(isProcessingEnabled);
            Log.d("MainActivity", "Toggle: Processing " + (isProcessingEnabled ? "enabled" : "disabled"));
        });
        
        effectButton.setOnClickListener(v -> {
            renderer.cycleEffect();
            String[] effectNames = {"Normal", "Grayscale", "Invert"};
            int currentEffect = renderer.getEffectMode();
            effectButton.setText("Effect: " + effectNames[currentEffect]);
            Log.d("MainActivity", "Effect changed to: " + effectNames[currentEffect]);
        });
        
        if (checkCameraPermission()) {
            android.util.Log.d("MainActivity", "Camera permission granted, starting camera...");
            startCamera();
        } else {
            android.util.Log.d("MainActivity", "Camera permission not granted, requesting...");
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
                Log.d("MainActivity", "Camera permission granted by user, starting camera...");
                startCamera();
            } else {
                Log.e("MainActivity", "Camera permission denied by user");
                Toast.makeText(this, "Camera permission is required to use this app", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startCamera() {
        Log.d("MainActivity", "Starting camera...");
        
        // Ensure PreviewView is ready
        if (previewView == null) {
            Log.e("MainActivity", "PreviewView is null!");
            Toast.makeText(this, "PreviewView not initialized", Toast.LENGTH_LONG).show();
            return;
        }
        
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);
        
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Log.d("MainActivity", "Camera provider obtained");
                
                // Configure Preview (for raw camera feed - hidden but needed for camera stream)
                // Don't set target resolution - let camera choose best available
                Preview preview = new Preview.Builder()
                        .build();
                
                // Set surface provider on UI thread
                runOnUiThread(() -> {
                    try {
                        preview.setSurfaceProvider(previewView.getSurfaceProvider());
                        Log.d("MainActivity", "Preview surface provider set");
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error setting surface provider: " + e.getMessage(), e);
                    }
                });
                
                // Configure ImageAnalysis (for processing frames)
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setTargetResolution(new android.util.Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                        .build();
                
                imageAnalysis.setAnalyzer(cameraExecutor, frameProcessor);
                Log.d("MainActivity", "ImageAnalysis analyzer set");
                
                // Select camera
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                
                // Check if camera is available
                if (cameraProvider.hasCamera(cameraSelector)) {
                    Log.d("MainActivity", "Back camera available, binding...");
                } else {
                    Log.e("MainActivity", "Back camera not available!");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Back camera not available", Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);
                
                Log.d("MainActivity", "Camera bound successfully!");
                runOnUiThread(() -> {
                    Toast.makeText(this, "Camera started", Toast.LENGTH_SHORT).show();
                });
                
            } catch (ExecutionException e) {
                Log.e("MainActivity", "Camera initialization failed: " + e.getMessage(), e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } catch (InterruptedException e) {
                Log.e("MainActivity", "Camera initialization interrupted: " + e.getMessage(), e);
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                Log.e("MainActivity", "Unexpected camera error: " + e.getMessage(), e);
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
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

