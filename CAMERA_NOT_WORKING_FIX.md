# Camera Not Working - Diagnostic Guide

## What I Fixed

1. ✅ Changed PreviewView from `visibility="gone"` to `visibility="invisible"` (camera needs view in layout)
2. ✅ Removed forced resolution (let camera choose best)
3. ✅ Added front camera fallback (if back camera unavailable)
4. ✅ Improved error handling and logging
5. ✅ Fixed camera binding on UI thread

## Step-by-Step Diagnosis

### Step 1: Rebuild App
1. In Android Studio: **Build → Clean Project**
2. Then: **Build → Rebuild Project**
3. Install on device: Click **Run** button

### Step 2: Check Logcat
Open **Logcat** in Android Studio and filter by: `MainActivity|FrameProcessor`

**Look for these messages in order:**

```
✅ GOOD - Camera is working:
MainActivity: Starting camera...
MainActivity: Camera provider obtained
MainActivity: Preview surface provider set
MainActivity: ImageAnalysis analyzer set
MainActivity: Camera available, binding to lifecycle...
MainActivity: Camera bound successfully!
FrameProcessor: Frame received: 640x480, format: 35
FrameProcessor: Processing frame: 640x480
```

```
❌ BAD - Camera not working:
MainActivity: PreviewView is null!
OR
MainActivity: Back camera not available!
OR
MainActivity: No camera available!
OR
MainActivity: Camera initialization failed: ...
OR
(No "Frame received" messages)
```

### Step 3: Common Issues & Fixes

#### Issue 1: "PreviewView is null"
**Fix**: This shouldn't happen, but if it does, the layout might not be loading. Check if app crashes on startup.

#### Issue 2: "No camera available"
**Possible causes:**
- Using emulator without camera
- Device has no camera
- Camera already in use by another app

**Fix:**
- Use physical device with camera
- Or configure emulator with camera: AVD Manager → Edit → Show Advanced → Camera → Webcam0

#### Issue 3: "Camera initialization failed"
**Check the error message** - it will tell you what's wrong:
- Permission denied → Grant camera permission
- Camera in use → Close other camera apps
- Other error → Share the exact error message

#### Issue 4: Camera bound but no frames
**Symptoms:**
- See "Camera bound successfully!"
- But NO "Frame received" messages

**Possible causes:**
- ImageAnalysis not receiving frames
- FrameProcessor not processing
- OpenCV library not loaded

**Fix:**
- Check if you see "Native library loaded successfully" in logs
- Check for OpenCV errors

### Step 4: Quick Test

1. **Grant Permission:**
   - When app starts, grant camera permission
   - Or: Settings → Apps → FLAM Edge Detection → Permissions → Camera → Allow

2. **Check Toast Messages:**
   - Should see "Camera started successfully" toast
   - If you see error toast, read the message

3. **Check FPS Counter:**
   - Should start increasing from 0
   - If stays at 0, camera not sending frames

4. **Check Screen:**
   - Should see something on GLSurfaceView (not just black)
   - Even if processing fails, you should see something

## What to Share for Help

If camera still doesn't work, share:

1. **Full Logcat output** (filter: `MainActivity|FrameProcessor|OpenCVProcessing`)
2. **Toast messages** you see on screen
3. **Device/Emulator** you're using
4. **What you see** on screen (black? app crashes? nothing?)

## Expected Behavior When Working

1. App starts → Permission dialog appears
2. Grant permission → Camera initializes
3. Toast: "Camera started successfully"
4. Screen shows camera feed (processed or raw)
5. FPS counter increases (10-30 FPS)
6. Resolution shows (e.g., "640x480")
7. Processing time shows (e.g., "15.50 ms")

If you see all of these, camera is working! 🎉

