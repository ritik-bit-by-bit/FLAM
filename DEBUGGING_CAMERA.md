# Debugging Camera Feed Issues

## Check Logcat for Errors

After building and running the app, check Android Logcat for these log messages:

### Expected Logs (Success):
```
FrameProcessor: Native library loaded successfully
FrameProcessor: Processing frame: 640x480
OpenCVProcessing: Processing frame: 640x480, processing: 1
OpenCVProcessing: YUV Mat created: 640x720
OpenCVProcessing: YUV to RGB conversion successful: 640x480
```

### Error Logs to Look For:

1. **Native Library Not Loading:**
```
FrameProcessor: Failed to load native library: ...
```
**Fix:** Ensure OpenCV SDK is properly placed in `app/src/main/jniLibs/opencv/`

2. **Unsupported Image Format:**
```
FrameProcessor: Unsupported image format: X
```
**Fix:** CameraX might be using a different format. Check what format is being used.

3. **OpenCV Conversion Error:**
```
OpenCVProcessing: OpenCV conversion error: ...
```
**Fix:** YUV conversion might be incorrect. Check YUV data format.

4. **Size Mismatch:**
```
OpenCVProcessing: Size mismatch: Mat(X,Y) vs expected(W,H)
```
**Fix:** YUV to RGB conversion is producing wrong dimensions.

## Common Issues and Solutions

### Issue 1: No Frames Being Processed
**Symptoms:** FPS stays at 0, no image displayed

**Check:**
- Camera permission granted?
- Camera initialized? (check logcat for camera provider errors)
- ImageAnalysis receiving frames? (look for "Processing frame" logs)

**Solution:**
- Grant camera permission in app settings
- Check if camera is available on device/emulator
- Verify ImageAnalysis is bound to camera lifecycle

### Issue 2: Native Library Not Found
**Symptoms:** App crashes or "Failed to load native library" error

**Solution:**
1. Ensure OpenCV SDK is downloaded
2. Place in: `app/src/main/jniLibs/opencv/`
3. Update CMakeLists.txt with correct OpenCV path
4. Rebuild project

### Issue 3: Black Screen / No Image
**Symptoms:** App runs but shows black screen

**Check:**
- Are frames being processed? (check logs)
- Is renderer receiving frames? (check "updateFrame" calls)
- Is OpenGL context created? (check for OpenGL errors)

**Solution:**
- Check if `renderer.updateFrame()` is being called
- Verify OpenGL shaders compile correctly
- Check if texture is being updated

### Issue 4: Wrong Colors / Distorted Image
**Symptoms:** Image displays but colors are wrong or distorted

**Check:**
- YUV to RGB conversion
- BGR to ARGB conversion in C++ code
- Pixel format in OpenGL texture

**Solution:**
- Verify YUV_420_888 to NV21 conversion is correct
- Check color channel order (OpenCV uses BGR, Android uses ARGB)

## Testing Steps

1. **Build and install app:**
   ```bash
   ./gradlew installDebug
   ```

2. **Open Logcat in Android Studio:**
   - View → Tool Windows → Logcat
   - Filter by: `FrameProcessor` or `OpenCVProcessing`

3. **Run app and check logs:**
   - Look for "Native library loaded successfully"
   - Look for "Processing frame" messages
   - Check for any error messages

4. **Verify camera feed:**
   - FPS should increase from 0
   - Resolution should display
   - Image should appear on screen

## Quick Test Commands

```bash
# View logs in real-time
adb logcat | grep -E "FrameProcessor|OpenCVProcessing"

# Clear logs and restart
adb logcat -c
# Then run app and check new logs
```

## Next Steps

If you see specific error messages in logcat, share them and I can help fix the exact issue!

