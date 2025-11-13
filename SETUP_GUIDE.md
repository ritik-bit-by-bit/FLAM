# Quick Setup Guide

## Prerequisites Checklist

- [ ] Android Studio (Arctic Fox or later)
- [ ] Android NDK (r23b or later) - Install via SDK Manager
- [ ] OpenCV Android SDK 4.8.0+ - Download from [opencv.org](https://opencv.org/releases/)
- [ ] Node.js v18+ (for web viewer)
- [ ] Physical Android device or emulator (API 24+)

## Step-by-Step Setup

### 1. Clone and Open Project

```bash
git clone https://github.com/ritik-bit-by-bit/FLAM.git
cd FLAM
```

Open in Android Studio: `File → Open → Select FLAM directory`

### 2. Install Android NDK

In Android Studio:
1. `Tools → SDK Manager`
2. Go to `SDK Tools` tab
3. Check `NDK (Side by side)` and `CMake`
4. Click `Apply` and wait for installation

### 3. Setup OpenCV

1. Download OpenCV Android SDK from https://opencv.org/releases/
2. Extract the downloaded archive
3. Copy the `opencv` folder to: `app/src/main/jniLibs/`
4. Update `app/src/main/cpp/CMakeLists.txt` line 6:
   ```cmake
   set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/../../../opencv/sdk/native/jni)
   ```
   Adjust path if your OpenCV location differs.

### 4. Sync Gradle

In Android Studio:
- Click `Sync Now` when prompted, or
- `File → Sync Project with Gradle Files`

### 5. Build and Run

1. Connect Android device (enable USB debugging) or start emulator
2. Click `Run` button (green play icon) or press `Shift+F10`
3. Grant camera permission when prompted

### 6. Setup Web Viewer

```bash
cd web
npm install
npm run build
npm run serve
```

Open browser: `http://localhost:8080`

## Troubleshooting

### NDK Not Found
- Ensure NDK is installed via SDK Manager
- Check `local.properties` has `ndk.dir` set correctly

### OpenCV Not Found
- Verify OpenCV folder is in `app/src/main/jniLibs/opencv/`
- Check CMakeLists.txt OpenCV_DIR path
- Ensure OpenCV version is 4.8.0 or later

### Build Errors
- Clean project: `Build → Clean Project`
- Rebuild: `Build → Rebuild Project`
- Invalidate caches: `File → Invalidate Caches / Restart`

### Camera Permission Denied
- Go to device Settings → Apps → FLAM Edge Detection → Permissions
- Enable Camera permission

## Verification

After setup, you should see:
- ✅ Android app builds without errors
- ✅ App installs on device/emulator
- ✅ Camera feed displays
- ✅ Edge detection toggle works
- ✅ FPS and resolution display correctly
- ✅ Web viewer loads in browser

