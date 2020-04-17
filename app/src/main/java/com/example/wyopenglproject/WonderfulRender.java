package com.example.wyopenglproject;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;
import com.example.wyopenglproject.face.FaceTrack;
import com.example.wyopenglproject.filter.BeautyFilter;
import com.example.wyopenglproject.filter.BigEyeFilter;
import com.example.wyopenglproject.filter.CameraFilter;
import com.example.wyopenglproject.filter.ScreenFilter;
import com.example.wyopenglproject.filter.StickFilter;
import com.example.wyopenglproject.record.WonderfulMediaRecorder;
import com.example.wyopenglproject.utils.CameraHelper;
import com.example.wyopenglproject.utils.FileUtil;
import java.io.File;
import java.io.IOException;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import static android.opengl.GLES20.*;

/**
 * 自定义着色器
 */
//https://www.cnblogs.com/wytiger/p/5693569.html //surfaceTexture
//初次接触SurfaceView、TextureView、GLSurfaceView、SurfaceTexture一些迷惑，但他们并不是同一个东西，
//SurfaceTexture并不是View，它可以将相机等的数据转换成GL纹理，然后就可以对纹理进行各种处理，
//而TextureView和GLSurfaceView都可将纹理显示出来
public class WonderfulRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener,CameraHelper.PreviewCallback {

    private static final String TAG = "WonderfulRender";
    
    //openGL提供的surface，用于显示纹理数据
    private GLSurfaceView surfaceView;
    //相机
    private CameraHelper cameraHelper;
    //纹理id
    private int[] textureId;
    //画布，可以将相机等的数据源转成纹理
    private SurfaceTexture surfaceTexture;
    //相机滤镜,赋值将图像绘制到FBO
    private CameraFilter cameraFilter;
    //大眼滤镜，真正的滤镜
    private BigEyeFilter bigEyeFilter;
    //贴纸滤镜，真正的滤镜
    private StickFilter stickFilter;
    //美颜滤镜，真正的滤镜
    private BeautyFilter beautyFilter;
    //屏幕滤镜，赋值将FBO渲染到屏幕
    private ScreenFilter screenFilter;
    //变换矩阵
    private float[] matrix = new float[16];
    //录屏
    private WonderfulMediaRecorder recorder;
    //人脸追踪器
    private FaceTrack faceTrack;

    private String lbpFaceModelPath;
    private String seetaModelPath;

    private int width;
    private int height;

    //TODO 测试用
    private boolean bigEyeEnable = false;

    public WonderfulRender(GLSurfaceView surfaceView){
        this.surfaceView = surfaceView;
        //将模型文件拷贝的sd卡
        lbpFaceModelPath = FileUtil.copyAssets2SDCard(surfaceView.getContext(),"lbpFaceModel.xml");
        seetaModelPath = FileUtil.copyAssets2SDCard(surfaceView.getContext(),"seetaModel.bin");
    }

    /**
     * 画布创建时调用
     * @param gl 1.0 api预留参数
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textureId = new int[1];
        //创建纹理，textureId.length :指定创建纹理数量 textureId纹理id保存，
        glGenTextures(textureId.length,textureId,0);

        //创建SurfaceTexture并将openGL生成的纹理绑定上
        surfaceTexture = new SurfaceTexture(textureId[0]);
        //设置回调,当画布有有效数据时回调
        surfaceTexture.setOnFrameAvailableListener(this);

        //创建相机
        cameraHelper = new CameraHelper((Activity) surfaceView.getContext(), Camera.CameraInfo.CAMERA_FACING_FRONT,640,480);
        cameraHelper.setPreviewTexture(surfaceTexture);
        cameraHelper.setPreviewCallback(this);

        //创建滤镜
        cameraFilter = new CameraFilter(surfaceView.getContext());
        screenFilter = new ScreenFilter(surfaceView.getContext());

        EGLContext eglContext = EGL14.eglGetCurrentContext();
        File file = new File(Environment.getExternalStorageDirectory(),File.separator + "wonderful" + File.separator + "openGL_record_" + System.currentTimeMillis() + ".mp4");
        String path = file.getAbsolutePath();
        recorder = new WonderfulMediaRecorder(surfaceView.getContext(),eglContext,cameraHelper.getWidth(),cameraHelper.getHeight(),path);
    }

    /**
     * 画布发生改变时回调
     * @param gl 1.0 api预留参数
     * @param width
     * @param height
     *
     * camera中的宽高等于图像image的宽高，但是和glSurface的宽高不等，
     * 这是打印的数据 camera width: 640 height: 480  ||  glSurface width: 1080 height: 1692
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        cameraHelper.startPreview();
        faceTrack = new FaceTrack(cameraHelper,lbpFaceModelPath,seetaModelPath);
        //启动追踪器
        faceTrack.startTrack();
        cameraFilter.onReady(width,height);
        screenFilter.onReady(width,height);

        Log.d(TAG, "camera width: " + cameraHelper.getWidth() + " height: " + cameraHelper.getHeight());
        Log.d(TAG, "glSurface width: " + width + " height: " + height);
    }

    /**
     * 会画一帧图像时回调
     * 该方法必须进行绘画操作（返回后会交换渲染缓冲区，不绘制会导致闪屏）
     * 由测试得出：当模式设置为按需渲染时，surfaceView.requestRender()触发了此方法的回调
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame: ");
        //设置清屏颜色
        glClearColor(255,0,0,0);
        //清理颜色缓冲区
        glClear(GL_COLOR_BUFFER_BIT);

        //绘制相机图像数据
        //一般在onDrawFrame中调用updateTexImage()将数据绑定给OpenGLES对应的纹理对象。
        //注意，必须显示的调用updateTexImage()将数据更新到GL_OES_EGL_image_external类型的OpenGL ES纹理对象中后，
        //SurfaceTexture才有空间来获取下一帧的数据。否则下一帧数据永远不会交给SurfaceTexture。
        //版权声明：本文为CSDN博主「lyzirving」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
        //原文链接：https://blog.csdn.net/lyzirving/java/article/details/79051437
        surfaceTexture.updateTexImage();
        //当从OpenGL ES的纹理对象取样时，首先应该调用getTransformMatrix()来转换纹理坐标。
        //每次updateTexImage()被调用时，纹理矩阵都可能发生变化。所以，每次texture image被更新时，getTransformMatrix ()也应该被调用。
        //版权声明：本文为CSDN博主「lyzirving」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
        //原文链接：https://blog.csdn.net/lyzirving/java/article/details/79051437
        //根据老师的说法，matrix只是一个变换矩阵，不是像素
        surfaceTexture.getTransformMatrix(matrix);
        cameraFilter.setMatrix(matrix);
        //渲染到FBO
        int textureID = cameraFilter.onDrawFrame(textureId[0]);
        //。。。各种滤镜叠加
        //大眼滤镜
        if(bigEyeFilter != null){
            bigEyeFilter.setFace(faceTrack.getFace());
            textureID = bigEyeFilter.onDrawFrame(textureID);
        }
        //贴纸滤镜
        if(stickFilter != null){
            stickFilter.setFace(faceTrack.getFace());
            textureID = stickFilter.onDrawFrame(textureID);
        }
        //美颜滤镜
        if(beautyFilter != null){
            textureID = beautyFilter.onDrawFrame(textureID);
        }
        //渲染到屏幕
        screenFilter.onDrawFrame(textureID);
        //录屏
        recorder.encodeFrame(textureID,surfaceTexture.getTimestamp());

    }

    /**========================================*/

    //当画布SurfaceTexture有有效数据时回调，告诉GLSurfaceView可以显示了
    //SurfaceTexture.OnFrameAvailableListener
    //在前面的代码中，此接口设置给了SurfaceTexture，
    //而SurfaceTexture又设置给了相机 -> cameraHelper.setPreviewTexture(surfaceTexture);
    //而SurfaceTexture可以将相机等的数据转换成GL纹理，于是我们猜测当相机获取到数据时他就绘制到surfaceTexture上，
    //或者里理解为相机把数据交给了surfaceTexture，这样surfaceTexture获取到这帧数据后就回掉onFrameAvailable这个接口，
    //然后我们在通知GLSurface进行渲染（而GLSurface的渲染应该指的是GPU的操作，可能是滤镜的处理可能是绘制到屏幕上）
    //经过上面的分析于是我们得出利用openGL渲染相机数据的流程：
    //1.摄像头获取一帧数据
    //2.交给SurfaceTexture
    //3.SurfaceTexture回调onFrameAvailable
    //4.按需渲染-调用GLSurface.requestRender()
    //5.GLSurface回调onDrawFrame(GL10 gl)方法
    //6.从SurfaceTexture中取出纹理并交给openGl
    //7.openGl对纹理进行采用处理（一般是绘制的FBO上）
    //8.渲染到屏幕上
    //实际上从6-8的理解还是有点模糊的，这里面涉及到EGL环境，而EGL如何和纹理联系起来的至今仍没有搞明白
    //或许可以这样理解，我们知道GLSurface有一套自己的EGL环境，因此对纹理的操作实际上是渲染到了GLSurface内部创建的surface缓存去上了
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //This method is typically used when the render mode has been set to
        //{@link #RENDERMODE_WHEN_DIRTY}, so that frames are only rendered on demand.
        //从注释中可以知道，这就是按需渲染，当有数据时才渲染，而我们在创建surfaceView时设置的就是按需渲染
        surfaceView.requestRender();

        Log.d(TAG, "onFrameAvailable: ");
    }

    public void surfaceDestroyed(){
        cameraHelper.stopPreview();
        faceTrack.stopTrack();
        stopRecording();
    }

    public void startRecording(float speed){
        try {
            recorder.start(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording(){
        recorder.stop();
    }

    //相机数据回调
    @Override
    public void onPreviewFrame(byte[] bytes) {
        //人脸定位检测
//        Log.d(TAG, "onPreviewFrame: ");
        faceTrack.faceDetector(bytes);
    }

    /**
     * 大眼滤镜开关
     * @param enable
     */
    public void enableBigEye(final boolean enable){
        bigEyeEnable = enable;
        surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if(enable){
                    bigEyeFilter = new BigEyeFilter(surfaceView.getContext());
                    bigEyeFilter.onReady(width,height);
                }else {
                    if(bigEyeFilter != null){
                        bigEyeFilter.release();
                        bigEyeFilter = null;
                    }
                }
            }
        });
    }

    /**
     * 贴纸滤镜开关
     * @param enable
     * @param stickSourceId 贴纸资源id
     */
    public void enableStick(final boolean enable,final int stickSourceId){
        surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if(enable){
                    stickFilter = new StickFilter(surfaceView.getContext(),stickSourceId);
                    stickFilter.onReady(width,height);
                }else {
                    if(stickFilter != null){
                        stickFilter.release();
                        stickFilter = null;
                    }
                }
            }
        });
    }

    /**
     * 美颜开关
     * @param enable
     */
    public void enableBeauty(final boolean enable){
        surfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if(enable){
                    beautyFilter = new BeautyFilter(surfaceView.getContext());
                    beautyFilter.onReady(width,height);
                }else {
                    if(beautyFilter != null){
                        beautyFilter.release();
                        beautyFilter = null;
                    }
                }
            }
        });
    }
}
