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
    Mat yuvMat(height + height/2, width, CV_8UC1, (unsigned char*)yuvBytes);
    Mat rgbMat;
    cvtColor(yuvMat, rgbMat, COLOR_YUV2RGB_NV21);
    
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
    int* pixels = outputInts;
    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            Vec3b pixel = processedMat.at<Vec3b>(y, x);
            // Convert RGB to ARGB (Android format)
            int argb = (0xFF << 24) | (pixel[0] << 16) | (pixel[1] << 8) | pixel[2];
            pixels[y * width + x] = argb;
        }
    }
    
    // Release resources
    env->ReleaseByteArrayElements(yuvData, yuvBytes, JNI_ABORT);
    env->ReleaseIntArrayElements(outputPixels, outputInts, 0);
}

} // extern "C"

