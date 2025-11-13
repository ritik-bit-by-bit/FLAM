# Web Viewer Not Showing Live Camera Feed - Troubleshooting

## Why You're Seeing Test Pattern Instead of Camera Feed

The web viewer is showing a test pattern because **frames from the Android app are not reaching the web server**.

## Step-by-Step Troubleshooting

### Step 1: Check Web Server is Running

1. Open terminal/command prompt
2. Navigate to `web` folder:
   ```bash
   cd web
   ```
3. Start server:
   ```bash
   npm run serve
   ```
4. You should see:
   ```
   Server running at:
     http://localhost:8080/
     http://192.168.1.4:8080/ (or your IP)
   ```

**If server is NOT running → Start it!**

### Step 2: Check Your Computer's IP Address

The Android app is configured to send frames to: `http://192.168.1.4:8080/api/frame`

**Find your actual IP:**

**Windows:**
```bash
ipconfig
```
Look for "IPv4 Address" under your WiFi adapter (usually starts with 192.168.x.x)

**Mac/Linux:**
```bash
ifconfig
```
or
```bash
ip addr
```

**If your IP is different from 192.168.1.4 → Update it in FrameSender.java!**

### Step 3: Update IP Address in Android App

1. Open: `app/src/main/java/com/flam/edgedetection/FrameSender.java`
2. Find line 18:
   ```java
   private static final String SERVER_URL = "http://192.168.1.4:8080/api/frame";
   ```
3. Replace `192.168.1.4` with **your computer's IP address**
4. Rebuild and run the app

### Step 4: Check Android Logcat

1. Open Android Studio
2. Open Logcat (bottom tab)
3. Filter by: `FrameSender`
4. Look for these messages:

**✅ GOOD - Frames are being sent:**
```
FrameSender: ✓ Frame sent successfully: 640x480, FPS: 30
```

**❌ BAD - Connection failed:**
```
FrameSender: ✗ Connection failed - Is server running?
FrameSender:   Server URL: http://192.168.1.4:8080/api/frame
FrameSender:   Make sure: 1) Server is running, 2) Same WiFi network, 3) Correct IP address
```

**If you see connection errors → Check Steps 1-3!**

### Step 5: Check Web Server Terminal

When Android sends frames, you should see in the server terminal:
```
[timestamp] POST /api/frame from 192.168.x.x
POST /api/frame - Receiving frame data...
Received body length: XXXXX bytes
Frame received: 640x480, FPS: 30
```

**If you DON'T see these messages → Android is not connecting to server!**

### Step 6: Verify Same WiFi Network

**CRITICAL**: Android device and computer must be on the **same WiFi network**!

- ✅ Same WiFi network = Works
- ❌ Different networks = Won't work
- ❌ Phone on mobile data = Won't work

### Step 7: Check Web Viewer Console

1. Open web viewer in browser: `http://localhost:8080`
2. Press `F12` to open Developer Tools
3. Go to Console tab
4. Look for messages:

**✅ GOOD:**
```
Received real frame: 640x480
```

**❌ BAD:**
```
No frame available yet: No frame available
```

## Quick Checklist

- [ ] Web server is running (`npm run serve`)
- [ ] IP address in `FrameSender.java` matches your computer's IP
- [ ] Android device and computer on same WiFi network
- [ ] Android app is running and showing camera feed
- [ ] Logcat shows "✓ Frame sent successfully"
- [ ] Server terminal shows "POST /api/frame" messages
- [ ] Browser console shows "Received real frame"

## Common Issues

### Issue 1: "Connection failed" in Logcat
**Cause**: Wrong IP address or server not running
**Fix**: Check Steps 1-3

### Issue 2: "No frame available" in browser
**Cause**: Android not sending frames or server not receiving
**Fix**: Check Logcat and server terminal

### Issue 3: Server shows "POST /api/frame" but browser shows nothing
**Cause**: Browser not polling or cached old page
**Fix**: Hard refresh browser (Ctrl+F5) or clear cache

### Issue 4: Frames sent but wrong IP
**Cause**: IP address changed (WiFi reconnected)
**Fix**: Update IP in `FrameSender.java` and rebuild

## Test Connection

1. **Start server**: `cd web && npm run serve`
2. **Open test page**: `http://localhost:8080/test`
3. **Click**: "Start Auto-Polling"
4. **Run Android app**
5. **Watch**: Frames should appear!

## Still Not Working?

Share:
1. Your computer's IP address (from `ipconfig`)
2. IP address in `FrameSender.java`
3. Logcat output (filter: `FrameSender`)
4. Server terminal output
5. Browser console messages

This will help identify the exact issue!

