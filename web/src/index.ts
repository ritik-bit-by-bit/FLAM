import { EdgeDetectionViewer } from './viewer.js';

let viewer: EdgeDetectionViewer | null = null;

document.addEventListener('DOMContentLoaded', async () => {
    console.log('FLAM Edge Detection Web Viewer initialized');
    
    try {
        viewer = new EdgeDetectionViewer('frameCanvas', 'frameStats');
        
        // Initialize camera
        console.log('Initializing camera...');
        await viewer.initializeCamera();
        console.log('✅ Camera initialized successfully!');
        
        // Add toggle button functionality
        const toggleButton = document.getElementById('toggleButton');
        if (toggleButton) {
            toggleButton.addEventListener('click', () => {
                if (viewer) {
                    viewer.toggleProcessing();
                }
            });
        }
        
        // Cleanup on page unload
        window.addEventListener('beforeunload', () => {
            if (viewer) {
                viewer.stop();
            }
        });
        
    } catch (error) {
        console.error('Failed to initialize viewer:', error);
        const canvas = document.getElementById('frameCanvas') as HTMLCanvasElement;
        if (canvas) {
            const ctx = canvas.getContext('2d');
            if (ctx) {
                ctx.fillStyle = '#000';
                ctx.fillRect(0, 0, canvas.width || 640, canvas.height || 480);
                ctx.fillStyle = '#fff';
                ctx.font = '20px Arial';
                ctx.textAlign = 'center';
                ctx.fillText('Camera access failed. Please check permissions.', 
                    (canvas.width || 640) / 2, (canvas.height || 480) / 2);
            }
        }
    }
});
