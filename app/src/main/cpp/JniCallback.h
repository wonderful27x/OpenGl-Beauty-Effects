//
// Created by Acer on 2019/12/31.
//

#ifndef WYWONDERFULPLAYER_JNICALLBACK_H
#define WYWONDERFULPLAYER_JNICALLBACK_H

#include <jni.h>
#include "errorType.h"

class JniCallback {
public:
    JniCallback(JavaVM *javaVm,JNIEnv *env,jobject instance);
    ~JniCallback();
    void error(int threadMode,ErrorType type);
    jobject createFaceObject( int imgWidth, int imgHeight,int faceWidth, int faceHeight,jfloatArray floatArray);

private:
    JavaVM *javaVm = nullptr;
    JNIEnv *env = nullptr;
    jobject instance = 0;
    jmethodID errorId = 0;
    jmethodID faceConstructId = 0;
    jclass faceClass = 0;
};


#endif //WYWONDERFULPLAYER_JNICALLBACK_H
