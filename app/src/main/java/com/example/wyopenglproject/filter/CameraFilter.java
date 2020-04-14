package com.example.wyopenglproject.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import static android.opengl.GLES20.*;
import com.example.wyopenglproject.R;

/**
 * 相机滤镜，只负责把相机数据渲染到FBO上
 * 注意：这里的纹理坐标不需要变化，和原屏幕顺序一致即可
 */
public class CameraFilter extends BaseFrameFilter{

    private float[] matrix;  //变换矩阵

    public CameraFilter(Context context) {
        super(context, R.raw.camera_vertex_shader, R.raw.camera_fragment_shader);
    }

    /**
     * 绘制
     * @param textureId 摄像头纹理id
     * @return
     */
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
        /**5.变化矩阵赋值（uniform类型，这与源码中是对应的）*/
        //p1：矩阵索引，p2：被赋值的Uniform数量（可以是数组所以可以有多个）
        //p3：uniform变量赋值时该矩阵是否需要转置。因为我们使用的是glm定义的矩阵，因此不要进行转置
        //p4：数据指针
        //p5：偏移
        glUniformMatrix4fv(transformMatrix,1,false,matrix,0);
        /**6.激活图层*/
        glActiveTexture(GL_TEXTURE0);
        /**7.绑定纹理*/
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);
        //采样器赋值，这个有点晕，0好像是默认的位置
        glUniform1i(texture,0);
        /**8.通知openGL绘制*/
        //以连续三顶点绘制三角形的方式绘制
        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
        /**解除绑定*/
        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0);
        glBindFramebuffer(GL_FRAMEBUFFER,0);
        return frameBufferTextures[0];
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

}



///**
// * 相机滤镜，只负责把相机数据渲染到FBO上
// */
//public class CameraFilter extends BaseFilter{
//
//    private int[] frameBuffers;       //FBO id数组
//    private int[] frameBufferTextures;//FBO的纹理id数组
//    private float[] matrix;  //变换矩阵
//
//    public CameraFilter(Context context) {
//        super(context, R.raw.camera_vertex_shader, R.raw.camera_fragment_shader);
//    }
//
//    @Override
//    public void onReady(int width, int height) {
//        super.onReady(width, height);
//        //创建FBO(一个虚拟的屏幕，并不会显示出来，可以用来操作各种滤镜效果)
//        frameBuffers = new int[1];
//        glGenFramebuffers(frameBuffers.length,frameBuffers,0);
//        //创建属于FBO的纹理，这里的纹理需要配置
//        frameBufferTextures = new int[1];
//        TextureHelper.genTextures(frameBufferTextures);
//        //让FBO与其纹理相绑定
//        glBindTexture(GL_TEXTURE_2D,frameBufferTextures[0]);
//        glBindFramebuffer(GL_FRAMEBUFFER,frameBuffers[0]);
//
//        //纹理允许着色器读取图像阵列的元素。
//        //要定义纹理图像，请调用glTexImage2D
//        glTexImage2D(
//                GL_TEXTURE_2D,    //此纹理是一个2D纹理
//                0,          //代表图像的详细程度, 默认为0即可
//                GL_RGBA,          //指定纹理的内部格式
//                width,            //纹理的宽度
//                height,           //纹理的高度
//                0,        //边框的值
//                GL_RGBA,          //指定纹理数据的格式，必须匹配上面的格式
//                GL_UNSIGNED_BYTE, //组成图像的数据是无符号字节类型
//                null       //指定一个指向内存中图像数据的指针。
//        );
//        //将纹理图像附加到帧缓冲对象
//        glFramebufferTexture2D(
//                GL_FRAMEBUFFER,          //指定帧缓冲目标。 符号常量必须是GL_FRAMEBUFFER。
//                GL_COLOR_ATTACHMENT0,    //指定应附加纹理图像的附着点。
//                GL_TEXTURE_2D,           //指定纹理目标。
//                frameBufferTextures[0],  //指定要附加图像的纹理对象。
//                0                  //指定要附加的纹理图像的mipmap级别，该级别必须为0。
//        );
//
//        glBindFramebuffer(GL_FRAMEBUFFER,0);
//        glBindTexture(GL_TEXTURE_2D,0);
//    }
//
//    /**
//     * 绘制
//     * @param textureId 摄像头纹理id
//     * @return
//     */
//    @Override
//    public int onDrawFrame(int textureId) {
//        /**0.绑定FBO否则会渲染到屏幕上*/
//        glBindFramebuffer(GL_FRAMEBUFFER,frameBuffers[0]);
//        /**1.设置视窗大小*/
//        glViewport(0,0,width,height);
//        /**2.使用着色器程序*/
//        glUseProgram(programId);
//        /**3.顶点坐标赋值*/
//        //因为要是从第0个开始取，所有设置position为零，相当于一个偏移
//        vertexCoordinateBuffer.position(0);
//        //顶点坐标赋值（attribute类型，这与源码中是对应的）
//        //p1：顶点坐标索引，p2：坐标维度（2D,相对与每次取2个数据为一个坐标），p3：数据类型
//        //p4：指定在访问定点数据值时是应将其标准化（GL_TRUE）还是直接转换为定点值（GL_FALSE）。
//        //p5：指定连续通用顶点属性之间的字节偏移量。 如果stride为0，则通用顶点属性被理解为紧密打包在数组中的。 初始值为0
//        //p6：指定指向数组中第一个通用顶点指针
//        glVertexAttribPointer(vertexCoordinate,2,GL_FLOAT,false,0,vertexCoordinateBuffer);
//        //激活
//        glEnableVertexAttribArray(vertexCoordinate);
//        /**4.纹理坐标赋值（attribute类型，这与源码中是对应的），原理参数同上*/
//        textureCoordinateBuffer.position(0);
//        glVertexAttribPointer(textureCoordinate,2,GL_FLOAT,false,0,textureCoordinateBuffer);
//        //激活
//        glEnableVertexAttribArray(textureCoordinate);
//        /**5.变化矩阵赋值（uniform类型，这与源码中是对应的）*/
//        //p1：矩阵索引，p2：被赋值的Uniform数量（可以是数组所以可以有多个）
//        //p3：uniform变量赋值时该矩阵是否需要转置。因为我们使用的是glm定义的矩阵，因此不要进行转置
//        //p4：数据指针
//        //p5：偏移
//        glUniformMatrix4fv(transformMatrix,1,false,matrix,0);
//        /**6.激活图层*/
//        glActiveTexture(GL_TEXTURE0);
//        /**7.绑定纹理*/
//        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,textureId);
//        //采样器赋值，这个有点晕，0好像是默认的位置
//        glUniform1i(texture,0);
//        /**8.通知openGL绘制*/
//        //以连续三顶点绘制三角形的方式绘制
//        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
//        /**解除绑定*/
//        glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,0);
//        glBindFramebuffer(GL_FRAMEBUFFER,0);
//        return frameBufferTextures[0];
//    }
//
//    public void setMatrix(float[] matrix) {
//        this.matrix = matrix;
//    }
//
//    private void releaseFBO(){
//        if(frameBuffers != null){
//            glDeleteFramebuffers(1,frameBuffers,0);
//            frameBuffers = null;
//        }
//        if(frameBufferTextures != null){
//            glDeleteTextures(1,frameBufferTextures,0);
//            frameBufferTextures = null;
//        }
//    }
//
//    @Override
//    public void release() {
//        super.release();
//        releaseFBO();
//    }
//}
