//美颜着色器
//https://github.com/wuhaoyu1990/MagicCamera/blob/master/Project-AndroidStudio/magicfilter/src/main/res/raw/beauty.glsl
//设置float为低等精度
precision lowp float;
//从顶点着色器传过来的参数（这里是坐标）
varying vec2 coordinate;
//采样器
uniform sampler2D texture;
//纹理宽高
uniform int textureWidth;
uniform int textureHeight;

//高斯模糊取点
vec2 blurCoordinates[20];

void main(){
    //1.高斯模糊
    //这得到的大概是单位向量的意思
    vec2 singleStepOffset = vec2(1.0/float(textureWidth),1.0/float(textureHeight));
    //采集20个点
    blurCoordinates[0] = coordinate.xy + singleStepOffset * vec2(0.0,-10.0);
    blurCoordinates[1] = coordinate.xy + singleStepOffset * vec2(0.0, 10.0);
    blurCoordinates[2] = coordinate.xy + singleStepOffset * vec2(-10.0, 0.0);
    blurCoordinates[3] = coordinate.xy + singleStepOffset * vec2(10.0, 0.0);
    blurCoordinates[4] = coordinate.xy + singleStepOffset * vec2(5.0, -8.0);
    blurCoordinates[5] = coordinate.xy + singleStepOffset * vec2(5.0, 8.0);
    blurCoordinates[6] = coordinate.xy + singleStepOffset * vec2(-5.0, 8.0);
    blurCoordinates[7] = coordinate.xy + singleStepOffset * vec2(-5.0, -8.0);
    blurCoordinates[8] = coordinate.xy + singleStepOffset * vec2(8.0, -5.0);
    blurCoordinates[9] = coordinate.xy + singleStepOffset * vec2(8.0, 5.0);
    blurCoordinates[10] = coordinate.xy + singleStepOffset * vec2(-8.0, 5.0);
    blurCoordinates[11] = coordinate.xy + singleStepOffset * vec2(-8.0, -5.0);
    blurCoordinates[12] = coordinate.xy + singleStepOffset * vec2(0.0, -6.0);
    blurCoordinates[13] = coordinate.xy + singleStepOffset * vec2(0.0, 6.0);
    blurCoordinates[14] = coordinate.xy + singleStepOffset * vec2(6.0, 0.0);
    blurCoordinates[15] = coordinate.xy + singleStepOffset * vec2(-6.0, 0.0);
    blurCoordinates[16] = coordinate.xy + singleStepOffset * vec2(-4.0, -4.0);
    blurCoordinates[17] = coordinate.xy + singleStepOffset * vec2(-4.0, 4.0);
    blurCoordinates[18] = coordinate.xy + singleStepOffset * vec2(4.0, -4.0);
    blurCoordinates[19] = coordinate.xy + singleStepOffset * vec2(4.0, 4.0);

    //当前点的像素值
    vec4 current = texture2D(texture,coordinate);
    vec3 rgb = current.rgb;
    //求和并计算平均值得到高斯模糊
    for(int i=0; i<20; i++){
        rgb += texture2D(texture,blurCoordinates[i].xy).rgb;
    }
    vec4 blur = vec4(rgb / 21.0,current.a);

//    gl_FragColor = blur;

    //2.高反差图 = 原图 - 高斯模糊
    vec4 heightContrast = current - blur;
    //设置高反差强度系数 clamp() : 夹具函数，求中间值
    heightContrast.r = clamp(2.0 * heightContrast.r * heightContrast.r * 24.0,0.0,1.0);
    heightContrast.g = clamp(2.0 * heightContrast.g * heightContrast.g * 24.0,0.0,1.0);
    heightContrast.b = clamp(2.0 * heightContrast.b * heightContrast.b * 24.0,0.0,1.0);

//    gl_FragColor = heightContrast;

    //3.磨皮
    //取蓝色分量
    float blue = min(current.b,blur.b);
    float value = clamp((blue-0.2)*5.0,0.0,1.0);
    //取rgb最大值
    float maxChannel = max(max(heightContrast.r,heightContrast.g),heightContrast.b);
    //磨皮强度
    float strength = 1.0;
    float currentStrength = (1.0-maxChannel/(maxChannel+0.2))*value*strength;
    //线性混合
    vec3 lineMix = mix(current.rgb,blur.rgb,currentStrength);

    gl_FragColor = vec4(lineMix, current.a);
}