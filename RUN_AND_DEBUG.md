# Run App and Capture Logcat Output

## Method 1: Using Android Studio (Recommended)

### Step 1: Open Project
1. Open Android Studio
2. File → Open → Select the `floww` folder
3. Wait for Gradle sync to complete

### Step 2: Build Project
1. Build → Make Project (or press `Ctrl+F9`)
2. Wait for build to complete
3. Check for any build errors in the Build output

### Step 3: Run on Device/Emulator
1. Connect Android device via USB (enable USB debugging)
   - OR start an Android emulator
2. Click Run button (green play icon) or press `Shift+F10`
3. Select your device/emulator
4. Wait for app to install and launch

### Step 4: View Logcat
1. Open Logcat tab (bottom of Android Studio)
2. Filter by: `MainActivity|FrameProcessor|FrameSender|OpenCVProcessing`
3. Or use filter: `tag:MainActivity OR tag:FrameProcessor OR tag:FrameSender`

### Step 5: Copy Logcat Output
1. Select all logs (Ctrl+A)
2. Copy (Ctrl+C)
3. Paste here or save to a file

## Method 2: Using Command Line (ADB)

### Prerequisites
- Android SDK platform-tools in PATH
- Device connected via USB with USB debugging enabled

### Step 1: Build APK
```bash
# If gradlew exists
.\gradlew.bat assembleDebug

# APK will be in: app\build\outputs\apk\debug\app-debug.apk
```

### Step 2: Install APK
```bash
adb install app\build\outputs\apk\debug\app-debug.apk
```

### Step 3: Run App
```bash
adb shell am start -n com.flam.edgedetection/.MainActivity
```

### Step 4: Capture Logcat
```bash
# Clear old logs
adb logcat -c

# Capture logs with filters
adb logcat -s MainActivity:D FrameProcessor:D FrameSender:D OpenCVProcessing:D *:S

# Or capture all and filter later
adb logcat > logcat_output.txt
```

### Step 5: Stop Logcat
Press `Ctrl+C` to stop capturing

## What to Look For in Logs

### Success Indicators:
```
MainActivity: Starting camera...
MainActivity: Camera provider obtained
MainActivity: Camera bound successfully!
FrameProcessor: Frame received: 640x480, format: 35
FrameProcessor: Processing frame: 640x480
OpenCVProcessing: Processing frame: 640x480, processing: 1
FrameSender: ✓ Frame sent successfully: 640x480, FPS: 25
```

### Error Indicators:
```
MainActivity: Camera permission denied
MainActivity: Back camera not available!
MainActivity: Camera initialization failed: ...
FrameProcessor: Error processing frame: ...
FrameSender: ✗ Connection failed - Is server running?
OpenCVProcessing: ERROR: ...
```

## Quick Test Script

Save this as `test_app.ps1`:

```powershell
# Clear logcat
adb logcat -c

# Start app
adb shell am start -n com.flam.edgedetection/.MainActivity

# Wait a moment
Start-Sleep -Seconds 2

# Capture logs for 10 seconds
adb logcat -s MainActivity:D FrameProcessor:D FrameSender:D OpenCVProcessing:D *:S > logcat_output.txt

# Wait
Start-Sleep -Seconds 10

# Stop (manually with Ctrl+C or kill adb)
```

Run with: `powershell -ExecutionPolicy Bypass -File test_app.ps1`

## Share Logcat Output

After capturing logs, share:
1. The full logcat output (or relevant sections)
2. Any error messages you see
3. What happens on the device (black screen? app crashes? etc.)

This will help identify exactly where the camera feed is failing!

