export interface FrameStats {
    fps: number;
    resolution: { width: number; height: number };
    processingTime: number;
}

export class EdgeDetectionViewer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private statsContainer: HTMLElement;
    private videoElement: HTMLVideoElement;
    private stream: MediaStream | null = null;
    private animationFrameId: number | null = null;
    private frameCount = 0;
    private lastFpsTime = Date.now();
    private currentFps = 0;
    private processingTime = 0;
    private isProcessing = true; // Edge detection enabled by default

    constructor(canvasId: string, statsId: string) {
        const canvas = document.getElementById(canvasId) as HTMLCanvasElement;
        const statsContainer = document.getElementById(statsId);
        
        if (!canvas) {
            throw new Error(`Canvas element with id "${canvasId}" not found`);
        }
        if (!statsContainer) {
            throw new Error(`Stats container with id "${statsId}" not found`);
        }

        this.canvas = canvas;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
            throw new Error('Could not get 2D rendering context');
        }
        this.ctx = ctx;
        this.statsContainer = statsContainer;

        // Create video element for webcam
        this.videoElement = document.createElement('video');
        this.videoElement.autoplay = true;
        this.videoElement.playsInline = true;
    }

    async initializeCamera(): Promise<void> {
        try {
            console.log('🎥 Requesting camera access...');
            
            // Request camera access
            this.stream = await navigator.mediaDevices.getUserMedia({
                video: {
                    width: { ideal: 640 },
                    height: { ideal: 480 },
                    facingMode: 'user' // Use front camera, change to 'environment' for back camera
                }
            });

            this.videoElement.srcObject = this.stream;
            
            // Wait for video to be ready
            await new Promise((resolve) => {
                this.videoElement.onloadedmetadata = () => {
                    this.videoElement.play();
                    this.canvas.width = this.videoElement.videoWidth;
                    this.canvas.height = this.videoElement.videoHeight;
                    console.log(`✅ Camera initialized: ${this.canvas.width}x${this.canvas.height}`);
                    resolve(null);
                };
            });

            // Start processing frames
            this.startFrameProcessing();
            
        } catch (error: any) {
            console.error('❌ Error accessing camera:', error);
            if (error.name === 'NotAllowedError') {
                alert('Camera access denied. Please allow camera access and refresh the page.');
            } else if (error.name === 'NotFoundError') {
                alert('No camera found. Please connect a camera and refresh the page.');
            } else {
                alert(`Camera error: ${error.message}`);
            }
            throw error;
        }
    }

    private startFrameProcessing(): void {
        const processFrame = () => {
            if (this.videoElement.readyState === this.videoElement.HAVE_ENOUGH_DATA) {
                const startTime = performance.now();
                
                // Always draw raw video frame to canvas (no filter)
                this.ctx.drawImage(this.videoElement, 0, 0, this.canvas.width, this.canvas.height);
                
                // Run edge detection in background for stats (but don't apply to display)
                if (this.isProcessing) {
                    this.calculateEdgeDetection();
                }
                
                // Calculate processing time
                this.processingTime = performance.now() - startTime;
                
                // Update FPS
                this.updateFps();
                
                // Update stats
                this.updateStats();
            }
            
            this.animationFrameId = requestAnimationFrame(processFrame);
        };
        
        processFrame();
    }

    /**
     * Calculate edge detection in background (for stats) but don't apply to display
     * The raw camera feed is always shown to the user
     */
    private calculateEdgeDetection(): void {
        // Get image data from canvas (which has the raw video frame)
        const imageData = this.ctx.getImageData(0, 0, this.canvas.width, this.canvas.height);
        const data = imageData.data;
        
        // Convert to grayscale and apply Sobel edge detection
        const width = this.canvas.width;
        const height = this.canvas.height;
        const grayscale = new Uint8Array(width * height);
        
        // Convert to grayscale
        for (let i = 0; i < data.length; i += 4) {
            const r = data[i];
            const g = data[i + 1];
            const b = data[i + 2];
            grayscale[i / 4] = Math.round(0.299 * r + 0.587 * g + 0.114 * b);
        }
        
        // Apply Sobel edge detection (just for calculation, not display)
        const sobelX = [[-1, 0, 1], [-2, 0, 2], [-1, 0, 1]];
        const sobelY = [[-1, -2, -1], [0, 0, 0], [1, 2, 1]];
        
        // Calculate edge magnitudes (for stats/metrics)
        let maxMagnitude = 0;
        let edgePixelCount = 0;
        const threshold = 50; // Threshold for edge detection
        
        for (let y = 1; y < height - 1; y++) {
            for (let x = 1; x < width - 1; x++) {
                let gx = 0, gy = 0;
                
                for (let ky = -1; ky <= 1; ky++) {
                    for (let kx = -1; kx <= 1; kx++) {
                        const idx = (y + ky) * width + (x + kx);
                        const gray = grayscale[idx];
                        gx += gray * sobelX[ky + 1][kx + 1];
                        gy += gray * sobelY[ky + 1][kx + 1];
                    }
                }
                
                const magnitude = Math.sqrt(gx * gx + gy * gy);
                if (magnitude > maxMagnitude) {
                    maxMagnitude = magnitude;
                }
                if (magnitude > threshold) {
                    edgePixelCount++;
                }
            }
        }
        
        // Note: We don't draw the edges - raw feed stays visible
        // Edge detection is calculated for processing time measurement only
    }

    private updateFps(): void {
        this.frameCount++;
        const now = Date.now();
        if (now - this.lastFpsTime >= 1000) {
            this.currentFps = this.frameCount;
            this.frameCount = 0;
            this.lastFpsTime = now;
        }
    }

    private updateStats(): void {
        const stats: FrameStats = {
            fps: this.currentFps,
            resolution: {
                width: this.canvas.width,
                height: this.canvas.height
            },
            processingTime: this.processingTime
        };

        this.statsContainer.innerHTML = `
            <div><strong>FPS:</strong> ${stats.fps}</div>
            <div><strong>Resolution:</strong> ${stats.resolution.width}x${stats.resolution.height}</div>
            <div><strong>Processing Time:</strong> ${stats.processingTime.toFixed(2)} ms</div>
            <div><strong>Display:</strong> Raw Camera Feed</div>
            <div><strong>Background:</strong> ${this.isProcessing ? 'Edge Detection Active' : 'Edge Detection Disabled'}</div>
        `;
    }

    toggleProcessing(): void {
        this.isProcessing = !this.isProcessing;
        console.log(`Edge detection calculation ${this.isProcessing ? 'enabled' : 'disabled'} (background processing)`);
        // Note: Display always shows raw feed regardless of this setting
    }

    stop(): void {
        if (this.animationFrameId !== null) {
            cancelAnimationFrame(this.animationFrameId);
            this.animationFrameId = null;
        }
        
        if (this.stream) {
            this.stream.getTracks().forEach(track => track.stop());
            this.stream = null;
        }
        
        if (this.videoElement.srcObject) {
            this.videoElement.srcObject = null;
        }
    }
}
