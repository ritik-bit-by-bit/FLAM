# Fix ERR_CONNECTION_REFUSED Error

## Server Status: ✅ RUNNING

The server is running on port 8080. The error might be a browser cache issue.

## Quick Fixes

### Fix 1: Hard Refresh Browser
1. Press **Ctrl + F5** (hard refresh)
2. OR **Ctrl + Shift + R**
3. This clears cache and reloads the page

### Fix 2: Clear Browser Cache
1. Press **F12** (open DevTools)
2. Right-click the refresh button
3. Select **"Empty Cache and Hard Reload"**

### Fix 3: Restart Server (if needed)
1. Stop current server: Press **Ctrl+C** in the terminal running the server
2. Start again:
   ```bash
   cd web
   npm run serve
   ```
3. Wait for: `Server running at http://localhost:8080/`
4. Refresh browser

### Fix 4: Check Server Terminal
Look at the server terminal - you should see:
```
Server running at:
  http://localhost:8080/
  http://192.168.1.4:8080/
```

If you see errors, the server might have crashed.

## Verify Server is Working

Test in browser:
1. Open: `http://localhost:8080`
2. Should load the web viewer (no errors)
3. Check browser console (F12) - should NOT see `ERR_CONNECTION_REFUSED`

## Next: Get Frames from Android

Once server is working:
1. ✅ Server running
2. ✅ Web viewer loads without errors
3. ⏳ **Now need Android app to send frames**

**Check Android Logcat** (filter: `FrameSender`):
- Should see: `✅ Frame sent successfully to server`
- If you see: `❌ Connection failed` → Check IP address

**Check Server Terminal**:
- Should see: `📥 POST /api/frame from 192.168.x.x`
- If missing → Android not connecting

## Current Status

✅ Server: Running and responding
✅ Web Viewer: Should work after hard refresh
⏳ Android: Need to check if sending frames

**Next step**: Hard refresh browser (Ctrl+F5), then check if Android app is sending frames!

