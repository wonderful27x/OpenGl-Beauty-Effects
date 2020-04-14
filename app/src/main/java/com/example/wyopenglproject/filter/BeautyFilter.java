package com.example.wyopenglproject.filter;

import android.content.Context;
import com.example.wyopenglproject.R;
import static android.opengl.GLES20.*;

/**
 * 美颜滤镜
 */
public class BeautyFilter extends BaseFrameFilter {

    //纹理宽高
    private int textureWidth;
    private int textureHeight;

    public BeautyFilter(Context context) {
        super(context, R.raw.base_vertex_shader, R.raw.beauty_frame_shader);
        textureWidth = glGetUniformLocation(programId,"textureWidth");
        textureHeight = glGetUniformLocation(programId,"textureHeight");
    }

    @Override
    protected void changeTextureCoordinate() {
        float[] TEXTURE = {
                //逆时针旋转并镜像翻转
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        textureCoordinateBuffer.clear();
        textureCoordinateBuffer.put(TEXTURE);
    }

    @Override
    public int onDrawFrame(int textureId) {
        /**0.绑定FBO否则会渲染到屏幕上*/
        glBindFramebuffer(GL_FRAMEBUFFER,frameBuffers[0]);
        /**1.设置视窗大小*/
        glViewport(0,0,width,height);
        /**2.使用着色器程序*/
        glUseProgram(programId);
        /**3.顶点坐标赋值*/
        //因为要是从第0个开始取，所有设置position为零，相当于一个偏移
        vertexCoordinateBuffer.position(0);
        //顶点坐标赋值（attribute类型，这与源码中是对应的）
        //p1：顶点坐标索引，p2：坐标维度（2D,相对与每次取2个数据为一个坐标），p3：数据类型
        //p4：指定在访问定点数据值时是应将其标准化（GL_TRUE）还是直接转换为定点值（GL_FALSE）。
        //p5：指定连续通用顶点属性之间的字节偏移量。 如果stride为0，则通用顶点属性被理解为紧密打包在数组中的。 初始值为0
        //p6：指定指向数组中第一个通用顶点指针
        glVertexAttribPointer(vertexCoordinate,2,GL_FLOAT,false,0,vertexCoordinateBuffer);
        //激活
        glEnableVertexAttribArray(vertexCoordinate);
        /**4.纹理坐标赋值（attribute类型，这与源码中是对应的），原理参数同上*/
        textureCoordinateBuffer.position(0);
        glVertexAttribPointer(textureCoordinate,2,GL_FLOAT,false,0,textureCoordinateBuffer);
        //激活
        glEnableVertexAttribArray(textureCoordinate);
        /**5.纹理宽高赋值，从这里可以看出glSurface宽高就是纹理宽高*/
         glUniform1i(textureWidth,width);
         glUniform1i(textureHeight,height);
        /**6.激活图层*/
        glActiveTexture(GL_TEXTURE0);
        /**7.绑定纹理*/
        glBindTexture(GL_TEXTURE_2D,textureId);
        //采样器赋值，这个有点晕，0好像是默认的位置
        glUniform1i(texture,0);
        /**8.通知openGL绘制*/
        //以连续三顶点绘制三角形的方式绘制
        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
        /**解除绑定*/
        glBindTexture(GL_TEXTURE_2D,0);
        glBindFramebuffer(GL_FRAMEBUFFER,0);
        return frameBufferTextures[0];
    }
}
