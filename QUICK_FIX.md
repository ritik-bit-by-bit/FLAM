# Quick Fix: Still Seeing Demo Frame

## Problem
Web viewer shows demo frame instead of real camera feed.

## Solution Steps

### 1. Stop Old Server
```bash
# Kill any running node processes
# Or just close the terminal running the old server
```

### 2. Start New Server (IMPORTANT!)
```bash
cd web
npm run serve
```

**CRITICAL**: You must use `npm run serve` (which runs `server.js`), NOT `npm run serve-simple`!

The new server will show:
```
Server running at:
  http://localhost:8080/
  http://192.168.1.4:8080/ (or your IP)

API endpoints:
  POST /api/frame - Receive frame from Android
  GET  /api/frame - Get latest frame

Waiting for frames from Android app...
```

### 3. Check Browser Console
Open browser DevTools (F12) and check Console tab. You should see:
- "FLAM Edge Detection Web Viewer initialized"
- "Waiting for frames from Android app..."
- If frames arrive: "Received real frame: 640x480"

### 4. Check Server Terminal
When Android app sends frames, you should see in server terminal:
```
[22:30:45] Received frame: 640x480, FPS: 25
```

### 5. Verify Android App
- Make sure Android app is running
- Check Logcat for "FrameSender" logs
- Should see "Frame sent successfully" messages

### 6. Network Check
- Android device and computer must be on same WiFi
- IP in `FrameSender.java` must match your computer's IP
- Try accessing `http://YOUR_IP:8080` from Android device's browser

## Still Not Working?

1. **Check if server is receiving frames:**
   - Look at server terminal for "Received frame" messages
   - If you see them, frames are arriving but viewer might not be fetching

2. **Check browser console:**
   - Open DevTools (F12) → Console
   - Look for errors or "Received real frame" messages

3. **Test API directly:**
   - Open browser and go to: `http://localhost:8080/api/frame`
   - Should show JSON with frame data or `{"error":"No frame available"}`

4. **Verify Android is sending:**
   - Check Android Logcat for "FrameSender" tag
   - Should see "Frame sent successfully" messages

