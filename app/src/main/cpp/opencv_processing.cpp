#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc.hpp>

#define LOG_TAG "OpenCVProcessing"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace cv;

extern "C" {

JNIEXPORT void JNICALL
Java_com_flam_edgedetection_FrameProcessor_processFrame(
        JNIEnv *env,
        jobject thiz,
        jbyteArray yuvData,
        jint width,
        jint height,
        jintArray outputPixels,
        jboolean enableProcessing) {
    
    // Get input data
    jbyte* yuvBytes = env->GetByteArrayElements(yuvData, nullptr);
    jint* outputInts = env->GetIntArrayElements(outputPixels, nullptr);
    
    // Convert YUV to Mat
    // YUV_420_888 format: Y plane + interleaved UV plane
    // For NV21-like conversion, we create a Mat with proper dimensions
    int yuvSize = width * height * 3 / 2;  // YUV420 format size
    Mat yuvMat(height + height/2, width, CV_8UC1, (unsigned char*)yuvBytes);
    Mat rgbMat;
    
    // Convert YUV to RGB (assuming NV21 format from CameraX)
    try {
        cvtColor(yuvMat, rgbMat, COLOR_YUV2RGB_NV21);
    } catch (cv::Exception& e) {
        LOGE("OpenCV conversion error: %s", e.what());
        env->ReleaseByteArrayElements(yuvData, yuvBytes, JNI_ABORT);
        env->ReleaseIntArrayElements(outputPixels, outputInts, JNI_ABORT);
        return;
    }
    
    Mat processedMat;
    
    if (enableProcessing) {
        // Convert to grayscale
        Mat grayMat;
        cvtColor(rgbMat, grayMat, COLOR_RGB2GRAY);
        
        // Apply Canny edge detection
        Mat edges;
        Canny(grayMat, edges, 50, 150);
        
        // Convert back to RGB for display
        cvtColor(edges, processedMat, COLOR_GRAY2RGB);
    } else {
        // Return original frame
        processedMat = rgbMat.clone();
    }
    
    // Convert Mat to int array (ARGB format)
    // Ensure processedMat has correct size
    if (processedMat.rows != height || processedMat.cols != width) {
        LOGE("Size mismatch: Mat(%d,%d) vs expected(%d,%d)", 
             processedMat.cols, processedMat.rows, width, height);
        env->ReleaseByteArrayElements(yuvData, yuvBytes, JNI_ABORT);
        env->ReleaseIntArrayElements(outputPixels, outputInts, JNI_ABORT);
        return;
    }
    
    int* pixels = outputInts;
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            Vec3b pixel = processedMat.at<Vec3b>(y, x);
            // Convert RGB to ARGB (Android format: A R G B)
            // OpenCV uses BGR, so pixel[0]=B, pixel[1]=G, pixel[2]=R
            int argb = (0xFF << 24) | (pixel[2] << 16) | (pixel[1] << 8) | pixel[0];
            pixels[y * width + x] = argb;
        }
    }
    
    // Release resources
    env->ReleaseByteArrayElements(yuvData, yuvBytes, JNI_ABORT);
    env->ReleaseIntArrayElements(outputPixels, outputInts, 0);
}

} // extern "C"

