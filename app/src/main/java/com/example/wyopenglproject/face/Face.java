package com.example.wyopenglproject.face;

import java.util.Arrays;

/**
 * 人脸图像封装类
 */
public class Face {

    //原始图片宽高
    private int imageWidth;
    private int imageHeight;
    //人脸框宽高
    private int faceWidth;
    private int faceHeight;
    //这个数组每两个值为一个坐标xy，前两个坐标保存人脸框在图像中的左上角坐标，
    //后面的才是人脸关键点坐标
    //人脸的关键点坐标，这里使用中科院的算法定位5个关键点：
    //即2眼睛、鼻尖、2嘴角，关键点算法其实还有更详细的68点特征分布等。
    //https://blog.csdn.net/zj360202/article/details/78674700?utm_source=blogxgwz2
    private float[] landmarks;

    public Face(int imageWidth, int imageHeight, int faceWidth, int faceHeight, float[] landmarks) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.faceWidth = faceWidth;
        this.faceHeight = faceHeight;
        this.landmarks = landmarks;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getFaceWidth() {
        return faceWidth;
    }

    public void setFaceWidth(int faceWidth) {
        this.faceWidth = faceWidth;
    }

    public int getFaceHeight() {
        return faceHeight;
    }

    public void setFaceHeight(int faceHeight) {
        this.faceHeight = faceHeight;
    }

    public float[] getLandmarks() {
        return landmarks;
    }

    public void setLandmarks(float[] landmarks) {
        this.landmarks = landmarks;
    }

    @Override
    public String toString() {
        return "Face{" +
                "imageWidth=" + imageWidth +
                ", imageHeight=" + imageHeight +
                ", faceWidth=" + faceWidth +
                ", faceHeight=" + faceHeight +
                ", landmarks=" + Arrays.toString(landmarks) +
                '}';
    }
}
