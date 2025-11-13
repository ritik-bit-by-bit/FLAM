package com.flam.edgedetection;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Size;

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
        
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        
        byte[] yuvData = new byte[ySize + uSize + vSize];
        yBuffer.get(yuvData, 0, ySize);
        uBuffer.get(yuvData, ySize, uSize);
        vBuffer.get(yuvData, ySize + uSize, vSize);
        
        int width = image.getWidth();
        int height = image.getHeight();
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

