//
// Created by Acer on 2020/4/11.
//

#include "FaceTrack.h"

//faceModel：人脸模型，seetaModel：中科院人脸5点特征分布模型
FaceTrack::FaceTrack(const char *faceModel, const char *seetaModel) {
    //保存模型路径
    this->faceModelPath = new char[strlen(faceModel) + 1];
    strcpy(this->faceModelPath,faceModel);
    this->seetaModelPath = new char[strlen(seetaModel) + 1];
    strcpy(this->seetaModelPath,seetaModel);

    //创建主适配器，传入人脸模型路径
    Ptr<CascadeClassifier> mainCascadeClassifier = makePtr<CascadeClassifier>(faceModelPath);
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(mainCascadeClassifier);
    //创建追踪检测适配器
    Ptr<CascadeClassifier> trackerCascadeClassifier = makePtr<CascadeClassifier>(faceModelPath);
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(trackerCascadeClassifier);
    //创建追踪器
    Ptr<DetectionBasedTracker> detectorTracker;
    DetectionBasedTracker::Parameters detectorParams;
    detectorTracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, detectorParams);

    this->detectorTracker = detectorTracker;
    this->faceAlignment = makePtr<seeta::FaceAlignment>(seetaModelPath);
}

FaceTrack::~FaceTrack() {
    if(this->faceModelPath){
        delete (this->faceModelPath);
        this->faceModelPath = nullptr;
    }
    if(this->seetaModelPath){
        delete (this->seetaModelPath);
        this->seetaModelPath = nullptr;
    }
}

void FaceTrack::startTracking() {
    this->detectorTracker->run();
}

void FaceTrack::stopTracking() {
    this->detectorTracker->stop();
}

/**
 * 人脸检测
 * @param src 原图
 * @param faceFeatures 人脸关键数据
 */
void FaceTrack::detector(Mat src, vector <Rect2f> &faceFeatures) {
    //获取人脸
    vector<Rect> faces;
    detectorTracker->process(src);
    detectorTracker->getObjects(faces);

    if(faces.size() > 0){
        Rect face = faces[0];
        //faceFeatures的第一个数据是人脸框
        faceFeatures.push_back(Rect2f(face.x,face.y,face.width,face.height));
        //构造中科院人脸关键点检测数据结构
        seeta::ImageData imageData(src.cols,src.rows);
        imageData.data = src.data;

        seeta::FaceInfo faceInfo;
        seeta::Rect bbox;
        bbox.x = face.x;
        bbox.y = face.y;
        bbox.width = face.width;
        bbox.height = face.height;
        faceInfo.bbox = bbox;
        //5点关键点
        seeta::FacialLandmark points[5];
        //检测关键点
        faceAlignment->PointDetectLandmarks(imageData,faceInfo,points);
        //把5个特征点也转成rect，统一输出格式
        for(int i=0; i< sizeof(points)/ sizeof(points[0]); i++){
            faceFeatures.push_back(Rect2f(points[i].x,points[i].y,0,0));
        }
    }
}
