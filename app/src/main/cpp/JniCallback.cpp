//
// Created by Acer on 2019/12/31.
//

#include "JniCallback.h"
#include "macro.h"
#include <jni.h>

JniCallback::JniCallback(JavaVM *javaVm,JNIEnv *env,jobject instance) {
    this->javaVm = javaVm;
    this->env = env;
    this->instance = env->NewGlobalRef(instance);
    this->faceClass = env->FindClass("com/example/wyopenglproject/face/Face");
    this->faceConstructId = env->GetMethodID(faceClass,"<init>","(IIII[F)V");
}

JniCallback::~JniCallback() {
    javaVm = nullptr;
    env->DeleteGlobalRef(instance);
    env = nullptr;
    instance = 0;
    errorId = 0;
    faceClass = 0;
    faceConstructId = 0;
}

jobject JniCallback::createFaceObject( int imgWidth, int imgHeight,int faceWidth, int faceHeight,jfloatArray floatArray) {
    return env->NewObject(faceClass,faceConstructId,imgWidth,imgHeight,faceWidth,faceHeight,floatArray);
}

void JniCallback::error(int threadMode,ErrorType type) {
    if(!errorId || instance)return;
    //主线程jni回调
    if(threadMode == THREAD_MAIN){
        env->CallVoidMethod(instance,errorId,type);
    }
    //子线程的接口回调不能使用主线程的env,需要通过JavaVM拿到子线程的env
    else if(threadMode == THREAD_CHILD){
        JNIEnv *childEnv;
        javaVm->AttachCurrentThread(&childEnv,nullptr);
        childEnv->CallVoidMethod(instance,errorId,type);
        javaVm->DetachCurrentThread();
    }
}

