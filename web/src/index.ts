import { EdgeDetectionViewer } from './viewer';

/**
 * Initialize the web viewer when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', () => {
    try {
        const viewer = new EdgeDetectionViewer('frameCanvas', 'frameStats');
        
        // Display initial sample frame
        viewer.simulateFrameUpdate();
        
        // Simulate frame updates every 2 seconds (for demo)
        setInterval(() => {
            viewer.simulateFrameUpdate();
        }, 2000);
        
        console.log('FLAM Edge Detection Web Viewer initialized');
    } catch (error) {
        console.error('Failed to initialize viewer:', error);
    }
});

