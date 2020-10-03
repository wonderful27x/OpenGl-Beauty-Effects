package com.example.wyopenglproject.record;

import android.content.Context;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.util.Log;
import android.view.Surface;
import com.example.wyopenglproject.filter.ScreenFilter;
import static android.opengl.EGL14.*;

//OpenGL是一个操作GPU的API，它通过驱动向GPU发送相关指令，控制图形渲染管线状态机的运行状态。
//但OpenGL需要本地视窗系统进行交互，这就需要一个中间控制层，最好与平台无关。
//EGL——因此被独立的设计出来，它作为OpenGL ES和本地窗口的桥梁。

//作者：Damon_He
//链接：https://www.jianshu.com/p/299d23340528
//来源：简书
//著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

//作者：eric_la
//链接：https://www.jianshu.com/p/bc84a293e254
//来源：简书
//著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
public class WonderfulEgl {

    private static final String TAG = "WonderfulEgl";

    //EGL采用双缓冲机制，EGLDisplay可以理解为Front Display,EGLSurface为Back Surface
    private EGLDisplay eglDisplay;//系统显示 ID 或句柄，可以理解为一个前端的显示窗口
    private EGLSurface eglSurface;//系统窗口或 frame buffer 句柄 ，可以理解为一个后端的渲染目标窗口
    private EGLConfig eglConfig;  //Surface的EGL配置，可以理解为绘制目标framebuffer的配置属性
    //OpenGL ES 图形上下文，它代表了OpenGL状态机；如果没有它，OpenGL指令就没有执行的环境
    //有当前的颜色、纹理坐标、变换矩阵、绚染模式等一大堆状态，
    //这些状态作用于OpenGL API程序提交的顶点坐标等图元从而形成帧缓冲内的像素。
    //在OpenGL的编程接口中，Context就代表这个状态机，
    //OpenGL API程序的主要工作就是向Context提供图元、设置状态，偶尔也从Context里获取一些信息
    private EGLContext eglContext;
    private ScreenFilter screenFilter;

    public WonderfulEgl(Context context, EGLContext eglShareContext, Surface mediaCodecSurface,int width,int height){
        createEGL(context,eglShareContext,mediaCodecSurface,width,height);
    }

    private boolean createEGL(Context context, EGLContext eglShareContext, Surface mediaCodecSurface,int width,int height){
        /**
         * 1.创建EGLDisplay，使用默认设备（手机屏幕）
         */
        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL_NO_DISPLAY){
            Log.e(TAG, "EGLDisplay创建失败！");
            return false;
        }
        /**
         * 2.初始化设备
         */
        //版本号，如：EGL1.2 -> version[0]=1,version[1]=2
        int[] version = new int[2];
        eglInitialize(eglDisplay,version,0,version,1);
        /**
         * 3.初始化Surface的EGL配置Config，由于这个配置选项很多，并且不能随意组合，
         * 不同的设备支持的配置不一样，所以我们先根据自己的需求构造一个Config，
         * 然后调用eglChooseConfig()函数获取一个较接近的有效的系统配置Config
         */
        //数组以键值对形式，EGL_NONE结束
        int[] configAttrs = {
                //像素采用rgb格式
                EGL_RED_SIZE,8,
                EGL_GREEN_SIZE,8,
                EGL_BLUE_SIZE,8,
                EGL_ALPHA_SIZE,8,
                //渲染api类型
                EGL_RENDERABLE_TYPE,EGL_OPENGL_ES2_BIT,
                //告诉EGL以android兼容的方式创建surface
                EGLExt.EGL_RECORDABLE_ANDROID,1,
                EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] numConfig = new int[1];
        boolean chooseOk;
        chooseOk = eglChooseConfig(
                eglDisplay,        //egl显示窗口
                configAttrs,       //期望的配置
                0, //偏移
                configs,           //输出有效配置
                0,   //偏移
                configs.length,    //指定返回的配置数量
                numConfig,         //返回实际匹配的配置总数
                0//偏移
        );
        if (!chooseOk){
            Log.e(TAG, "EGLSurface配置EGLConfig获取失败！");
            return false;
        }
        eglConfig = configs[0];
        /**
         * 4.创建EGL上下文环境
         */
        int[] contextAttrs = {
                EGL_CONTEXT_CLIENT_VERSION,2,
                EGL_NONE
        };
        eglContext = eglCreateContext(eglDisplay,eglConfig,eglShareContext,contextAttrs,0);
        if(eglContext == EGL_NO_CONTEXT){
            Log.e(TAG, "EGL上下文EGLContext创建失败！");
            return false;
        }
        /**
         * 5.创建EGLSurface
         */
        int[] surfaceAttrs = {
                EGL_NONE
        };
        eglSurface = eglCreateWindowSurface(eglDisplay,eglConfig,mediaCodecSurface,surfaceAttrs,0);
        if(eglSurface == EGL_NO_SURFACE){
            Log.e(TAG, "EGLSurface创建失败！");
            return false;
        }
        /**
         * 6.绑定 EGLDisplay,EGLSurface,EGLContext
         */
        //关联context：
        //创建了Surface和Context之后，因为可能有多个Surface和Context，所以需要通过eglMakeCurrent绑定Context到Surface，
        //dpy为对应的Display，draw用于绘制，read用于读，ctx为要绑定的Context，成功是返回EGL_TRUE，失败时返回EGL_FALSE。
        //因为EGL规范要求eglMakeCurrent实现进行一次刷新，所以这一调用对于基于图块的架构代价很高。
        //作者：eric_la
        //链接：https://www.jianshu.com/p/bc84a293e254
        //来源：简书
        //著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
        boolean bindOk = eglMakeCurrent(eglDisplay,eglSurface,eglSurface,eglContext);
        if (!bindOk){
            Log.e(TAG, "EGL绑定失败！");
            return false;
        }
        /**
         * 7.创建渲染器
         */
        screenFilter = new ScreenFilter(context);
        screenFilter.onReady(width,height);
        return true;
    }

    //渲染
    public void draw(int textureId,long timestamp){
        //渲染
        screenFilter.onDrawFrame(textureId);
        //刷新时间戳
        EGLExt.eglPresentationTimeANDROID(eglDisplay,eglSurface,timestamp);
        //交换缓冲区
        eglSwapBuffers(eglDisplay,eglSurface);
    }

    //释放资源
    public void release(){
        eglMakeCurrent(eglDisplay,EGL_NO_SURFACE,EGL_NO_SURFACE,EGL_NO_CONTEXT);
        eglDestroySurface(eglDisplay,eglSurface);
        eglDestroyContext(eglDisplay,eglContext);
        eglReleaseThread();
        eglTerminate(eglDisplay);
    }
}
