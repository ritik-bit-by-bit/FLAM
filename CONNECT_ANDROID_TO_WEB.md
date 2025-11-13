# Connect Android App to Web Viewer

## Setup Instructions

### Step 1: Find Your Computer's IP Address

**Windows:**
```bash
ipconfig
```
Look for "IPv4 Address" under your WiFi adapter (e.g., 192.168.1.4)

**Mac/Linux:**
```bash
ifconfig
```
or
```bash
ip addr show
```

### Step 2: Update Android App

1. Open `app/src/main/java/com/flam/edgedetection/FrameSender.java`
2. Update the `SERVER_URL` constant with your computer's IP:
   ```java
   private static final String SERVER_URL = "http://YOUR_IP:8080/api/frame";
   ```
   Replace `YOUR_IP` with your actual IP address (e.g., `192.168.1.4`)

### Step 3: Start Web Server

```bash
cd web
npm run serve
```

The server will start on port 8080 and show:
```
Server running at http://localhost:8080/
API endpoints:
  POST /api/frame - Receive frame from Android
  GET  /api/frame - Get latest frame
```

### Step 4: Ensure Same Network

- **Important**: Your Android device and computer must be on the same WiFi network
- The Android app cannot connect if they're on different networks

### Step 5: Run Android App

1. Build and install the app on your Android device
2. Grant camera permission
3. The app will automatically start sending frames to the web viewer

### Step 6: Open Web Viewer

Open your browser and go to:
```
http://YOUR_IP:8080
```
or
```
http://localhost:8080
```

You should now see real-time camera feed from your Android device!

## Troubleshooting

### No Frames Appearing

1. **Check IP Address:**
   - Verify the IP in `FrameSender.java` matches your computer's IP
   - Make sure it's the WiFi IP, not localhost (127.0.0.1)

2. **Check Network:**
   - Ensure Android device and computer are on same WiFi
   - Try pinging from Android: Install a network tool app and ping your computer's IP

3. **Check Server:**
   - Is the web server running? Check terminal for "Server running..."
   - Try accessing `http://YOUR_IP:8080` from your phone's browser

4. **Check Logs:**
   - Android Logcat: Look for "FrameSender" logs
   - Should see "Frame sent successfully" messages
   - If you see errors, check the error message

5. **Firewall:**
   - Windows Firewall might block port 8080
   - Allow Node.js through firewall or disable temporarily for testing

### Testing Connection

You can test if the server is reachable from your Android device:

1. Open browser on Android device
2. Go to: `http://YOUR_IP:8080`
3. You should see the web viewer page

If this works, the network connection is fine and frames should flow!

## How It Works

1. **Android App** processes camera frames with OpenCV
2. **FrameSender** converts processed frame to base64 image
3. Sends frame via HTTP POST to web server
4. **Web Server** stores the latest frame
5. **Web Viewer** polls server every 100ms to get latest frame
6. Displays frame in real-time

## Performance Notes

- Frames are sent every 5th frame to reduce network load
- Each frame is compressed to PNG at 80% quality
- Web viewer updates at ~10 FPS (100ms polling interval)
- Adjust these values in code if needed for your use case

