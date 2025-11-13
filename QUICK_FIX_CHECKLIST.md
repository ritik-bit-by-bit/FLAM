# Quick Fix Checklist - Camera Feed Not Working

## Immediate Checks (Do These First!)

### ✅ Check 1: Is the App Running?
- [ ] App installed on device/emulator
- [ ] App opened and showing UI
- [ ] Camera permission granted (check Settings → Apps → FLAM → Permissions)

### ✅ Check 2: Open Android Studio Logcat
1. Open **Logcat** tab (bottom of Android Studio)
2. **Filter by**: `FrameProcessor|OpenCVProcessing|EdgeDetectionRenderer|MainActivity`
3. **Clear logs** (trash icon)
4. **Restart app** (close and reopen)
5. **Watch for messages**

### ✅ Check 3: Look for These Critical Messages

**MUST SEE (in order):**

1. **Native Library:**
   ```
   FrameProcessor: ✅ Native library 'opencv_processing' loaded successfully!
   ```
   ❌ If you see "Failed to load" → **OpenCV not installed/configured**

2. **Camera Start:**
   ```
   MainActivity: Camera bound successfully!
   ```
   ❌ If missing → **Camera not initializing**

3. **Frame Reception:**
   ```
   FrameProcessor: === 📸 FRAME RECEIVED ===
   ```
   ❌ If missing → **Camera not sending frames**

4. **OpenCV Call:**
   ```
   OpenCVProcessing: === OpenCV Native Function Called ===
   ```
   ❌ If missing → **OpenCV not working**

5. **Rendering:**
   ```
   EdgeDetectionRenderer: === 🎨 UPDATE FRAME ===
   EdgeDetectionRenderer: === 🖼️ onDrawFrame CALLED ===
   ```
   ❌ If missing → **Renderer not displaying**

## Most Common Issues

### Issue 1: "Failed to load native library"
**Problem:** OpenCV not installed or wrong path

**Fix:**
1. Download OpenCV Android SDK from https://opencv.org/releases/
2. Extract to: `app/src/main/jniLibs/opencv/`
3. Check `CMakeLists.txt` line 10 - path should match your OpenCV location
4. Clean project: `Build → Clean Project`
5. Rebuild: `Build → Rebuild Project`

### Issue 2: No "FRAME RECEIVED" messages
**Problem:** Camera not sending frames

**Fix:**
1. Check camera permission is granted
2. Check if using emulator (may not have camera)
3. Try front camera (code has fallback)
4. Check Logcat for camera errors

### Issue 3: "Native method not found"
**Problem:** Native library built but JNI function missing

**Fix:**
1. Clean and rebuild project
2. Check `opencv_processing.cpp` exists
3. Verify CMake is building the library
4. Check build output for errors

### Issue 4: OpenCV errors
**Problem:** OpenCV functions failing

**Fix:**
1. Check OpenCV version (need 4.8.0+)
2. Verify OpenCV libraries are linked
3. Check for missing dependencies

## Quick Test

Run this in terminal after starting app:
```powershell
.\check_logcat.ps1
```

Then check Logcat and share the output!

## What to Share

If still not working, share:
1. **Logcat output** (filter: `FrameProcessor|OpenCVProcessing|EdgeDetectionRenderer`)
2. **Which step is missing** (from checklist above)
3. **Any error messages** you see
4. **Device/emulator** you're using

This will help identify the exact issue!

