# Test Connection Between Android and Web Server

## Step 1: Verify Server is Running

```bash
cd web
npm run serve
```

You should see:
```
========================================
Server running at:
  http://localhost:8080/
  http://192.168.1.4:8080/ (or your IP)

API endpoints:
  POST /api/frame - Receive frame from Android
  GET  /api/frame - Get latest frame

Waiting for frames from Android app...
========================================
```

## Step 2: Test Server is Receiving Requests

When you run the Android app, you should see in server terminal:
```
[22:30:45] POST /api/frame from 192.168.1.X
POST /api/frame - Receiving frame data...
Received body length: XXXXX bytes
[22:30:45] ✓ Received frame: 640x480, FPS: 25
    Image data length: XXXXX chars
```

## Step 3: Check Android Logcat

In Android Studio Logcat, filter by "FrameSender" and look for:
- `✓ Frame sent successfully: 640x480, FPS: 25` - SUCCESS
- `✗ Connection failed - Is server running?` - Server not running or wrong IP
- `✗ Connection timeout` - Network issue
- `✗ Error sending frame` - Other error

## Step 4: Common Issues

### No logs in server terminal
**Problem**: Server not receiving requests
**Solutions**:
1. Check if server is actually running (`npm run serve`)
2. Verify IP address in `FrameSender.java` matches your computer's IP
3. Check if Android device and computer are on same WiFi
4. Check Windows Firewall - might be blocking port 8080

### "Connection failed" in Android Logcat
**Problem**: Android can't reach server
**Solutions**:
1. Verify server is running
2. Check IP address is correct (use `ipconfig` on Windows)
3. Try accessing `http://YOUR_IP:8080` from Android device's browser
4. Check firewall settings

### "Empty request body" in server logs
**Problem**: Request received but no data
**Solutions**:
1. Check Android Logcat for errors during frame sending
2. Verify frame processing is working (check if frames are being processed)
3. Check if `FrameSender.sendFrame()` is being called

### Server receives but browser doesn't show
**Problem**: Web viewer not fetching frames
**Solutions**:
1. Open browser console (F12)
2. Check for "Received real frame" messages
3. Check for fetch errors
4. Try refreshing the page

## Quick Test Commands

### Test server API directly:
```bash
# From browser or curl
curl http://localhost:8080/api/frame
# Should return: {"error":"No frame available"}
```

### Test from Android device browser:
1. Open browser on Android
2. Go to: `http://YOUR_COMPUTER_IP:8080`
3. Should see the web viewer page
4. If this works, network is fine!

### Check your IP:
```bash
# Windows
ipconfig

# Look for IPv4 Address under your WiFi adapter
```

## Debug Checklist

- [ ] Server is running (`npm run serve`)
- [ ] Server shows "Waiting for frames..." message
- [ ] IP in `FrameSender.java` matches computer's IP
- [ ] Android device and computer on same WiFi
- [ ] Android app is running
- [ ] Camera permission granted
- [ ] Check Android Logcat for "FrameSender" messages
- [ ] Check server terminal for incoming requests
- [ ] Check browser console (F12) for errors

