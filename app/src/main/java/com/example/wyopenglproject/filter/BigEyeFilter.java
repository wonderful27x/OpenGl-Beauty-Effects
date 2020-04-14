package com.example.wyopenglproject.filter;

import android.content.Context;
import com.example.wyopenglproject.R;
import com.example.wyopenglproject.face.Face;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import static android.opengl.GLES20.*;

/**
 * 大眼滤镜
 * 注意：这里纹理坐标需要旋转变换
 */
public class BigEyeFilter extends BaseFrameFilter {

    private int leftEyeIndex;         //左眼变量索引
    private int rightEyeIndex;        //右眼变量索引
    private FloatBuffer leftBuffer;   //左眼变量赋值缓存区
    private FloatBuffer rightBuffer;  //右眼变量赋值缓存区
    private Face face;                //人脸关键数据

    public BigEyeFilter(Context context) {
        super(context, R.raw.base_vertex_shader, R.raw.big_eye_frame_shader);
        //获取相关变量的索引
        leftEyeIndex = glGetUniformLocation(programId,"leftEye");  //顶点坐标
        rightEyeIndex = glGetUniformLocation(programId,"rightEye");//纹理坐标
        //创建变量赋值缓存区
        leftBuffer = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        rightBuffer = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
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
        if(face == null)return textureId;
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
        /**5.左右眼左边赋值（uniform类型，这与源码中是对应的）*/
        float [] landmarks = face.getLandmarks();
        //左眼(纹理坐标0-1)
        //这里关于坐标的理解仍然有些疑惑，TODO 下面是我自己的理解，有可能是错误的
        //camera中的宽高等于图像image的宽高，但是和glSurface的宽又不等，
        //这是打印的数据 camera width: 640 height: 480  ||  glSurface width: 1080 height: 1692
        //而这里的纹理坐标应该使用glSurface里的坐标，因此需要进行转换，转换的公式由课上老师的解释得出
        //设对应glSurface的横坐标为?,则 ? / glSurface.width = x / camera.width （纵坐标类似）
        //由于纹理坐标需要归一化，由上面的公式知道（x / camera.width）就是归一化坐标，
        //它等于glSurface上的归一坐标（? / glSurface.width），因此得出下面的计算纹理坐标的公式
        //其中landmarks[2] / face.getImageWidth()，等同于（x / camera.width）
        float x = landmarks[2] / face.getImageWidth();
        float y = landmarks[3] / face.getImageHeight();
        leftBuffer.clear();
        leftBuffer.put(x);
        leftBuffer.put(y);
        leftBuffer.position(0);
        glUniform2fv(leftEyeIndex,1,leftBuffer);
        //右眼(纹理坐标0-1)
        x = landmarks[4] / face.getImageWidth();
        y = landmarks[5] / face.getImageHeight();
        rightBuffer.clear();
        rightBuffer.put(x);
        rightBuffer.put(y);
        rightBuffer.position(0);
        glUniform2fv(rightEyeIndex,1,rightBuffer);

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

    public void setFace(Face face) {
        this.face = face;
    }
}
