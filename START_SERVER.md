# How to Start the Web Server

## The Error You're Seeing

`ERR_CONNECTION_REFUSED` means **the web server is not running**.

## Quick Fix

### Option 1: Start Server in Terminal

1. Open a **new terminal/command prompt**
2. Navigate to web folder:
   ```bash
   cd web
   ```
3. Start server:
   ```bash
   npm run serve
   ```
4. You should see:
   ```
   Server running at:
     http://localhost:8080/
     http://192.168.1.4:8080/ (or your IP)
   ```

5. **Keep this terminal open** - server must keep running!

6. Refresh your browser: `http://localhost:8080`

### Option 2: Start Server in Background (Already Done)

I've started the server in the background. Check if it's running:

**Windows:**
```powershell
netstat -an | findstr :8080
```

If you see `LISTENING` on port 8080, server is running!

## Verify Server is Running

1. Open browser: `http://localhost:8080`
2. You should see the web viewer (no connection errors)
3. Check browser console - should NOT see `ERR_CONNECTION_REFUSED`

## If Server Still Not Working

1. **Check if port 8080 is in use:**
   ```powershell
   netstat -an | findstr :8080
   ```

2. **Try different port:**
   - Edit `web/server.js` line 6: Change `const PORT = 8080;` to `8081`
   - Update `FrameSender.java` line 18: Change port to `8081`
   - Restart server

3. **Check Node.js is installed:**
   ```bash
   node --version
   npm --version
   ```

## Next Steps After Server Starts

Once server is running:
1. ✅ Web viewer should load without errors
2. ✅ Run Android app
3. ✅ Check server terminal for "POST /api/frame" messages
4. ✅ Check Android Logcat for "Frame sent successfully"

The server MUST be running for the web viewer to receive frames!

