const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 8080;

const MIME_TYPES = {
    '.html': 'text/html',
    '.js': 'text/javascript',
    '.css': 'text/css',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon'
};

// Store latest frame data
let latestFrame = null;

const server = http.createServer((req, res) => {
    const parsedUrl = url.parse(req.url, true);
    let pathname = parsedUrl.pathname;

    // API endpoint to receive frames from Android
    if (pathname === '/api/frame' && req.method === 'POST') {
        let body = '';
        req.on('data', chunk => {
            body += chunk.toString();
        });
        req.on('end', () => {
            try {
                const frameData = JSON.parse(body);
                latestFrame = frameData;
                const timestamp = new Date().toLocaleTimeString();
                console.log(`[${timestamp}] Received frame: ${frameData.width}x${frameData.height}, FPS: ${frameData.fps}`);
                res.writeHead(200, { 
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin': '*'
                });
                res.end(JSON.stringify({ success: true }));
            } catch (e) {
                console.error('Error parsing frame data:', e.message);
                res.writeHead(400, { 
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin': '*'
                });
                res.end(JSON.stringify({ error: 'Invalid JSON: ' + e.message }));
            }
        });
        return;
    }

    // API endpoint to get latest frame
    if (pathname === '/api/frame' && req.method === 'GET') {
        res.writeHead(200, { 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        });
        res.end(JSON.stringify(latestFrame || { error: 'No frame available' }));
        return;
    }

    // Serve static files
    if (pathname === '/') {
        pathname = '/index.html';
    }

    const filePath = path.join(__dirname, pathname);
    const ext = path.extname(filePath).toLowerCase();
    const contentType = MIME_TYPES[ext] || 'application/octet-stream';

    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                res.writeHead(404);
                res.end('File not found');
            } else {
                res.writeHead(500);
                res.end('Server error: ' + err.code);
            }
        } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content, 'utf-8');
        }
    });
});

server.listen(PORT, '0.0.0.0', () => {
    console.log(`\n========================================`);
    console.log(`Server running at:`);
    console.log(`  http://localhost:${PORT}/`);
    console.log(`  http://192.168.1.4:${PORT}/ (or your IP)`);
    console.log(`\nAPI endpoints:`);
    console.log(`  POST /api/frame - Receive frame from Android`);
    console.log(`  GET  /api/frame - Get latest frame`);
    console.log(`\nWaiting for frames from Android app...`);
    console.log(`========================================\n`);
});

