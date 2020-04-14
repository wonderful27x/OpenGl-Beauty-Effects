//着色器：着色器(Shader)是运行在GPU上的小程序
//片元着色器：处理光、阴影、遮挡、环境等等对物体表面的影响，最终生成一副图像的小程序
//FBO中可以使用 Sampler2D
//设置float为中等精度
precision mediump float;
//从顶点着色器传过来的参数（这里是坐标）
//这个坐标应该指的是构成整个纹理区域的坐标，应该说是一个坐标集合，并不是单一的一个坐标
//然而上面这种理解似乎是错误的，按老师的解释是：这个坐标的确就是代表一个单一的坐标，只不过程序一直在运行，这个坐标会被多次赋值
varying vec2 coordinate;
//采样器
uniform sampler2D texture;

void main(){
    //采集指定坐标位置的纹理（2D）
    //gl_FragColor（内置变量）：vec4类型，表示片元着色器中颜色
    //疑问：这里采集的纹理是一个像素吗
    gl_FragColor = texture2D(texture,coordinate);
}