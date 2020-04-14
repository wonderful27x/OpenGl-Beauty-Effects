package com.example.wyopenglproject.filter;

import android.content.Context;
import com.example.wyopenglproject.utils.BufferHelper;
import com.example.wyopenglproject.utils.OpenGLCodeReader;
import com.example.wyopenglproject.utils.ShaderHelper;
import static android.opengl.GLES20.*;
import java.nio.FloatBuffer;

/**
 * 滤镜基类
 */
public class BaseFilter {

    protected int vertexSourceId;   //顶点着色器程序id
    protected int fragmentSourceId; //片元着色器程序id

    protected int vertexShaderId;   //顶点着色器id
    protected int fragmentShaderId ;//片元着色器id
    protected int programId;        //程序id

    protected int vertexCoordinate; //顶点坐标索引
    protected int textureCoordinate;//纹理坐标索引
    protected int transformMatrix;  //变换矩阵索引
    protected int texture;          //采样器索引

    protected FloatBuffer vertexCoordinateBuffer; //顶点坐标
    protected FloatBuffer textureCoordinateBuffer;//纹理坐标

    protected int width;
    protected int height;

    public BaseFilter(Context context,int vertexSourceId, int fragmentSourceId){
        this.vertexSourceId = vertexSourceId;
        this.fragmentSourceId = fragmentSourceId;
        float[] VERTEX = {
                -1.0f,-1.0f,
                1.0f, -1.0f,
                -1.0f,1.0f,
                1.0f, 1.0f
        };
        //TODO 这个纹理坐标是个大坑，在最开始利用openGL直接将摄像头数据渲染到屏幕时，
        //TODO 需要逆时针旋转并镜像翻转，否则图像是倒的，但是加入离屏FBO后又不需要变换了，
        //TODO 最坑的是当加入大眼滤镜的时候又需要再次逆时针旋转并镜像翻转
        float[] TEXTURE = {
//                //逆时针旋转并镜像翻转
//                0.0f,0.0f,
//                1.0f,0.0f,
//                0.0f,1.0f,
//                1.0f,1.0f

                //和顶点坐标顺序一样
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        };
        vertexCoordinateBuffer = BufferHelper.createFloatBuffer(VERTEX);
        textureCoordinateBuffer = BufferHelper.createFloatBuffer(TEXTURE);

        init(context);
        changeTextureCoordinate();
    }

    //修改纹理坐标，有需求时子类可重写 TODO （这是一个大坑，尽量不要再基类重写这个方法）
    protected void changeTextureCoordinate(){}

    //初始化滤镜
    private void init(Context context){
        //读取顶点和片元着色器源码
        String vertexShaderSource = OpenGLCodeReader.sourceReader(context,vertexSourceId);
        String fragmentShaderSource = OpenGLCodeReader.sourceReader(context,fragmentSourceId);
        //编译顶点和片元着色器程序
        vertexShaderId = ShaderHelper.compileVertexShader(vertexShaderSource);
        fragmentShaderId = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        //链接程序
        programId = ShaderHelper.linkProgram(vertexShaderId,fragmentShaderId);
        //获取相关变量的索引
        vertexCoordinate = glGetAttribLocation(programId,"vertexCoordinate");  //顶点坐标
        textureCoordinate = glGetAttribLocation(programId,"textureCoordinate");//纹理坐标
        transformMatrix = glGetUniformLocation(programId,"transformMatrix");   //变换矩阵
        texture = glGetUniformLocation(programId,"texture");                   //采样器
    }

    public void onReady(int width,int height){
        this.width = width;
        this.height = height;
    }

    /**
     * 绘制并返回操作后的纹理id
     * @param textureId 纹理id
     * @return 返回纹理id
     */
    public int onDrawFrame(int textureId){
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
        //。。。相机才使用变化矩阵，基类不需要赋值
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
        return textureId;
    }

    public void release(){
        glDeleteShader(vertexShaderId);
        glDeleteShader(fragmentShaderId);
        glDeleteProgram(programId);
    }

}
