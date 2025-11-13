package com.flam.edgedetection;

import android.graphics.ImageFormat;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class FrameProcessor implements ImageAnalysis.Analyzer {
    private EdgeDetectionRenderer renderer;
    private boolean processingEnabled = true;
    private FpsCallback fpsCallback;
    private ResolutionCallback resolutionCallback;
    
    private long lastFrameTime = 0;
    private int frameCount = 0;
    private long fpsStartTime = System.currentTimeMillis();
    
    // Native methods
    static {
        System.loadLibrary("opencv_processing");
    }
    
    public native void processFrame(byte[] yuvData, int width, int height, 
                                   int[] outputPixels, boolean enableProcessing);
    
    public void setRenderer(EdgeDetectionRenderer renderer) {
        this.renderer = renderer;
    }
    
    public void setProcessingEnabled(boolean enabled) {
        this.processingEnabled = enabled;
    }
    
    public void setFpsCallback(FpsCallback callback) {
        this.fpsCallback = callback;
    }
    
    public void setResolutionCallback(ResolutionCallback callback) {
        this.resolutionCallback = callback;
    }
    
    @Override
    public void analyze(@NonNull ImageProxy image) {
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            processYUVFrame(image);
        }
        image.close();
    }
    
    private void processYUVFrame(ImageProxy image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        // YUV_420_888 to NV21 conversion
        // NV21 format: Y plane + interleaved VU plane
        int ySize = yBuffer.remaining();
        int uvSize = width * height / 2;  // UV plane size for NV21
        
        byte[] yuvData = new byte[ySize + uvSize];
        
        // Copy Y plane
        yBuffer.get(yuvData, 0, ySize);
        
        // Convert UV planes to interleaved VU (NV21 format)
        // In YUV_420_888, U and V are separate, we need to interleave them as VU
        int uvRowStride = planes[1].getRowStride();
        int uvPixelStride = planes[1].getPixelStride();
        int uvPlaneOffset = ySize;
        
        byte[] uRow = new byte[uvRowStride];
        byte[] vRow = new byte[planes[2].getRowStride()];
        
        for (int row = 0; row < height / 2; row++) {
            uBuffer.position(row * uvRowStride);
            uBuffer.get(uRow, 0, Math.min(uvRowStride, uBuffer.remaining()));
            
            vBuffer.position(row * planes[2].getRowStride());
            vBuffer.get(vRow, 0, Math.min(planes[2].getRowStride(), vBuffer.remaining()));
            
            // Interleave VU
            for (int col = 0; col < width / 2; col++) {
                int uvIndex = uvPlaneOffset + row * width + col * 2;
                yuvData[uvIndex] = vRow[col * uvPixelStride];     // V
                yuvData[uvIndex + 1] = uRow[col * uvPixelStride]; // U
            }
        }
        
        int[] outputPixels = new int[width * height];
        
        // Process frame using native OpenCV
        processFrame(yuvData, width, height, outputPixels, processingEnabled);
        
        // Update renderer with processed frame
        if (renderer != null) {
            renderer.updateFrame(outputPixels, width, height);
        }
        
        // Update resolution (only once per session or when changed)
        if (resolutionCallback != null) {
            resolutionCallback.onResolutionUpdate(width, height);
        }
        
        // Calculate FPS
        updateFps();
    }
    
    private void updateFps() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - fpsStartTime >= 1000) {
            int fps = frameCount;
            frameCount = 0;
            fpsStartTime = currentTime;
            
            if (fpsCallback != null) {
                fpsCallback.onFpsUpdate(fps);
            }
        }
    }
    
    public void release() {
        // Cleanup if needed
    }
    
    interface FpsCallback {
        void onFpsUpdate(int fps);
    }
    
    interface ResolutionCallback {
        void onResolutionUpdate(int width, int height);
    }
}

