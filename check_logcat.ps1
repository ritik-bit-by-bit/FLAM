# Quick Logcat Check Script
# Run this after starting the app to see what's happening

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "FLAM Camera Feed Diagnostic Check" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Make sure:" -ForegroundColor Yellow
Write-Host "1. App is running on device/emulator" -ForegroundColor White
Write-Host "2. Camera permission is granted" -ForegroundColor White
Write-Host "3. Check Android Studio Logcat (filter: FrameProcessor|OpenCVProcessing|EdgeDetectionRenderer)" -ForegroundColor White
Write-Host ""

Write-Host "What to look for in Logcat:" -ForegroundColor Yellow
Write-Host ""

Write-Host "STEP 1: Native Library Loading" -ForegroundColor Green
Write-Host "  Look for: 'Native library loaded successfully'" -ForegroundColor White
Write-Host "  If you see 'Failed to load native library' -> OpenCV not configured" -ForegroundColor Red
Write-Host ""

Write-Host "STEP 2: Camera Initialization" -ForegroundColor Green
Write-Host "  Look for: 'Camera bound successfully!'" -ForegroundColor White
Write-Host "  If missing -> Camera not starting" -ForegroundColor Red
Write-Host ""

Write-Host "STEP 3: Frame Reception" -ForegroundColor Green
Write-Host "  Look for: 'FRAME RECEIVED'" -ForegroundColor White
Write-Host "  If missing -> Camera not sending frames" -ForegroundColor Red
Write-Host ""

Write-Host "STEP 4: OpenCV Processing" -ForegroundColor Green
Write-Host "  Look for: 'OpenCV Native Function Called'" -ForegroundColor White
Write-Host "  If missing -> OpenCV not working or not being called" -ForegroundColor Red
Write-Host ""

Write-Host "STEP 5: Rendering" -ForegroundColor Green
Write-Host "  Look for: 'UPDATE FRAME' and 'onDrawFrame CALLED'" -ForegroundColor White
Write-Host "  If missing -> Renderer not receiving/displaying frames" -ForegroundColor Red
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Copy the Logcat output and share it!" -ForegroundColor Yellow
Write-Host "Filter by: FrameProcessor|OpenCVProcessing|EdgeDetectionRenderer|MainActivity" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

