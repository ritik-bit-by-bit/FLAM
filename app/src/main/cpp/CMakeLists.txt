cmake_minimum_required(VERSION 3.22.1)

project("opencv_processing")

# Set C++ standard
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Find OpenCV
set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/../../../opencv/sdk/native/jni)
find_package(OpenCV REQUIRED)

# Include directories
include_directories(${OpenCV_INCLUDE_DIRS})

# Add library
add_library(
    opencv_processing
    SHARED
    opencv_processing.cpp
)

# Link libraries
target_link_libraries(
    opencv_processing
    ${OpenCV_LIBS}
    android
    log
)

# Set output directory
set_target_properties(opencv_processing PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_LIBRARY_OUTPUT_DIRECTORY}
)

