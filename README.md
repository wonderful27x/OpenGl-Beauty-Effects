## opengl美颜特效
* **功能**
	1. 大眼滤镜
	2. 美颜
	3. 贴图-猫耳朵
	4. 录制
* **知识点**
	1. 相机数据流
		* cameraId = CameraInfo.前摄/后摄
		* Camera.Open(cameraId)
		* setPreviewFormat(NV21)
		* setPreviewCallback获取预览yuv数据,送入opencv进行人脸检测
		* setPreviewTexture更新纹理到SurfaceTexture,渲染到FBO帧缓冲
	2. opencv人脸检测
		* 导入模型，创建级联分类器CascadeClassifier
		* 创建检测追踪器DetectionBasedTracker
		* 传入yuv byte数组，调用process进行人脸检测，得到人脸框
	3. 中科院人脸5点定位
		* 加载中科院5点人脸检测模型
		* 根据原图像和人脸框信息检测人脸5各关键点的坐标: 两个眼睛、鼻尖，左右嘴角
		* 保存信息，通过jni创建class对象，返回检测数据
	4. opengl渲染
		* surfaceTexture收到camera预览数据回调
		* glSurfaceView.requestRender请求渲染
		* updateTextImage更新纹理
		* 渲染到FBO帧缓冲
	5. 大眼睛算法
		* uniform传递眼睛坐标
		* 公式fs(r) = (1-(r/Rmax-1)^2)*a*r
		* r: 当前像素到眼睛的距离
		* Rmax: 最大作用半径
		* a: 放大系数
		* fs: 计算后得到的新的像素距离眼睛的距离
		* 通过这个公式: 输入当前像素坐标，得到一个转换后的像素坐标，对它进行采样输出颜色
	6. 美颜算法
		* 高斯模糊
		* 高反差 = 源图-高斯模糊
		* 磨皮
		* mix线性混合
	7. 贴图原理
		* 创建纹理Id
		* texImage2D将图片转成纹理
		* 开启图层混合glBendFunc
		* 根据人脸框位置设置贴图区域，glViewport
		* 渲染到屏幕上
	8. 录制原理
		* 创建egl环境
		* 创建MediaCodec，并将surface设置给opengl
		* 渲染编码
		* 通过MediaMuxer封装成mp4

