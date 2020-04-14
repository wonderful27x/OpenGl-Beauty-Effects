package com.example.wyopenglproject.filter;

import android.content.Context;
import com.example.wyopenglproject.utils.TextureHelper;
import static android.opengl.GLES20.*;

/**
 * 在FBO上操作的滤镜基类
 */
public class BaseFrameFilter extends BaseFilter {

    protected int[] frameBuffers;       //FBO id数组
    protected int[] frameBufferTextures;//FBO的纹理id数组

    public BaseFrameFilter(Context context, int vertexSourceId, int fragmentSourceId) {
        super(context, vertexSourceId, fragmentSourceId);
    }

    //TODO 特别注意：这里基类，千万别重写这个方法，因为子类的纹理坐标需不需要变换，
    //TODO 如何变换是有特异性的，如果重写了，而子类的纹理坐标变换与其不一致又忘记重写了，
    //TODO 这个错误几乎无法定位
//    @Override
//    protected void changeTextureCoordinate() {
//        float[] TEXTURE = {
//                //逆时针旋转并镜像翻转
//                0.0f, 0.0f,
//                1.0f, 0.0f,
//                0.0f, 1.0f,
//                1.0f, 1.0f
//        };
//        textureCoordinateBuffer.clear();
//        textureCoordinateBuffer.put(TEXTURE);
//    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        if (frameBuffers != null){
            releaseFBO();
        }
        //创建FBO(一个虚拟的屏幕，并不会显示出来，可以用来操作各种滤镜效果)
        frameBuffers = new int[1];
        glGenFramebuffers(frameBuffers.length,frameBuffers,0);
        //创建属于FBO的纹理，这里的纹理需要配置
        frameBufferTextures = new int[1];
        TextureHelper.genTextures(frameBufferTextures);
        //让FBO与其纹理相绑定
        glBindTexture(GL_TEXTURE_2D,frameBufferTextures[0]);
        glBindFramebuffer(GL_FRAMEBUFFER,frameBuffers[0]);

        //纹理允许着色器读取图像阵列的元素。
        //要定义纹理图像，请调用glTexImage2D
        glTexImage2D(
                GL_TEXTURE_2D,    //此纹理是一个2D纹理
                0,          //代表图像的详细程度, 默认为0即可
                GL_RGBA,          //指定纹理的内部格式
                width,            //纹理的宽度
                height,           //纹理的高度
                0,        //边框的值
                GL_RGBA,          //指定纹理数据的格式，必须匹配上面的格式
                GL_UNSIGNED_BYTE, //组成图像的数据是无符号字节类型
                null       //指定一个指向内存中图像数据的指针。
        );
        //将纹理图像附加到帧缓冲对象
        glFramebufferTexture2D(
                GL_FRAMEBUFFER,          //指定帧缓冲目标。 符号常量必须是GL_FRAMEBUFFER。
                GL_COLOR_ATTACHMENT0,    //指定应附加纹理图像的附着点。
                GL_TEXTURE_2D,           //指定纹理目标。
                frameBufferTextures[0],  //指定要附加图像的纹理对象。
                0                  //指定要附加的纹理图像的mipmap级别，该级别必须为0。
        );

        glBindFramebuffer(GL_FRAMEBUFFER,0);
        glBindTexture(GL_TEXTURE_2D,0);
    }

    private void releaseFBO(){
        if(frameBuffers != null){
            glDeleteFramebuffers(1,frameBuffers,0);
            frameBuffers = null;
        }
        if(frameBufferTextures != null){
            glDeleteTextures(1,frameBufferTextures,0);
            frameBufferTextures = null;
        }
    }

    @Override
    public void release() {
        super.release();
        releaseFBO();
    }
}
