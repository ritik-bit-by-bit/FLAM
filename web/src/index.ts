import { EdgeDetectionViewer } from './viewer';

/**
 * Initialize the web viewer when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', () => {
    try {
        const viewer = new EdgeDetectionViewer('frameCanvas', 'frameStats');
        
        // Show initial message
        console.log('FLAM Edge Detection Web Viewer initialized');
        console.log('Waiting for frames from Android app...');
        console.log('Make sure:');
        console.log('1. Android app is running and sending frames');
        console.log('2. Both devices are on same WiFi network');
        console.log('3. Server is running with: npm run serve');
        
        // Try to fetch real frame immediately
        viewer.fetchLatestFrame();
        
        // Poll for new frames from Android app every 100ms (10 FPS display)
        // But only if no local image is loaded
        let frameCount = 0;
        setInterval(() => {
            viewer.fetchLatestFrame();
            frameCount++;
            if (frameCount % 50 === 0) { // Log every 5 seconds
                console.log('Still waiting for frames from Android app...');
            }
        }, 100);
        
        // Add button to clear local image and resume server polling
        const clearButton = document.createElement('button');
        clearButton.textContent = 'Clear & Resume Server Feed';
        clearButton.style.cssText = 'position: fixed; bottom: 20px; right: 20px; padding: 10px; z-index: 1000; background: #ff4444; color: white; border: none; border-radius: 4px; cursor: pointer;';
        clearButton.onclick = () => {
            viewer.clearLocalImage();
            viewer.fetchLatestFrame();
            clearButton.style.display = 'none';
        };
        clearButton.style.display = 'none';
        document.body.appendChild(clearButton);
        
        // Show clear button when local image is loaded
        const originalDisplayFrame = viewer.displayFrame.bind(viewer);
        viewer.displayFrame = function(base64Image: string, stats?: Partial<FrameStats>) {
            originalDisplayFrame(base64Image, stats);
            // Check if this is a local image (not from server)
            if (base64Image.startsWith('data:image')) {
                clearButton.style.display = 'block';
            }
        };
        
    } catch (error) {
        console.error('Failed to initialize viewer:', error);
    }
});

