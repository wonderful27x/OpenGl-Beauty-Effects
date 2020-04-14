#include <jni.h>
#include <string>
#include <pthread.h>
#include <android/native_window_jni.h>
#include <opencv2/imgproc/types_c.h>
#include "androidlog.h"
#include "FaceTrack.h"
#include "macro.h"
#include "JniCallback.h"

FaceTrack *faceTrack = nullptr;
JavaVM *javaVm = nullptr;
JniCallback *jniCallback = nullptr;

//创建追踪器、初始化
//这里将FaceTrack返回到java层，
//然后java调用的时候后传到底层，在转成FaceTrack
//感觉这样做没有什么必要，但是这种方法值得借鉴
extern "C"
JNIEXPORT jlong JNICALL
Java_com_example_wyopenglproject_face_FaceTrack_trackerCreateNative(JNIEnv *env, jobject thiz,
                                                                    jstring face_model,
                                                                    jstring seeta_model) {
    // TODO: implement trackerCreateNative()
    jniCallback = new JniCallback(javaVm,env,thiz);
    const char *facePath = env->GetStringUTFChars(face_model,nullptr);
    const char *seetaPath = env->GetStringUTFChars(seeta_model,nullptr);

    faceTrack = new FaceTrack(facePath,seetaPath);

    env->ReleaseStringUTFChars(face_model,facePath);
    env->ReleaseStringUTFChars(seeta_model,seetaPath);

    return reinterpret_cast<jlong>(faceTrack);
}


/**
 * native层人脸追踪和5点特征分布定位检测，将相机数据丢给底层，
 * 处理完成后返回人脸的关键数据，这是真正进行数据处理的入口
 * @param nativeFaceTrack native追踪类句柄
 * @param data 相机yuv数据，没有旋转和镜像处理
 * @param cameraId 相机Id
 * @param width 图像据宽
 * @param height 图像数据高
 * @return 定位到的人脸
 */
extern "C"
JNIEXPORT jobject JNICALL
Java_com_example_wyopenglproject_face_FaceTrack_faceDetectorNative(JNIEnv *env, jobject thiz,
                                                                   jlong native_face_track,
                                                                   jbyteArray data, jint camera_id,
                                                                   jint width, jint height) {
    // TODO: implement faceDetectorNative()
    static int count = 25;
//    LOGD("faceDetector start");
//    LOGD("图像宽：%d - 图像高：%d",width,height);
    FaceTrack *faceTracker = reinterpret_cast<FaceTrack *>(native_face_track);
    if(faceTracker == nullptr)return nullptr;

    jbyte *yuv = env->GetByteArrayElements(data, 0);
    //摄像头数据yuv（NV21）转成openCv的Mat
    //y分量有height行 + uv分量有height/2行
    Mat src = Mat(height + height/2 ,width,CV_8UC1,yuv);
//    //保存原始图像
//    imwrite("/sdcard/wonderful/camera.jpg",src);
    //转成YUV转RGBA
    cvtColor(src,src,CV_YUV2RGBA_NV21);
    //前置摄像头则旋转90度并基于y轴翻转
    if(camera_id == 1){
        rotate(src,src,ROTATE_90_COUNTERCLOCKWISE);//逆时针270度
        flip(src,src,1);
    }
    //后摄则旋转90度
    else{
        rotate(src,src,ROTATE_90_CLOCKWISE);//逆时针90度
    }
    //灰度化
    cvtColor(src,src,COLOR_RGB2GRAY);
    //均衡化
    equalizeHist(src,src);
    //追踪识别人脸
    vector<Rect2f> faceFeatures;
    faceTracker->detector(src,faceFeatures);
    env->ReleaseByteArrayElements(data, yuv, 0);

    int imgWidth = src.cols;
    int imgHeight = src.rows;
    int faceFeaturesLen = faceFeatures.size();
    //将人脸特征faceFeatures转成float数组
    jobject face = 0;
    if(faceFeaturesLen){
        //每个Rect有两个有用的左边点
        int size = faceFeaturesLen*2;
        jfloatArray floatArray = env->NewFloatArray(size);
        for(int i=0,j=0; i<size; j++,i += 2){
            float f[2] = {faceFeatures[j].x,faceFeatures[j].y};
            env->SetFloatArrayRegion(floatArray,i,2,f);
        }
        //人脸宽高
        int faceWidth = faceFeatures[0].width;
        int faceHeight = faceFeatures[0].height;
//        face = jniCallback->createFaceObject(faceWidth,faceHeight,imgWidth,imgHeight,floatArray);

        jclass faceClass = env->FindClass("com/example/wyopenglproject/face/Face");
        jmethodID faceConstructId = env->GetMethodID(faceClass,"<init>","(IIII[F)V");
        face = env->NewObject(faceClass,faceConstructId,imgWidth,imgHeight,faceWidth,faceHeight,floatArray);

        //画人脸框
        rectangle(src,faceFeatures[0],Scalar(255,0,0));
        //画关键点
        for(int i=1; i<faceFeaturesLen; i++){
            circle(src,Point2f(faceFeatures[i].x,faceFeatures[i].y),5,Scalar(0,0,255));
        }

        if(count > 0){
            imwrite("/sdcard/wonderful/face.jpg",src);
            count--;
        }
    }
    src.release();
//    LOGD("faceDetector end");
    return face;
}

//开器追踪器进行追踪定位
extern "C"
JNIEXPORT void JNICALL
Java_com_example_wyopenglproject_face_FaceTrack_startTrackNative(JNIEnv *env, jobject thiz,
                                                                 jlong native_face_track) {
     //TODO: implement startTrackNative()
    if(native_face_track != 0){
        FaceTrack *faceTracker = reinterpret_cast<FaceTrack *>(native_face_track);
        faceTracker->startTracking();
    }
}

//停止
extern "C"
JNIEXPORT void JNICALL
Java_com_example_wyopenglproject_face_FaceTrack_stopTrackNative(JNIEnv *env, jobject thiz,
                                                                jlong native_face_track) {
     //TODO: implement stopTrackNative()
    if(native_face_track != 0){
        FaceTrack *faceTracker = reinterpret_cast<FaceTrack *>(native_face_track);
        faceTracker->stopTracking();
        DELETE (faceTracker);
    }
    if(faceTrack){
        faceTrack = nullptr;
    }
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_wyopenglproject_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


jint JNI_OnLoad(JavaVM *vm,void *argc){
    javaVm = vm;
    return JNI_VERSION_1_6;
}
////在so卸载时调用
//void JNI_OnUnload(JavaVM *jvm, void *reserved){
//    if(!wonderfulOpenCv){
//        delete (wonderfulOpenCv);
//        wonderfulOpenCv = nullptr;
//    }
//}