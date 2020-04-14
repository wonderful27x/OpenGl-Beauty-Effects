package com.example.wyopenglproject.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import com.example.wyopenglproject.R;
import com.example.wyopenglproject.face.Face;
import com.example.wyopenglproject.utils.TextureHelper;
import static android.opengl.GLES20.*;


/**
 * 贴纸滤镜-贴猫耳朵等
 * 先准备一张贴纸bitmap，然后将bitmap转成纹理，再将纹理融合渲染到FBO
 * 注意：需要转换纹理坐标
 */
public class StickFilter extends BaseFrameFilter {

    private Face face;                 //人脸
    private int[] imageTextureId;      //贴纸纹理id
    private Bitmap image;              //贴纸图片

    public StickFilter(Context context,int stickSourceId) {
        super(context, R.raw.base_vertex_shader, R.raw.base_fragment_shader);
        imageTextureId = new int[1];
        if(stickSourceId != -1){
            image = BitmapFactory.decodeResource(context.getResources(),stickSourceId);
        }else {
            image = BitmapFactory.decodeResource(context.getResources(),R.drawable.ear_0);
        }

    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        //生成一个纹理
        TextureHelper.genTextures(imageTextureId);
        //将bitmap转成纹理
        glBindTexture(GL_TEXTURE_2D,imageTextureId[0]);
        GLUtils.texImage2D(GL_TEXTURE_2D,0,image,0);
        glBindTexture(GL_TEXTURE_2D,0);
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
        /**5....*/

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

        /**画贴纸，上面的操作是画原图，然后再画贴纸，相当于纹理的叠加*/
        drawStick();
        return frameBufferTextures[0];
    }

    private void drawStick(){
        //开启openGl图层混合模式，将贴纸纹理和原纹理融合
        glEnable(GL_BLEND);
        //设置混合因子
        //p1->GL_ONE: 原图因子，保留原图所有属性
        //p2->GL_ONE_MINUS_SRC_ALPHA: 目标因子，透明度等于1-原图透明度
        glBlendFunc(GL_ONE,GL_ONE_MINUS_SRC_ALPHA);

        //获取人脸坐标
        float x = face.getLandmarks()[0];
        float y = face.getLandmarks()[1];
        //坐标转换
        //这里关于坐标的理解仍然有些疑惑，TODO 下面是我自己的理解，有可能是错误的
        //camera中的宽高等于图像image的宽高，但是和glSurface的宽又不等，
        //这是打印的数据 camera width: 640 height: 480  ||  glSurface width: 1080 height: 1692
        //而这里的纹理坐标应该使用glSurface里的坐标，因此需要进行转换，转换的公式由课上老师的解释得出
        //设对应glSurface的横坐标为?,则 ? / glSurface.width = x / camera.width （纵坐标类似）
        //于是 ? = x / camera.width * glSurface.width
        x = x / face.getImageWidth() * width;
        y = y / face.getImageHeight() * height;

        //设置贴图视窗大小(画板多大: 宽应该等于人脸的宽，高等于原图高)
        //和上面类似设画板宽为 ? ,则 ? / glSurface.width = face.width / camera.width
        //于是 ? = face.width / camera.width * glSurface.width
        int winWidth = (int)((float)face.getFaceWidth() / face.getImageWidth() * width);
        int winHeight = image.getHeight();
        int faceHeight = (int)((float)face.getFaceHeight() / face.getImageHeight() * height);
        //注意上面的xy得到的是人脸框映射到glSurface上的坐标，我们要基于这个坐标画耳朵，
        //而耳朵的应该在人脸上面一点的位置，所以还需要调整坐标
        //TODO 不同的贴纸大小不一样，需要调
        glViewport((int)x, (int) (y - winHeight/2),winWidth,winHeight);

        /**1.绑定FBO否则会渲染到屏幕上*/
        glBindFramebuffer(GL_FRAMEBUFFER,frameBuffers[0]);
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
        /**5....*/

        /**6.激活图层*/
        glActiveTexture(GL_TEXTURE0);
        /**7.绑定纹理*/
        glBindTexture(GL_TEXTURE_2D,imageTextureId[0]);
        //采样器赋值，这个有点晕，0好像是默认的位置
        glUniform1i(texture,0);
        /**8.通知openGL绘制*/
        //以连续三顶点绘制三角形的方式绘制
        glDrawArrays(GL_TRIANGLE_STRIP,0,4);
        /**解除绑定*/
        glBindTexture(GL_TEXTURE_2D,0);
        glBindFramebuffer(GL_FRAMEBUFFER,0);
        //关闭混合模式
        glDisable(GL_BLEND);
    }

    @Override
    public void release() {
        super.release();
        image.recycle();
    }

    public void setFace(Face face) {
        this.face = face;
    }
}
