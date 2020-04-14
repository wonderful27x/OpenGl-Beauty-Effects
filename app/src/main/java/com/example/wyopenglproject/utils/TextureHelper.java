package com.example.wyopenglproject.utils;

import static android.opengl.GLES20.*;

public class TextureHelper {

    /**
     * 创建并配置纹理
     * @param textureIds 纹理Id数组
     */
    public static void genTextures(int[] textureIds){
        //创建纹理，textureId.length :指定创建纹理数量 textureId纹理id保存，最后一个参数指定保存从数组哪个位置开始保存
        glGenTextures(textureIds.length,textureIds,0);
        //配置纹理,先绑定再操作，最后解除绑定
        //https://www.jianshu.com/p/1b0ecbd671ff
        for (int i=0; i<textureIds.length; i++){
            //绑定纹理
            glBindTexture(GL_TEXTURE_2D,textureIds[i]);
            //配置纹理-设置参数，当纹理被用到一个比它大或小的形状时，openGL该如何处理
            //当图元比纹理大时使用GL_LINEAR线性采样
            //GL_LINEAR线性采样：平滑过渡，但会变模糊
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
            //当图元比纹理小时使用
            //GL_NEAREST：最近点采样（速度快，但当图元比纹理大时会有明显的锯齿）
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_NEAREST);
            //GL_TEXTURE_WRAP_S/GL_TEXTURE_WRAP_T分别对应纹理坐标的x/y
            //配置纹理-设置纹理环绕，纹理坐标范围0-1，当超出范围时告诉openGL如何处理
            //GL_REPEAT重复拉伸（平铺）
            //GL_CLAMP_TO_EDGE边缘拉伸
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
            //解除绑定
            glBindTexture(GL_TEXTURE_2D,0);
        }
    }
}
