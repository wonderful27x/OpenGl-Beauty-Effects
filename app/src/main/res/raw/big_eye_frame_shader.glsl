//着色器：着色器(Shader)是运行在GPU上的小程序
//片元着色器：处理光、阴影、遮挡、环境等等对物体表面的影响，最终生成一副图像的小程序
//FBO中可以使用 Sampler2D
//设置float为中等精度
precision mediump float;
//从顶点着色器传过来的参数（这里是坐标）
//这个坐标应该指的是构成整个纹理区域的坐标，应该说是一个坐标集合，并不是单一的一个坐标（误解）
//然而上面这种理解似乎是错误的，按老师的解释是：这个坐标的确就是代表一个单一的坐标，只不过程序一直在运行，这个坐标会被多次赋值
//coordinate 对应的就是一个具体的坐标xy，这一点在下面的算法中得到证实
varying vec2 coordinate;
//采样器
uniform sampler2D texture;
//左眼坐标
uniform vec2 leftEye;
//右眼坐标
uniform vec2 rightEye;

//大眼滤镜的原理就是先找到眼睛的坐标，然后通过算法（这里有一个公式）将纹理放大
//E:\ANDROID\AndroidCode\WYOpenGLProject\files\warping-thesis.pdf
//fs(r) = (1-(r/Rmax-1)*(r/Rmax-1)*a)*r
//其中r为半径，即到眼睛的距离，Rmax为最大半径，可以理解为局部放大最大作用半径，一般取两眼距离的一半，超过则会重叠这很好理解
//a为一个系数，可以理解为放大系数
//这个函数的作用是根据当前像素坐标到眼睛的距离r,和局部放大最大作用半径Rmax，计算得到处理后的像素的坐标到眼睛的距离fsr
float fs(float r,float Rmax){
    float a = 0.4;
    return (1.0 - pow(r/Rmax - 1.0,2.0)*a) * r;
}

//计算新的坐标 coordinate：旧坐标 eye：眼睛坐标  Rmax：局部放大最大作用半径
vec2 calNewCoordinate(vec2 coordinate,vec2 eye,float Rmax){
    vec2 newCoordinate = coordinate;
    //计算半径 r: 坐标到眼睛距离
    float r = distance(coordinate,eye);
    float fsr = fs(r,Rmax);
    //只有在作用范围内的数据才处理
    //注意：r > 0.0f 这种和0比较的表达式在部分机型会报错
    if(r > 0.001 && r < Rmax){
        //新半径/旧半径 = 新坐标到眼睛距离/旧坐标到研究距离
        //fsr/r = distance(newCoordinate，eye)/distance(coordinate,eye)
        //fsr/r = (newCoordinate-eye)/(coordinate-eye)
        //newCoordinate = fsr/r * (coordinate-eye) + eye
        //这里fsr/r可以简化，但是为了理解暂且不这样做
        newCoordinate = fsr / r * (coordinate-eye) + eye;
    }
    return newCoordinate;
}
void main(){
    //采集指定坐标位置的纹理（2D）
    //gl_FragColor（内置变量）：vec4类型，表示片元着色器中颜色
    //最大作用范围为两眼距离一半
    float Rmax = distance(leftEye,rightEye) / 2.0;
    //由于最大作用范围的限制，同一个点不可能同时出现在左眼和右眼的放大区域，
    //因此下面的算法分别对左右眼计算，如果在范围内则处理，不在范围内返回的还是原来的坐标
    vec2 newCoordinate = calNewCoordinate(coordinate,leftEye,Rmax);//计算左眼采用点
    newCoordinate = calNewCoordinate(newCoordinate,rightEye,Rmax); //计算右眼采用点
    gl_FragColor = texture2D(texture,newCoordinate);
}