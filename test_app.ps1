# Test Android App and Capture Logcat
# Make sure device is connected and USB debugging is enabled

Write-Host "========================================"
Write-Host "FLAM Edge Detection - Logcat Capture"
Write-Host "========================================"
Write-Host ""

# Check if adb is available
$adbCheck = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adbCheck) {
    Write-Host "ERROR: adb not found in PATH"
    Write-Host "Please install Android SDK platform-tools"
    exit 1
}

Write-Host "1. Clearing old logcat..."
adb logcat -c

Write-Host "2. Starting app..."
adb shell am start -n com.flam.edgedetection/.MainActivity

Write-Host "3. Waiting 3 seconds for app to start..."
Start-Sleep -Seconds 3

Write-Host "4. Capturing logcat for 15 seconds..."
Write-Host "   (Filtering: MainActivity, FrameProcessor, FrameSender, OpenCVProcessing)"
Write-Host ""

$logFile = "logcat_output_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"

# Start logcat capture in background
$job = Start-Job -ScriptBlock {
    param($file)
    adb logcat -s MainActivity:D FrameProcessor:D FrameSender:D OpenCVProcessing:D *:S | Out-File -FilePath $file -Encoding utf8
} -ArgumentList $logFile

Write-Host "   Capturing... (15 seconds)"
Start-Sleep -Seconds 15

# Stop the job
Stop-Job $job
Remove-Job $job

Write-Host ""
Write-Host "5. Logcat captured to: $logFile"
Write-Host ""
Write-Host "========================================"
Write-Host "Log Summary:"
Write-Host "========================================"

# Show summary
if (Test-Path $logFile) {
    $content = Get-Content $logFile
    $errorCount = ($content | Select-String -Pattern "ERROR|Error|Failed|failed").Count
    $successCount = ($content | Select-String -Pattern "successfully|bound successfully|Frame received").Count
    
    Write-Host "Total log lines: $($content.Count)"
    Write-Host "Success indicators: $successCount"
    Write-Host "Error indicators: $errorCount"
    Write-Host ""
    
    if ($errorCount -gt 0) {
        Write-Host "ERRORS FOUND:"
        $content | Select-String -Pattern "ERROR|Error|Failed|failed" | Select-Object -First 10
    }
    
    if ($successCount -gt 0) {
        Write-Host "SUCCESS INDICATORS:"
        $content | Select-String -Pattern "successfully|bound successfully|Frame received" | Select-Object -First 10
    }
}

Write-Host ""
Write-Host "Full log saved to: $logFile"
Write-Host "Please share this file or its contents for debugging"

