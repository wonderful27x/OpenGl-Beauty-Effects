//
// Created by Acer on 2020/3/16.
//

#ifndef WYOPENCV_CASCADEDETECTORADAPTER_H
#define WYOPENCV_CASCADEDETECTORADAPTER_H

#include <opencv2/opencv.hpp>
using namespace cv;

//动态人脸检测需要用的适配器，官方demo复制而来
//E:\OPENCV\opencv4.1.2\INSTALL\opencv\sources\samples\android\face-detection\jni\DetectionBasedTracker_jni.cpp
class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector
{
public:
    CascadeDetectorAdapter(Ptr<CascadeClassifier> detector) :IDetector(),Detector(detector)
    {
        CV_Assert(detector);
    }
    virtual ~CascadeDetectorAdapter()
    {
    }

    void detect(const Mat& Image, std::vector<Rect>& objects)
    {
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize, maxObjSize);
    }

private:
    CascadeDetectorAdapter();
    Ptr<CascadeClassifier> Detector;
};

#endif //WYOPENCV_CASCADEDETECTORADAPTER_H
