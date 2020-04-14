# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.



include_directories(opencv/include)
include_directories(faceAlignment/include)

#子模块
add_subdirectory(faceAlignment)

file(GLOB SOURCE *.cpp)

add_library( # Sets the name of the library.
        native-lib
        SHARED
#        native-lib.cpp
        ${SOURCE}
        )

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/opencv/libs/${CMAKE_ANDROID_ARCH_ABI}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/../jniLibs/${CMAKE_ANDROID_ARCH_ABI}")

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib
        -Wl,--start-group
        seeta_fa_lib
        opencv_java4
#        opencv_calib3d
#        opencv_core
#        opencv_dnn
#        opencv_features2d
#        opencv_flann
#        opencv_highgui
#        opencv_imgcodecs
#        opencv_imgproc
#        opencv_ml
#        opencv_objdetect
#        opencv_photo
#        opencv_stitching
#        opencv_video
#        opencv_videoio
        -Wl,--end-group
        z
        android
        ${log-lib})