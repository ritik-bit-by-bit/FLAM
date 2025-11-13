package com.flam.edgedetection;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class FrameSender {
    private static final String TAG = "FrameSender";
    // Update this with your computer's IP address (find it with: ipconfig on Windows, ifconfig on Mac/Linux)
    // Make sure Android device and computer are on the same WiFi network
    private static final String SERVER_URL = "http://192.168.1.4:8080/api/frame";
    private static final int TIMEOUT_MS = 1000;
    
    private static boolean enabled = false;
    
    public static void setEnabled(boolean enabled) {
        FrameSender.enabled = enabled;
    }
    
    public static void setServerUrl(String url) {
        // Can be used to update server URL dynamically
    }
    
    public static void sendFrame(int[] pixels, int width, int height, int fps, long processingTime) {
        if (!enabled) {
            return;
        }
        
        new Thread(() -> {
            try {
                // Convert pixels to Bitmap
                Bitmap bitmap = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888);
                
                // Convert to base64
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                
                // Create JSON payload
                String json = String.format(
                    "{\"image\":\"data:image/png;base64,%s\",\"width\":%d,\"height\":%d,\"fps\":%d,\"processingTime\":%.2f,\"resolution\":{\"width\":%d,\"height\":%d}}",
                    base64Image, width, height, fps, processingTime / 1000000.0, width, height
                );
                
                // Send to server
                URL url = new URL(SERVER_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = json.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "Frame sent successfully: " + width + "x" + height);
                } else {
                    Log.w(TAG, "Failed to send frame, response code: " + responseCode);
                }
                
                conn.disconnect();
                
            } catch (IOException e) {
                Log.e(TAG, "Error sending frame: " + e.getMessage());
            }
        }).start();
    }
}

