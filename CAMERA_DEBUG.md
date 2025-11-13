# Camera Feed Debugging Guide

## Check Logcat for Camera Status

After running the app, check Android Studio Logcat (filter by "MainActivity" or "FrameProcessor"):

### Expected Success Logs:
```
MainActivity: Starting camera...
MainActivity: Camera provider obtained
MainActivity: ImageAnalysis analyzer set
MainActivity: Back camera available, binding...
MainActivity: Camera bound successfully!
FrameProcessor: Frame received: 640x480, format: 35
FrameProcessor: Processing frame: 640x480
```

### Common Error Logs:

1. **Permission Denied:**
```
MainActivity: Camera permission denied by user
```
**Fix:** Grant camera permission in app settings

2. **Camera Not Available:**
```
MainActivity: Back camera not available!
```
**Fix:** 
- Use emulator with camera support
- Or use front camera: Change `DEFAULT_BACK_CAMERA` to `DEFAULT_FRONT_CAMERA`

3. **No Frames Received:**
```
MainActivity: Camera bound successfully!
(but no "Frame received" messages)
```
**Fix:**
- Check if camera is actually working (try other camera apps)
- Verify ImageAnalysis is set up correctly
- Check device/emulator has camera support

4. **Format Not Supported:**
```
FrameProcessor: Unsupported image format: X (expected: 35)
```
**Fix:** Camera might be using different format, check and handle accordingly

## Quick Tests

### Test 1: Check Camera Permission
1. Run app
2. Check if permission dialog appears
3. Grant permission
4. Check Logcat for "Camera permission granted"

### Test 2: Verify Camera Initialization
1. Check Logcat for "Camera bound successfully!"
2. If you see this, camera is initialized
3. If not, check error messages

### Test 3: Check Frame Reception
1. After camera starts, look for "Frame received" messages
2. Should see these continuously (every ~33ms for 30fps)
3. If no frames, ImageAnalysis might not be working

### Test 4: Check Processing
1. Look for "Processing frame: WxH" messages
2. Should see these after "Frame received"
3. If not, YUV conversion might be failing

## Troubleshooting Steps

1. **Camera Permission:**
   - Settings → Apps → FLAM Edge Detection → Permissions → Camera → Allow

2. **Emulator Camera:**
   - If using emulator, make sure it has camera support
   - AVD Manager → Edit → Show Advanced Settings → Camera → Webcam0

3. **Physical Device:**
   - Make sure camera works in other apps
   - Some devices need additional permissions

4. **Check Logs:**
   - Filter Logcat by: `MainActivity|FrameProcessor|Camera`
   - Look for any ERROR or WARN messages

## Expected Behavior

When working correctly:
- App starts → Permission requested → Camera initializes → Frames start flowing
- Logcat shows continuous "Frame received" messages
- FPS counter starts increasing
- Resolution displays actual camera resolution
- OpenGL view shows processed/raw camera feed

If any step fails, check the corresponding log messages!

