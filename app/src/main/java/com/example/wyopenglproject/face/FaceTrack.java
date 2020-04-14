package com.example.wyopenglproject.face;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.wyopenglproject.utils.CameraHelper;

/**
 * 人脸检测，底层通过openCv和中科院的算法追踪定位人脸
 * 这种方法其实是通过openCv和其他算在底层追踪定位到人脸，然后又把人脸数据返回到java层，
 * 在java层再通过openGl实现滤镜效果，但是为什么不直接在底层实现滤镜和渲染呢，
 * 频繁的jni调用肯定会造成性能问题，我们姑且先这样做
 * https://zhuanlan.zhihu.com/p/22451474
 */
public class FaceTrack {

    private static final String TAG = "FaceTrack";

    static {
        System.loadLibrary("native-lib");
    }

    //底层人脸追踪类的句柄（指针地址），后面有用
    private long nativeFaceTrack;
    //追踪到的人脸封装数据
    private Face face;
    private CameraHelper cameraHelper;
    private HandlerThread handlerThread;
    private FaceTrackTask handler;

    /**
     * 构造函数
     * @param cameraHelper 相机
     * @param faceModel 人脸模型
     * @param seetaModel 中科院5点特征分布模型
     */
    public FaceTrack(CameraHelper cameraHelper,String faceModel,String seetaModel){
        this.cameraHelper = cameraHelper;
        nativeFaceTrack = trackerCreateNative(faceModel,seetaModel);
        handlerThread = new HandlerThread("FaceTrack-Thread");
        handlerThread.start();
        handler = new FaceTrackTask(handlerThread.getLooper());
    }

    public void startTrack(){
        startTrackNative(nativeFaceTrack);
    }

    public void stopTrack(){
        synchronized (this){
            handlerThread.quitSafely();
            handler.removeCallbacksAndMessages(null);
            stopTrackNative(nativeFaceTrack);
            nativeFaceTrack = 0;
        }
    }

    public void faceDetector(byte[] data){
        if (handler != null){
            //将积压的111号任务移除
            handler.removeMessages(111);
            //重新添加111号任务
            Message message = handler.obtainMessage(111);
            message.obj = data;
            handler.handleMessage(message);

        }
    }

    public Face getFace() {
        return face;
    }

    //在线程中执行人脸追踪任务
    private class FaceTrackTask extends Handler{

        private FaceTrackTask(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            synchronized (FaceTrack.class){
                face = faceDetectorNative(nativeFaceTrack,(byte[]) msg.obj,cameraHelper.getCameraId(),cameraHelper.getWidth(),cameraHelper.getHeight());
                if (face != null){
                    Log.e(TAG, face.toString());
                }
            }
        }
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
    private native Face faceDetectorNative(long nativeFaceTrack,byte[] data, int cameraId,int width,int height);
    //创建追踪器、初始化
    private native long trackerCreateNative(String faceModel,String seetaModel);
    //开器追踪器进行追踪定位
    private native void startTrackNative(long nativeFaceTrack);
    //停止
    private native void stopTrackNative(long nativeFaceTrack);
}
