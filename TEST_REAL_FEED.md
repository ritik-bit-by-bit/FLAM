# Testing Real Camera Feed

## Important: Browser Does NOT Need Camera Permission!

**The browser is NOT accessing your camera directly.** It only displays images that the Android app sends to the server. The Android app handles all camera access.

## Step-by-Step Testing

### Step 1: Start Web Server
```bash
cd web
npm run serve
```

You should see:
```
Server running at:
  http://localhost:8080/
  http://192.168.1.4:8080/ (or your IP)
```

### Step 2: Open Test Page
Open in browser:
- `http://localhost:8080/test` (test connection page)
- OR `http://localhost:8080` (main viewer)

**Note**: The insecure connection warning is normal for HTTP (not HTTPS). It's safe to proceed. The browser is NOT accessing your camera.

### Step 3: Run Android App
1. Open Android Studio
2. Build and run the app on device/emulator
3. Grant camera permission when prompted
4. App should start showing camera feed

### Step 4: Check Connection
1. On the test page (`/test`), click "Start Auto-Polling"
2. This will check for frames every 500ms
3. When Android sends frames, you'll see:
   - ✓ Frame received message
   - Image displayed
   - FPS, resolution, processing time

### Step 5: View Real Feed
1. Go to main viewer: `http://localhost:8080`
2. Should automatically poll for frames
3. Real camera feed will appear when Android sends it

## Troubleshooting

### "No frames received yet"
**Possible causes:**
1. Android app not running
2. Android app not sending frames (check Logcat)
3. Network issue (different WiFi networks)
4. Wrong IP address in `FrameSender.java`

**Fix:**
- Check Android Logcat for "FrameSender" messages
- Should see "✓ Frame sent successfully"
- If you see "✗ Connection failed", check IP address

### Browser Shows "Insecure Connection"
**This is normal!** 
- HTTP (not HTTPS) shows this warning
- It's safe to proceed - browser is NOT accessing camera
- Click "Advanced" → "Proceed to localhost" (or your IP)

### Server Not Responding
**Check:**
1. Is server running? (`npm run serve`)
2. Can you access `http://localhost:8080`?
3. Check server terminal for errors

## Quick Test

1. **Start server**: `cd web && npm run serve`
2. **Open browser**: `http://localhost:8080/test`
3. **Click**: "Start Auto-Polling"
4. **Run Android app** on device
5. **Watch**: Frames should appear!

## Network Requirements

- Android device and computer must be on **same WiFi network**
- Update IP in `FrameSender.java` to match your computer's IP
- Find your IP: `ipconfig` (Windows) or `ifconfig` (Mac/Linux)

## What to Expect

When working correctly:
1. Android app shows camera feed (on device)
2. Android sends frames to server (every 5th frame)
3. Server receives frames (check server terminal)
4. Browser displays frames (on web viewer)
5. Stats update (FPS, resolution, processing time)

The browser never accesses the camera - it just displays what Android sends!

