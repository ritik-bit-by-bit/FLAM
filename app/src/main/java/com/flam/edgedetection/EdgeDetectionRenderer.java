package com.flam.edgedetection;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EdgeDetectionRenderer implements GLSurfaceView.Renderer {
    private static final String VERTEX_SHADER_CODE =
            "attribute vec4 vPosition;" +
            "attribute vec2 vTexCoord;" +
            "varying vec2 texCoord;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "  texCoord = vTexCoord;" +
            "}";
    
    private static final String FRAGMENT_SHADER_CODE =
            "precision mediump float;" +
            "varying vec2 texCoord;" +
            "uniform sampler2D texture;" +
            "uniform int effectMode;" +  // 0=normal, 1=grayscale, 2=invert
            "void main() {" +
            "  vec4 color = texture2D(texture, texCoord);" +
            "  if (effectMode == 1) {" +  // Grayscale
            "    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));" +
            "    gl_FragColor = vec4(gray, gray, gray, color.a);" +
            "  } else if (effectMode == 2) {" +  // Invert
            "    gl_FragColor = vec4(1.0 - color.rgb, color.a);" +
            "  } else {" +  // Normal
            "    gl_FragColor = color;" +
            "  }" +
            "}";
    
    private int effectMode = 0; // 0=normal, 1=grayscale, 2=invert
    
    private Context context;
    private GLSurfaceView glSurfaceView;
    private int program;
    private int textureHandle;
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    
    private int[] pixels;
    private int frameWidth = 0;
    private int frameHeight = 0;
    private boolean frameUpdated = false;
    private final Object frameLock = new Object();
    private int drawCallCount = 0; // Track draw calls for reduced logging
    
    // Quad vertices (full screen)
    private static final float[] QUAD_VERTICES = {
            -1.0f, -1.0f, 0.0f,
             1.0f, -1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f,
             1.0f,  1.0f, 0.0f
    };
    
    // Texture coordinates
    private static final float[] TEX_COORDS = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };
    
    public EdgeDetectionRenderer(Context context) {
        this.context = context;
        
        // Setup vertex buffer
        ByteBuffer bb = ByteBuffer.allocateDirect(QUAD_VERTICES.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(QUAD_VERTICES);
        vertexBuffer.position(0);
        
        // Setup texture coordinate buffer
        ByteBuffer tb = ByteBuffer.allocateDirect(TEX_COORDS.length * 4);
        tb.order(ByteOrder.nativeOrder());
        texCoordBuffer = tb.asFloatBuffer();
        texCoordBuffer.put(TEX_COORDS);
        texCoordBuffer.position(0);
    }
    
    public void setGLSurfaceView(GLSurfaceView glSurfaceView) {
        this.glSurfaceView = glSurfaceView;
    }
    
    public void setEffectMode(int mode) {
        // 0=normal, 1=grayscale, 2=invert
        if (mode >= 0 && mode <= 2) {
            this.effectMode = mode;
            if (glSurfaceView != null) {
                glSurfaceView.requestRender();
            }
        }
    }
    
    public void cycleEffect() {
        effectMode = (effectMode + 1) % 3; // Cycle: 0->1->2->0
        if (glSurfaceView != null) {
            glSurfaceView.requestRender();
        }
    }
    
    public int getEffectMode() {
        return effectMode;
    }
    
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        android.util.Log.d("EdgeDetectionRenderer", "=== 🎬 OpenGL Surface Created ===");
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        
        // Compile shaders
        android.util.Log.d("EdgeDetectionRenderer", "Compiling shaders...");
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE);
        
        // Create program
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        
        // Check for linking errors
        android.util.Log.d("EdgeDetectionRenderer", "Linking shader program...");
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            String error = GLES20.glGetProgramInfoLog(program);
            android.util.Log.e("EdgeDetectionRenderer", "Shader program linking failed: " + error);
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        
        // Generate texture
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        textureHandle = textures[0];
        android.util.Log.d("EdgeDetectionRenderer", "✅ Texture created: " + textureHandle);
        android.util.Log.d("EdgeDetectionRenderer", "✅ OpenGL setup complete!");
        
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, 
                               GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, 
                               GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, 
                               GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, 
                               GLES20.GL_CLAMP_TO_EDGE);
        
        // Initialize texture with a placeholder (black image) so something is always drawn
        int[] placeholder = new int[640 * 480]; // Default size
        for (int i = 0; i < placeholder.length; i++) {
            placeholder[i] = 0x000000FF; // Black RGBA (R=0, G=0, B=0, A=255)
        }
        IntBuffer placeholderBuffer = IntBuffer.wrap(placeholder);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 
                           640, 480, 0, 
                           GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, placeholderBuffer);
        android.util.Log.d("EdgeDetectionRenderer", "✅ Texture initialized with placeholder");
    }
    
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
    
    @Override
    public void onDrawFrame(GL10 gl) {
        // Reduce logging frequency to avoid spam (log every 30 frames)
        if (drawCallCount % 30 == 0) {
            android.util.Log.d("EdgeDetectionRenderer", "=== 🖼️ onDrawFrame CALLED (frame " + drawCallCount + ") ===");
        }
        drawCallCount++;
        
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        
        synchronized (frameLock) {
            // Always try to update texture if we have new frame data
            if (frameUpdated && pixels != null && frameWidth > 0 && frameHeight > 0) {
                if (drawCallCount % 30 == 0) {
                    android.util.Log.d("EdgeDetectionRenderer", "✅ Updating texture: " + frameWidth + "x" + frameHeight + ", pixels: " + pixels.length);
                }
                
                try {
                    // Update texture with new frame
                    IntBuffer pixelBuffer = IntBuffer.wrap(pixels);
                    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
                    GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 
                                       frameWidth, frameHeight, 0, 
                                       GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
                    
                    // Check for OpenGL errors
                    int error = GLES20.glGetError();
                    if (error != GLES20.GL_NO_ERROR) {
                        android.util.Log.e("EdgeDetectionRenderer", "❌ OpenGL error after glTexImage2D: " + error);
                    } else if (drawCallCount % 30 == 0) {
                        android.util.Log.d("EdgeDetectionRenderer", "✅ Texture updated successfully");
                    }
                    
                    frameUpdated = false;
                } catch (Exception e) {
                    android.util.Log.e("EdgeDetectionRenderer", "❌ Error updating texture: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (drawCallCount % 30 == 0) {
                android.util.Log.w("EdgeDetectionRenderer", "⚠️ No frame data - frameUpdated: " + frameUpdated + 
                                   ", pixels: " + (pixels != null ? "OK" : "NULL") + 
                                   ", size: " + frameWidth + "x" + frameHeight);
            }
        }
        
        // Always bind texture before drawing (even if no new frame)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
        
        // Draw quad with texture (always draw, even if no new frame)
        if (program == 0) {
            android.util.Log.e("EdgeDetectionRenderer", "❌ Shader program is 0!");
            return;
        }
        
        GLES20.glUseProgram(program);
        
        int positionHandle = GLES20.glGetAttribLocation(program, "vPosition");
        if (positionHandle < 0) {
            android.util.Log.e("EdgeDetectionRenderer", "❌ vPosition attribute not found!");
            return;
        }
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 
                                     12, vertexBuffer);
        
        int texCoordHandle = GLES20.glGetAttribLocation(program, "vTexCoord");
        if (texCoordHandle < 0) {
            android.util.Log.e("EdgeDetectionRenderer", "❌ vTexCoord attribute not found!");
            GLES20.glDisableVertexAttribArray(positionHandle);
            return;
        }
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 
                                     8, texCoordBuffer);
        
        int textureHandleUniform = GLES20.glGetUniformLocation(program, "texture");
        if (textureHandleUniform < 0) {
            android.util.Log.e("EdgeDetectionRenderer", "❌ texture uniform not found!");
        } else {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);
            GLES20.glUniform1i(textureHandleUniform, 0);
        }
        
        // Set effect mode uniform
        int effectModeUniform = GLES20.glGetUniformLocation(program, "effectMode");
        if (effectModeUniform >= 0) {
            GLES20.glUniform1i(effectModeUniform, effectMode);
        }
        
        android.util.Log.d("EdgeDetectionRenderer", "🎨 Drawing quad with texture " + textureHandle);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        android.util.Log.d("EdgeDetectionRenderer", "✅ Draw complete!");
        
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }
    
    public void updateFrame(int[] pixels, int width, int height) {
        android.util.Log.d("EdgeDetectionRenderer", "=== 🎨 UPDATE FRAME ===");
        android.util.Log.d("EdgeDetectionRenderer", "Pixels: " + (pixels != null ? pixels.length : 0));
        android.util.Log.d("EdgeDetectionRenderer", "Size: " + width + "x" + height);
        
        synchronized (frameLock) {
            this.pixels = pixels;
            this.frameWidth = width;
            this.frameHeight = height;
            this.frameUpdated = true;
            android.util.Log.d("EdgeDetectionRenderer", "Frame marked as updated");
        }
        
        // Request render when frame is updated
        if (glSurfaceView != null) {
            android.util.Log.d("EdgeDetectionRenderer", "🔄 Requesting render...");
            glSurfaceView.requestRender();
            android.util.Log.d("EdgeDetectionRenderer", "✅ Render requested");
        } else {
            android.util.Log.e("EdgeDetectionRenderer", "❌ GLSurfaceView is NULL!");
        }
    }
    
    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
    
    public void release() {
        if (textureHandle != 0) {
            int[] textures = {textureHandle};
            GLES20.glDeleteTextures(1, textures, 0);
            textureHandle = 0;
        }
    }
}

