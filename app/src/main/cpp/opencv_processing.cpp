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
    
    LOGI("Processing frame: %dx%d, processing: %d", width, height, enableProcessing);
    
    // Get input data
    jbyte* yuvBytes = env->GetByteArrayElements(yuvData, nullptr);
    jint* outputInts = env->GetIntArrayElements(outputPixels, nullptr);
    
    if (yuvBytes == nullptr || outputInts == nullptr) {
        LOGE("Failed to get array elements");
        return;
    }
    
    // Convert YUV to Mat
    // NV21 format: Y plane (width*height) + interleaved VU plane (width*height/2)
    int yuvHeight = height + height / 2;
    Mat yuvMat(yuvHeight, width, CV_8UC1, (unsigned char*)yuvBytes);
    Mat rgbMat;
    
    LOGI("YUV Mat created: %dx%d", yuvMat.cols, yuvMat.rows);
    
    // Convert YUV to RGB (NV21 format)
    try {
        cvtColor(yuvMat, rgbMat, COLOR_YUV2RGB_NV21);
        LOGI("YUV to RGB conversion successful: %dx%d", rgbMat.cols, rgbMat.rows);
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
            // Convert BGR to RGBA (OpenGL format: R G B A)
            // OpenCV uses BGR, so pixel[0]=B, pixel[1]=G, pixel[2]=R
            // OpenGL expects RGBA, so we need: R G B A
            int rgba = (pixel[2] << 24) | (pixel[1] << 16) | (pixel[0] << 8) | 0xFF;
            pixels[y * width + x] = rgba;
        }
    }
    
    LOGI("Converted %dx%d pixels to RGBA format", width, height);
    
    // Release resources
    env->ReleaseByteArrayElements(yuvData, yuvBytes, JNI_ABORT);
    env->ReleaseIntArrayElements(outputPixels, outputInts, 0);
}

} // extern "C"

