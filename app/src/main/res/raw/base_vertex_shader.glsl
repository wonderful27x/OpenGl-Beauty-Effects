//着色器：着色器(Shader)是运行在GPU上的小程序
//顶点着色器：处理顶点、法线等数据的小程序。

//顶点坐标,给其值赋确定要画的形状
//类型为 vec4 的原因:
//这个地方的点是 x,y,z 的坐标点, 那么想法上, 这个地方应该是可以直接用 3x3 的的 vec3 的向量保存
//但是由于这个地方需要和变换矩阵相乘, 有因为变换矩阵的类型就直接是 mat4 ,是 4 x 4的矩阵类型,
//又因为矩阵相乘的时候,能相乘的话 必须是 w x m , m X y 的矩阵, 也就是 一个矩阵的 行和另外一个矩阵的列相同.

//由此看来vec4代表的是储存一个坐标所需要的类型，
//而这个变量代表的应该是构成整个图形区域的坐标，应该说是一个坐标集合，并不是单一的一个坐标，
//然而上面这种理解似乎是错误的，按老师的解释是：这个坐标的确就是代表一个单一的坐标，只不过程序一直在运行，这个坐标会被多次赋值
//下面也是这个道理
attribute vec4 vertexCoordinate;
//纹理坐标，
attribute vec2 textureCoordinate;
//坐标，传给片元着色器
varying vec2 coordinate;

void main(){
    //gl_Position（内置变量）：vec4类型，表示顶点着色器中顶点位置，赋值后openGL就知道要画的形状了
    gl_Position = vertexCoordinate;
    //纹理坐标赋值
    coordinate = textureCoordinate;
}