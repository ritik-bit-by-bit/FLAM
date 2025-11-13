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
    private int totalFrameCount = 0; // Total frames processed (never resets)
    
    // Native methods
    static {
        try {
            android.util.Log.d("FrameProcessor", "Attempting to load native library 'opencv_processing'...");
            System.loadLibrary("opencv_processing");
            android.util.Log.d("FrameProcessor", "✅ Native library 'opencv_processing' loaded successfully!");
        } catch (UnsatisfiedLinkError e) {
            android.util.Log.e("FrameProcessor", "❌ CRITICAL: Failed to load native library 'opencv_processing'");
            android.util.Log.e("FrameProcessor", "Error: " + e.getMessage());
            android.util.Log.e("FrameProcessor", "Stack trace:");
            e.printStackTrace();
            android.util.Log.e("FrameProcessor", "Possible causes:");
            android.util.Log.e("FrameProcessor", "1. Native library not built (check build output)");
            android.util.Log.e("FrameProcessor", "2. OpenCV not properly configured in CMakeLists.txt");
            android.util.Log.e("FrameProcessor", "3. Library name mismatch");
            android.util.Log.e("FrameProcessor", "4. Missing OpenCV dependencies");
        } catch (Exception e) {
            android.util.Log.e("FrameProcessor", "❌ Unexpected error loading native library: " + e.getMessage());
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
    
    private int analyzeCallCount = 0; // Track analyze calls for reduced logging
    
    @Override
    public void analyze(@NonNull ImageProxy image) {
        analyzeCallCount++;
        
        // Log every 30 frames to reduce spam
        if (analyzeCallCount % 30 == 0) {
            android.util.Log.d("FrameProcessor", "=== 📸 FRAME RECEIVED (frame " + analyzeCallCount + ") ===");
            android.util.Log.d("FrameProcessor", "Size: " + image.getWidth() + "x" + image.getHeight());
            android.util.Log.d("FrameProcessor", "Format: " + image.getFormat() + " (35=YUV_420_888)");
            android.util.Log.d("FrameProcessor", "Processing enabled: " + processingEnabled);
        }
        
        try {
            if (image.getFormat() == ImageFormat.YUV_420_888) {
                processYUVFrame(image);
            } else {
                if (analyzeCallCount % 30 == 0) {
                    android.util.Log.w("FrameProcessor", "❌ Unsupported format: " + image.getFormat() + " (expected: " + ImageFormat.YUV_420_888 + ")");
                }
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
        
        if (analyzeCallCount % 30 == 0) {
            android.util.Log.d("FrameProcessor", "🔄 Converting YUV to NV21: " + width + "x" + height);
        }
        
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
            if (analyzeCallCount % 30 == 0) {
                android.util.Log.d("FrameProcessor", "✅ YUV conversion complete, size: " + yuvData.length);
                android.util.Log.d("FrameProcessor", "📞 Calling native OpenCV processFrame...");
                android.util.Log.d("FrameProcessor", "   Input: " + width + "x" + height + ", Processing: " + processingEnabled);
            }
            
            long startTime = System.nanoTime();
            
            // Process frame using native OpenCV
            try {
                processFrame(yuvData, width, height, outputPixels, processingEnabled);
                if (analyzeCallCount % 30 == 0) {
                    android.util.Log.d("FrameProcessor", "✅ Native processFrame returned successfully");
                }
            } catch (UnsatisfiedLinkError e) {
                android.util.Log.e("FrameProcessor", "❌ CRITICAL: Native method not found!");
                android.util.Log.e("FrameProcessor", "This means the native library was not loaded properly");
                android.util.Log.e("FrameProcessor", "Error: " + e.getMessage());
                android.util.Log.e("FrameProcessor", "Check build output for native library compilation errors");
                e.printStackTrace();
                image.close();
                return;
            } catch (Exception e) {
                android.util.Log.e("FrameProcessor", "❌ Error calling native processFrame: " + e.getMessage());
                e.printStackTrace();
                image.close();
                return;
            }
            
            long processingTime = System.nanoTime() - startTime;
            lastFrameProcessingTime = processingTime;
            double processingTimeMs = processingTime / 1_000_000.0; // Convert to milliseconds
            
            if (analyzeCallCount % 30 == 0) {
                android.util.Log.d("FrameProcessor", "✅ Native processing complete in " + String.format("%.2f", processingTimeMs) + "ms");
                android.util.Log.d("FrameProcessor", "Output pixels: " + outputPixels.length + " (expected: " + (width * height) + ")");
            }
            
            // Validate output pixels
            if (outputPixels.length != width * height) {
                android.util.Log.e("FrameProcessor", "❌ Output pixel count mismatch! Expected: " + (width * height) + ", Got: " + outputPixels.length);
                image.close();
                return;
            }
            
            // Check if pixels are all zeros (indicates processing might have failed)
            boolean allZeros = true;
            for (int i = 0; i < Math.min(100, outputPixels.length); i++) {
                if (outputPixels[i] != 0) {
                    allZeros = false;
                    break;
                }
            }
            if (allZeros && analyzeCallCount % 30 == 0) {
                android.util.Log.w("FrameProcessor", "⚠️ First 100 pixels are all zeros - OpenCV might not be processing correctly");
            }
            
            // Update processing time display
            if (processingTimeCallback != null) {
                processingTimeCallback.onProcessingTimeUpdate(processingTimeMs);
            }
            
            // Update renderer with processed frame
            if (renderer != null) {
                if (analyzeCallCount % 30 == 0) {
                    android.util.Log.d("FrameProcessor", "📤 Sending frame to renderer...");
                }
                renderer.updateFrame(outputPixels, width, height);
                if (analyzeCallCount % 30 == 0) {
                    android.util.Log.d("FrameProcessor", "✅ Frame sent to renderer");
                }
            } else {
                if (analyzeCallCount == 1) {
                    android.util.Log.e("FrameProcessor", "❌ Renderer is NULL! Frames will not be displayed!");
                }
            }
            
            // Update resolution (only once per session or when changed)
            if (resolutionCallback != null) {
                resolutionCallback.onResolutionUpdate(width, height);
            }
            
            // Increment total frame count (never resets)
            totalFrameCount++;
            
            // Calculate FPS (increments frameCount, resets every second)
            updateFps();
            
            // Send frame to web viewer (every 5 frames to reduce network load)
            // Use totalFrameCount which never resets, so we always send frame 1, 5, 10, 15, etc.
            // ALWAYS send first frame immediately, then every 5th frame
            boolean shouldSend = (totalFrameCount == 1 || totalFrameCount % 5 == 0);
            
            if (shouldSend) {
                android.util.Log.d("FrameProcessor", "📡 Sending frame to web server (total frame " + totalFrameCount + ")");
                android.util.Log.d("FrameProcessor", "   Frame details: " + width + "x" + height + ", pixels: " + outputPixels.length);
                try {
                    // Make a copy of pixels to avoid issues with concurrent access
                    int[] pixelsCopy = new int[outputPixels.length];
                    System.arraycopy(outputPixels, 0, pixelsCopy, 0, outputPixels.length);
                    FrameSender.sendFrame(pixelsCopy, width, height, currentFps, processingTime);
                    android.util.Log.d("FrameProcessor", "✅ FrameSender.sendFrame() called successfully");
                } catch (Exception e) {
                    android.util.Log.e("FrameProcessor", "❌ Error calling FrameSender: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (totalFrameCount < 10) {
                // Log why we're not sending for first few frames
                android.util.Log.d("FrameProcessor", "⏭️ Skipping frame " + totalFrameCount + " (will send on frame " + ((totalFrameCount / 5 + 1) * 5) + ")");
            }
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

