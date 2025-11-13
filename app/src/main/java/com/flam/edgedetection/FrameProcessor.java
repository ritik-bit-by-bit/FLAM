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
    private long lastFrameProcessingTime = 0;
    private int currentFps = 0;
    
    // Native methods
    static {
        try {
            System.loadLibrary("opencv_processing");
            android.util.Log.d("FrameProcessor", "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.e("FrameProcessor", "Failed to load native library: " + e.getMessage());
            e.printStackTrace();
        }
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
    
    public void setProcessingTimeCallback(ProcessingTimeCallback callback) {
        this.processingTimeCallback = callback;
    }
    
    @Override
    public void analyze(@NonNull ImageProxy image) {
        try {
            android.util.Log.d("FrameProcessor", "=== 📸 FRAME RECEIVED ===");
            android.util.Log.d("FrameProcessor", "Size: " + image.getWidth() + "x" + image.getHeight());
            android.util.Log.d("FrameProcessor", "Format: " + image.getFormat() + " (35=YUV_420_888)");
            android.util.Log.d("FrameProcessor", "Processing enabled: " + processingEnabled);
            
            if (image.getFormat() == ImageFormat.YUV_420_888) {
                processYUVFrame(image);
            } else {
                android.util.Log.w("FrameProcessor", "❌ Unsupported format: " + image.getFormat() + " (expected: " + ImageFormat.YUV_420_888 + ")");
            }
        } catch (Exception e) {
            android.util.Log.e("FrameProcessor", "❌ ERROR processing frame: " + e.getMessage(), e);
        } finally {
            image.close();
        }
    }
    
    private void processYUVFrame(ImageProxy image) {
        Image.Plane[] planes = image.getPlanes();
        if (planes.length < 3) {
            android.util.Log.e("FrameProcessor", "Invalid YUV image: expected 3 planes, got " + planes.length);
            return;
        }
        
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        
        int width = image.getWidth();
        int height = image.getHeight();
        
        android.util.Log.d("FrameProcessor", "🔄 Converting YUV to NV21: " + width + "x" + height);
        
        // YUV_420_888 to NV21 conversion
        // NV21 format: Y plane + interleaved VU plane
        int ySize = width * height;
        int uvSize = width * height / 2;  // UV plane size for NV21
        byte[] yuvData = new byte[ySize + uvSize];
        
        // Copy Y plane (ensure we copy exactly width*height bytes)
        yBuffer.rewind();
        if (yBuffer.remaining() >= ySize) {
            yBuffer.get(yuvData, 0, ySize);
        } else {
            android.util.Log.w("FrameProcessor", "Y buffer smaller than expected: " + yBuffer.remaining() + " < " + ySize);
            yBuffer.get(yuvData, 0, Math.min(ySize, yBuffer.remaining()));
        }
        
        // Convert UV planes to interleaved VU (NV21 format)
        int uvRowStride = planes[1].getRowStride();
        int uvPixelStride = planes[1].getPixelStride();
        int vRowStride = planes[2].getRowStride();
        int vPixelStride = planes[2].getPixelStride();
        int uvPlaneOffset = ySize;
        
        // Interleave U and V planes
        ByteBuffer uBufferCopy = uBuffer.duplicate();
        ByteBuffer vBufferCopy = vBuffer.duplicate();
        
        for (int row = 0; row < height / 2; row++) {
            for (int col = 0; col < width / 2; col++) {
                int uPos = row * uvRowStride + col * uvPixelStride;
                int vPos = row * vRowStride + col * vPixelStride;
                
                if (uPos < uBufferCopy.limit() && vPos < vBufferCopy.limit()) {
                    uBufferCopy.position(uPos);
                    vBufferCopy.position(vPos);
                    
                    int uvIndex = uvPlaneOffset + row * width + col * 2;
                    yuvData[uvIndex] = vBufferCopy.get();     // V
                    yuvData[uvIndex + 1] = uBufferCopy.get(); // U
                }
            }
        }
        
        int[] outputPixels = new int[width * height];
        
        try {
            android.util.Log.d("FrameProcessor", "✅ YUV conversion complete, size: " + yuvData.length);
            android.util.Log.d("FrameProcessor", "📞 Calling native OpenCV processFrame...");
            
            long startTime = System.nanoTime();
            
            // Process frame using native OpenCV
            processFrame(yuvData, width, height, outputPixels, processingEnabled);
            
            long processingTime = System.nanoTime() - startTime;
            lastFrameProcessingTime = processingTime;
            double processingTimeMs = processingTime / 1_000_000.0; // Convert to milliseconds
            
            android.util.Log.d("FrameProcessor", "✅ Native processing complete in " + String.format("%.2f", processingTimeMs) + "ms");
            android.util.Log.d("FrameProcessor", "Output pixels: " + outputPixels.length + " (expected: " + (width * height) + ")");
            
            // Update processing time display
            if (processingTimeCallback != null) {
                processingTimeCallback.onProcessingTimeUpdate(processingTimeMs);
            }
            
            // Update renderer with processed frame
            if (renderer != null) {
                android.util.Log.d("FrameProcessor", "📤 Sending frame to renderer...");
                renderer.updateFrame(outputPixels, width, height);
                android.util.Log.d("FrameProcessor", "✅ Frame sent to renderer");
            } else {
                android.util.Log.e("FrameProcessor", "❌ Renderer is NULL!");
            }
            
            // Send frame to web viewer (every 5 frames to reduce network load)
            if (frameCount % 5 == 0) {
                FrameSender.sendFrame(outputPixels, width, height, currentFps, processingTime);
            }
            
            // Update resolution (only once per session or when changed)
            if (resolutionCallback != null) {
                resolutionCallback.onResolutionUpdate(width, height);
            }
            
            // Calculate FPS
            updateFps();
        } catch (Exception e) {
            android.util.Log.e("FrameProcessor", "Error in native processing: " + e.getMessage(), e);
        }
    }
    
    private void updateFps() {
        frameCount++;
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - fpsStartTime >= 1000) {
            currentFps = frameCount;
            frameCount = 0;
            fpsStartTime = currentTime;
            
            if (fpsCallback != null) {
                fpsCallback.onFpsUpdate(currentFps);
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
    
    interface ProcessingTimeCallback {
        void onProcessingTimeUpdate(double timeMs);
    }
}

