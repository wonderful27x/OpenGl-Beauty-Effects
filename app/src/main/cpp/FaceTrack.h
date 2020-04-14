//
// Created by Acer on 2020/4/11.
//

#ifndef WYOPENGLPROJECT_FACETRACK_H
#define WYOPENGLPROJECT_FACETRACK_H

#include "CascadeDetectorAdapter.h"
#include <vector>
#include <face_alignment.h>

using namespace std;

class FaceTrack {
public:

    FaceTrack(const char *faceModel, const char *seetaModel);
    ~FaceTrack();

    void startTracking();
    void stopTracking();
    void detector(Mat src,vector<Rect2f> &faceFeatures);

private:
    char *faceModelPath = nullptr;                        //人脸模型路径
    char *seetaModelPath = nullptr;                       //人脸五点检测路径
    Ptr<DetectionBasedTracker> detectorTracker = nullptr; //人脸追踪器
    Ptr<seeta::FaceAlignment> faceAlignment = nullptr;    //人脸五点检测
};


#endif //WYOPENGLPROJECT_FACETRACK_H
