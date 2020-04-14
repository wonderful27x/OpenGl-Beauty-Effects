//
// Created by Acer on 2019/11/30.
//

#ifndef JNIPROJECT_ANDROIDLOG_H
#define JNIPROJECT_ANDROIDLOG_H

#include <android/log.h>
#define TAG "wonderfulJni"
#define ENABLE_LOG 1

#if ENABLE_LOG
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE,TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG,__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,TAG,__VA_ARGS__)
#else
#define LOGV(...)
#define LOGD(...)
#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#endif

#endif //JNIPROJECT_ANDROIDLOG_H
