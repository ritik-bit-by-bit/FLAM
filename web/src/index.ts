import { EdgeDetectionViewer } from './viewer';

/**
 * Initialize the web viewer when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', () => {
    try {
        const viewer = new EdgeDetectionViewer('frameCanvas', 'frameStats');
        
        // Try to fetch real frame, fallback to demo
        viewer.fetchLatestFrame();
        
        // Poll for new frames from Android app every 100ms (10 FPS display)
        setInterval(() => {
            viewer.fetchLatestFrame();
        }, 100);
        
        console.log('FLAM Edge Detection Web Viewer initialized');
        console.log('Waiting for frames from Android app...');
    } catch (error) {
        console.error('Failed to initialize viewer:', error);
    }
});

