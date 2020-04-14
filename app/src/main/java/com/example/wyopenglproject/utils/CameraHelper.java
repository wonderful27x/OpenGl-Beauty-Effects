package com.example.wyopenglproject.utils;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * 相机帮助类，主要用于摄像头数据的获取和预览
 */
public class CameraHelper implements Camera.PreviewCallback, SurfaceHolder.Callback {

    private static final String TAG = "CameraHelper";
    private SurfaceHolder holder;
    private Activity activity;
    private Camera camera;
    private int cameraId;
    private int width;
    private int height;
    private byte[] cameraBuff;
    private byte[] cameraBuffRotate;
    private int rotation;
    private SurfaceTexture surfaceTexture;

    private SizeChangedListener sizeChangedListener;
    private PreviewCallback previewCallback;


    public CameraHelper(Activity activity, int cameraId, int width, int height) {
        this.activity = activity;
        this.cameraId = cameraId;
        this.width = width;
        this.height = height;
    }

    public void setHolder(SurfaceHolder holder) {
        if (this.holder != null) {
            this.holder.removeCallback(this);
            this.holder = null;
        }
        this.holder = holder;
        this.holder.addCallback(this);
    }

    public void setPreviewTexture(SurfaceTexture surfaceTexture){
        this.surfaceTexture = surfaceTexture;
    }

    public void switchCamera() {
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopPreview();
        startPreview();
    }

    /**
     * 开始预览
     */
    public void startPreview() {
        try {
            //获得camera对象
            camera = Camera.open(cameraId);
            //设置camera属性
            Camera.Parameters parameters = camera.getParameters();
            //设置预览数据格式为NV21
            parameters.setPreviewFormat(ImageFormat.NV21);
            //设置摄像头预览尺寸
            setPreviewSize(parameters);
            //设置摄像头 图像传感器的角度、方向
            setPreviewOrientation(parameters);
            camera.setParameters(parameters);
            //数据缓存区,使用yuv数据格式计算大小
            cameraBuff = new byte[width * height * 3 / 2];
            cameraBuffRotate = new byte[width * height * 3 / 2];
            camera.addCallbackBuffer(cameraBuff);
            camera.setPreviewCallbackWithBuffer(this);
//            //设置渲染点surfaceHolder
//            camera.setPreviewDisplay(holder);
            //离屏渲染
            camera.setPreviewTexture(surfaceTexture);
            //开启预览
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    /**
     * 设置摄像头尺寸，因为自己设置的width和height摄像头不一定支持，
     * 需要做一些计算获取最接近的值
     *
     * @param parameters
     */
    private void setPreviewSize(Camera.Parameters parameters) {
        //获取摄像头支持的宽、高
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);
        Log.d(TAG, "Camera支持: " + size.width + "x" + size.height);
        //选择一个与设置的差距最小的支持分辨率
        int m = Math.abs(size.width * size.height - width * height);
        supportedPreviewSizes.remove(0);
        Iterator<Camera.Size> iterator = supportedPreviewSizes.iterator();
        //遍历
        while (iterator.hasNext()) {
            Camera.Size next = iterator.next();
            Log.d(TAG, "支持 " + next.width + "x" + next.height);
            int n = Math.abs(next.height * next.width - width * height);
            if (n < m) {
                m = n;
                size = next;
            }
        }
        width = size.width;
        height = size.height;
        parameters.setPreviewSize(width, height);
        Log.d(TAG, "预览分辨率 width:" + width + " height:" + height);
    }

    private void setPreviewOrientation(Camera.Parameters parameters) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(height, width);
                }
                break;
            case Surface.ROTATION_90: // 横屏 左边是头部(home键在右边)
                degrees = 90;
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(width, height);
                }
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(height, width);
                }
                break;
            case Surface.ROTATION_270:// 横屏 头部在右边
                degrees = 270;
                //回掉
                if (sizeChangedListener != null) {
                    sizeChangedListener.onChanged(width, height);
                }
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        //设置角度, 参考源码注释
        camera.setDisplayOrientation(result);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
//这里关于旋转和镜像的操作放在底层，通过openCv提供的函数来实现
//        switch (rotation) {
//            case Surface.ROTATION_0:
//                rotate90(bytes);
//                if (previewCallback != null) {
//                    previewCallback.onPreviewFrame(cameraBuffRotate);
//                }
//                camera.addCallbackBuffer(cameraBuff);//这句代码很重要，去掉后回调回中断
//                return;
//            case Surface.ROTATION_90:
//                break;
//            case Surface.ROTATION_270:
//                break;
//            default:
//                break;
//        }

        if (previewCallback != null) {
            previewCallback.onPreviewFrame(bytes);
        }
        camera.addCallbackBuffer(cameraBuff);//这句代码很重要，去掉后回调回中断
    }

    private void rotate90(byte[] data) {
        int index = 0;
        int ySize = width * height;
        //u和v
        int uvHeight = height / 2;
        //后置摄像头顺时针旋转90度
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            //将y的数据旋转之后 放入新的byte数组
            for (int i = 0; i < width; i++) {
                for (int j = height - 1; j >= 0; j--) {
                    cameraBuffRotate[index++] = data[width * j + i];
                }
            }

            //每次处理两个数据
            for (int i = 0; i < width; i += 2) {
                for (int j = uvHeight - 1; j >= 0; j--) {
                    // v
                    cameraBuffRotate[index++] = data[ySize + width * j + i];
                    // u
                    cameraBuffRotate[index++] = data[ySize + width * j + i + 1];
                }
            }
        } else {
            //逆时针旋转90度
            //            for (int i = 0; i < width; i++) {
            //                for (int j = 0; j < height; j++) {
            //                    cameraBuffRotate[index++] = data[width * j + width - 1 - i];
            //                }
            //            }
            //            //  u v
            //            for (int i = 0; i < width; i += 2) {
            //                for (int j = 0; j < uvHeight; j++) {
            //                    cameraBuffRotate[index++] = data[ySize + width * j + width - 1 - i - 1];
            //                    cameraBuffRotate[index++] = data[ySize + width * j + width - 1 - i];
            //                }
            //            }

            //旋转并镜像
            for (int i = 0; i < width; i++) {
                for (int j = height - 1; j >= 0; j--) {
                    cameraBuffRotate[index++] = data[width * j + width - 1 - i];
                }
            }
            //  u v
            for (int i = 0; i < width; i += 2) {
                for (int j = uvHeight - 1; j >= 0; j--) {
                    // v
                    cameraBuffRotate[index++] = data[ySize + width * j + width - 1 - i - 1];
                    // u
                    cameraBuffRotate[index++] = data[ySize + width * j + width - 1 - i];
                }
            }
        }
    }

    /**
     * surface创建时回调
     *
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    /**
     * surface改变时回调，如横竖屏切换
     *
     * @param surfaceHolder
     * @param i
     * @param i1
     * @param i2
     */
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        stopPreview();
        startPreview();
    }

    /**
     * surface销毁是回调
     *
     * @param surfaceHolder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopPreview();
    }


    /**
     * 数据预览回调，当获取到摄像头数据时回调出去，再通过底层把数据封装推流
     */
    public interface PreviewCallback {
        public void onPreviewFrame(byte[] bytes);
    }

    public void setPreviewCallback(PreviewCallback previewCallback) {
        this.previewCallback = previewCallback;
    }

    /**
     * surface大小发生改变回调接口，
     * 当其大小发生改变时数据的编码参数会发生改变，所以需要回调最后通知底层
     */
    public interface SizeChangedListener {
        public void onChanged(int width, int height);
    }

    public void setSizeChangedListener(SizeChangedListener sizeChangedListener) {
        this.sizeChangedListener = sizeChangedListener;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getCameraId() {
        return cameraId;
    }
}
