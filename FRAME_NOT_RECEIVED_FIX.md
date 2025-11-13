# Web Viewer Not Receiving Frames - Diagnostic Guide

## Current Status
✅ Web viewer is working (no errors)
❌ Not receiving frames from Android app

## Step-by-Step Diagnosis

### Step 1: Check Android App is Running
- [ ] App is installed and running on device/emulator
- [ ] Camera permission granted
- [ ] App shows camera feed (or at least UI is visible)

### Step 2: Check Android Logcat
**Filter**: `FrameProcessor|FrameSender|MainActivity`

**Look for these messages:**

1. **FrameSender Enabled:**
   ```
   FrameSender: 📤 Attempting to send frame: 640x480, FPS: 30
   ```
   OR
   ```
   FrameSender: FrameSender is disabled - frames will not be sent
   ```
   ❌ If you see "disabled" → FrameSender is not enabled!

2. **Frame Sending:**
   ```
   FrameSender: ✅ Frame sent successfully to server: 640x480, FPS: 30
   ```
   OR
   ```
   FrameSender: ❌ Connection failed - Is server running?
   ```
   ❌ If you see connection errors → Check Steps 3-4

3. **Frame Processing:**
   ```
   FrameProcessor: 📡 Sending frame to web server (frame 5)
   ```
   ❌ If missing → Frames not being processed

### Step 3: Check Web Server Terminal
**When Android sends frames, you should see:**
```
📥 POST /api/frame from 192.168.x.x - Receiving frame data...
📦 Received body length: XXXXX bytes from 192.168.x.x
✅ Frame received and stored: 640x480, FPS: 30
```

**If you DON'T see these:**
- Android is not connecting to server
- Check IP address in `FrameSender.java`
- Check server is running

### Step 4: Check IP Address
**Current IP in code**: `http://192.168.1.4:8080/api/frame`

**Find your actual IP:**
- Windows: `ipconfig` → Look for "IPv4 Address"
- Mac/Linux: `ifconfig` or `ip addr`

**Update if different:**
1. Open: `app/src/main/java/com/flam/edgedetection/FrameSender.java`
2. Line 18: Change `192.168.1.4` to your IP
3. Rebuild and run app

### Step 5: Check Same WiFi Network
**CRITICAL**: Android device and computer must be on **same WiFi network**!

- ✅ Same WiFi = Works
- ❌ Different networks = Won't work
- ❌ Phone on mobile data = Won't work

### Step 6: Check Server is Running
**In terminal:**
```bash
cd web
npm run serve
```

**You should see:**
```
Server running at:
  http://localhost:8080/
  http://192.168.1.4:8080/ (or your IP)
```

## Quick Checklist

- [ ] Android app running and showing camera feed
- [ ] Logcat shows "Frame sent successfully"
- [ ] Server terminal shows "POST /api/frame"
- [ ] IP address matches your computer's IP
- [ ] Same WiFi network
- [ ] Server is running (`npm run serve`)

## Most Common Issues

### Issue 1: "FrameSender is disabled"
**Fix**: Check `MainActivity.java` line 79 - should have:
```java
FrameSender.setEnabled(true);
```

### Issue 2: "Connection failed"
**Causes:**
- Wrong IP address
- Server not running
- Different WiFi networks
- Firewall blocking connection

**Fix**: Check Steps 3-5 above

### Issue 3: No "Frame sent" messages
**Causes:**
- Camera not working (check Logcat for camera errors)
- OpenCV not processing frames
- Frames not reaching FrameSender

**Fix**: Check Logcat for camera/OpenCV errors

## What to Share

If still not working, share:
1. **Android Logcat** (filter: `FrameSender|FrameProcessor`)
2. **Server terminal output** (when app is running)
3. **Your computer's IP** (from `ipconfig`)
4. **IP in FrameSender.java** (line 18)

This will help identify the exact connection issue!

