import { EdgeDetectionViewer, FrameStats } from './viewer';

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
        let frameCount = 0;
        setInterval(() => {
            viewer.fetchLatestFrame();
            frameCount++;
            if (frameCount % 50 === 0) { // Log every 5 seconds
                console.log('Still waiting for frames from Android app...');
            }
        }, 100);
        
    } catch (error) {
        console.error('Failed to initialize viewer:', error);
    }
});

