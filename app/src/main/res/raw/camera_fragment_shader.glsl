//着色器：着色器(Shader)是运行在GPU上的小程序
//片元着色器：处理光、阴影、遮挡、环境等等对物体表面的影响，最终生成一副图像的小程序
//安卓中不能直接使 Sampler2D（注意：这种情况是在将摄像头数据经openGL直接渲染到屏幕上，而没有经过FBO）,
//而是 samplerExternalOES，由于其不是openGL内部默认支持的，所以需要打开外部扩展
//https://www.jianshu.com/p/f1a86ac46b4d
#extension GL_OES_EGL_image_external : require
//设置float为中等精度
precision mediump float;
//从顶点着色器传过来的参数（这里是坐标）
//这个坐标应该指的是构成整个纹理区域的坐标，应该说是一个坐标集合，并不是单一的一个坐标
//然而上面这种理解似乎是错误的，按老师的解释是：这个坐标的确就是代表一个单一的坐标，只不过程序一直在运行，这个坐标会被多次赋值
varying vec2 coordinate;
//采样器
uniform samplerExternalOES texture;

void main(){
    //采集指定坐标位置的纹理（2D）
    //gl_FragColor（内置变量）：vec4类型，表示片元着色器中颜色
    //疑问：这里采集的纹理是一个像素吗
    gl_FragColor = texture2D(texture,coordinate);

//    //简单的滤镜可以直接操作像素
//    //黑白相机(305911 公式)
//    vec4 rgba = texture2D(texture,coordinate);
//    float gray = rgba.r * 0.30 + rgba.g * 0.59 + rgba.b * 0.11;
//    gl_FragColor = vec4(gray,gray,gray,rgba.a);

}