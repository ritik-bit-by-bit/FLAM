/**
 * FLAM Edge Detection Web Viewer
 * Displays processed frames from Android app
 */

export interface FrameStats {
    fps: number;
    resolution: {
        width: number;
        height: number;
    };
    processingTime: number;
}

export class EdgeDetectionViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private statsElement: HTMLElement;
    private imageElement: HTMLImageElement;
    
    private currentStats: FrameStats = {
        fps: 0,
        resolution: { width: 0, height: 0 },
        processingTime: 0
    };
    
    constructor(canvasId: string, statsId: string) {
        const canvas = document.getElementById(canvasId) as HTMLCanvasElement;
        const statsElement = document.getElementById(statsId);
        
        if (!canvas) {
            throw new Error(`Canvas element with id "${canvasId}" not found`);
        }
        
        if (!statsElement) {
            throw new Error(`Stats element with id "${statsId}" not found`);
        }
        
        this.canvas = canvas;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
            throw new Error('Could not get 2D rendering context');
        }
        this.ctx = ctx;
        this.statsElement = statsElement;
        this.imageElement = new Image();
        
        this.setupImageLoader();
    }
    
    /**
     * Display a processed frame from base64 encoded image
     */
    public displayFrame(base64Image: string, stats?: Partial<FrameStats>): void {
        this.imageElement.onload = () => {
            // Update canvas size to match image
            this.canvas.width = this.imageElement.width;
            this.canvas.height = this.imageElement.height;
            
            // Draw image
            this.ctx.drawImage(this.imageElement, 0, 0);
            
            // Update stats
            if (stats) {
                this.updateStats({
                    ...this.currentStats,
                    ...stats,
                    resolution: stats.resolution || {
                        width: this.imageElement.width,
                        height: this.imageElement.height
                    }
                });
            } else {
                this.updateStats({
                    ...this.currentStats,
                    resolution: {
                        width: this.imageElement.width,
                        height: this.imageElement.height
                    }
                });
            }
        };
        
        this.imageElement.src = base64Image;
    }
    
    /**
     * Update frame statistics display
     */
    private updateStats(stats: FrameStats): void {
        this.currentStats = stats;
        
        const statsHTML = `
            <div class="stat-item">
                <span class="stat-label">FPS:</span>
                <span class="stat-value">${stats.fps.toFixed(1)}</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Resolution:</span>
                <span class="stat-value">${stats.resolution.width} × ${stats.resolution.height}</span>
            </div>
            <div class="stat-item">
                <span class="stat-label">Processing Time:</span>
                <span class="stat-value">${stats.processingTime.toFixed(2)} ms</span>
            </div>
        `;
        
        this.statsElement.innerHTML = statsHTML;
    }
    
    
    /**
     * Fetch latest frame from server (called by Android app)
     */
    public async fetchLatestFrame(): Promise<void> {
        // Don't overwrite user-uploaded images
        if (this.hasLocalImage) {
            return;
        }
        
        try {
            const response = await fetch('/api/frame');
            if (response.ok) {
                const frameData = await response.json();
                if (frameData.image && !frameData.error) {
                    console.log('Received real frame:', frameData.width + 'x' + frameData.height);
                    this.hasLocalImage = false; // Server frame, not local
                    this.displayFrame(frameData.image, {
                        fps: frameData.fps || 0,
                        resolution: frameData.resolution || { width: 0, height: 0 },
                        processingTime: frameData.processingTime || 0
                    });
                    return; // Successfully displayed real frame
                } else if (frameData.error) {
                    // No frame available - don't overwrite existing display
                    return;
                }
            } else {
                console.log('Server response not OK:', response.status);
            }
        } catch (error) {
            console.error('Error fetching frame:', error);
        }
    }

    /**
     * Simulate receiving frame data (for demo)
     */
    public simulateFrameUpdate(): void {
        // This would typically receive data via WebSocket or HTTP
        // For demo, we'll use a sample image
        const sampleImage = this.getSampleBase64Image();
        this.displayFrame(sampleImage, {
            fps: Math.random() * 10 + 20, // 20-30 FPS
            processingTime: Math.random() * 10 + 10 // 10-20ms
        });
    }
    
    /**
     * Get a sample base64 encoded image for demonstration
     */
    private getSampleBase64Image(): string {
        // Create a simple test pattern
        const width = 640;
        const height = 480;
        const tempCanvas = document.createElement('canvas');
        tempCanvas.width = width;
        tempCanvas.height = height;
        const tempCtx = tempCanvas.getContext('2d')!;
        
        // Draw a gradient pattern
        const gradient = tempCtx.createLinearGradient(0, 0, width, height);
        gradient.addColorStop(0, '#000000');
        gradient.addColorStop(1, '#FFFFFF');
        tempCtx.fillStyle = gradient;
        tempCtx.fillRect(0, 0, width, height);
        
        // Draw some shapes to simulate edge detection
        tempCtx.strokeStyle = '#00FF00';
        tempCtx.lineWidth = 2;
        tempCtx.beginPath();
        tempCtx.arc(width / 2, height / 2, 100, 0, Math.PI * 2);
        tempCtx.stroke();
        
        tempCtx.beginPath();
        tempCtx.rect(width / 4, height / 4, width / 2, height / 2);
        tempCtx.stroke();
        
        return tempCanvas.toDataURL('image/png');
    }
}

