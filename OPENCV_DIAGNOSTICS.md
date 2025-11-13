# OpenCV Diagnostics Guide

## How to Check if OpenCV is Working

After rebuilding and running the app, check Logcat with these filters:

### Filter 1: Check Native Library Loading
**Filter**: `FrameProcessor`

**Look for:**
```
✅ GOOD:
FrameProcessor: Attempting to load native library 'opencv_processing'...
FrameProcessor: ✅ Native library 'opencv_processing' loaded successfully!

❌ BAD:
FrameProcessor: ❌ CRITICAL: Failed to load native library 'opencv_processing'
```

**If you see the error:**
- Native library was not built
- OpenCV not properly configured
- Library name mismatch

### Filter 2: Check OpenCV Function Calls
**Filter**: `OpenCVProcessing`

**Look for:**
```
✅ GOOD:
OpenCVProcessing: === OpenCV Native Function Called ===
OpenCVProcessing: ✅ OpenCV is initialized and working!
OpenCVProcessing: ✅ YUV to RGB conversion successful: 640x480
OpenCVProcessing: ✅ Canny edge detection applied: 640x480
OpenCVProcessing: === OpenCV Processing Complete ===

❌ BAD:
OpenCVProcessing: ❌ OpenCV initialization failed
OpenCVProcessing: ❌ OpenCV conversion error
```

### Filter 3: Check Frame Processing
**Filter**: `FrameProcessor|OpenCVProcessing`

**Complete successful flow:**
```
FrameProcessor: === 📸 FRAME RECEIVED ===
FrameProcessor: 🔄 Converting YUV to NV21: 640x480
FrameProcessor: ✅ YUV conversion complete
FrameProcessor: 📞 Calling native OpenCV processFrame...
OpenCVProcessing: === OpenCV Native Function Called ===
OpenCVProcessing: ✅ OpenCV is initialized and working!
OpenCVProcessing: ✅ YUV to RGB conversion successful
OpenCVProcessing: ✅ Canny edge detection applied
OpenCVProcessing: === OpenCV Processing Complete ===
FrameProcessor: ✅ Native processing complete in 15.50ms
FrameProcessor: 📤 Sending frame to renderer...
```

## Common Issues

### Issue 1: "Failed to load native library"
**Causes:**
1. OpenCV not installed in `app/src/main/jniLibs/opencv/`
2. CMakeLists.txt has wrong OpenCV path
3. Native library not built (check build output)
4. Wrong ABI (armeabi-v7a vs arm64-v8a)

**Fix:**
1. Download OpenCV Android SDK
2. Extract to `app/src/main/jniLibs/opencv/`
3. Update CMakeLists.txt path if needed
4. Clean and rebuild project

### Issue 2: "Native method not found"
**Cause:** Library loaded but JNI function not found

**Fix:**
- Check function name matches exactly
- Rebuild native code
- Check for JNI signature mismatches

### Issue 3: "OpenCV conversion error"
**Cause:** OpenCV functions failing

**Fix:**
- Check OpenCV version compatibility
- Verify OpenCV libraries are linked correctly
- Check for missing OpenCV dependencies

## Quick Test

1. **Rebuild app**: Clean + Rebuild
2. **Run app** on device
3. **Check Logcat** with filter: `FrameProcessor|OpenCVProcessing`
4. **Look for** all ✅ markers in the flow above

If any step is missing, that's where OpenCV is failing!

