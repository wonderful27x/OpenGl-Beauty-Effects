package com.example.wyopenglproject.record;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGLContext;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 屏幕录制模块，Android 4.1.2(API 16)提供了一套编解码API-MediaCodec，将数据放入输入缓冲区，
 * 从输出缓存区就可以取出编码后的数据。另外利用MediaCodec可以创建出一个surface，将图像绘制
 * 在这个surface上，也可以从输出缓冲区得到编码数据，这里的屏幕录制我们使用后者。
 * 关于MediaCodec的使用下面有两个链接
 * https://www.cnblogs.com/elesos/p/11777553.html
 * https://www.jianshu.com/p/30e596112015
 */
public class WonderfulMediaRecorder {

    private MediaCodec mediaCodec;    //编码器
    private Surface inputSurface;     //MediaCodec创建的surface
    private MediaMuxer mediaMuxer;    //封装器
    private Context context;          //android 上下文环境
    private EGLContext eglContext;    //EGL 上下文环境
    private int width;                //宽
    private int height;               //高
    private String outputPath;        //录屏输出路径
    private float speed;              //录屏视频的播放速度
    private Handler handler;
    private WonderfulEgl wonderfulEgl;//EGL环境
    private boolean recordRunning;

    public WonderfulMediaRecorder(Context context, EGLContext eglContext, int width, int height, String outputPath) {
        this.context = context;
        this.eglContext = eglContext;
        this.width = width;
        this.height = height;
        this.outputPath = outputPath;
    }

    /**
     * 开始录屏，标准初始化流程
     */
    public void start(float speed) throws IOException {
        this.speed = speed;
        /**
         * 1.创建编码器
         * MIMETYPE_VIDEO_AVC:高质量编码-h264
         */
        mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        /**
         * 2.配置编码器参数
         */
        //视频格式
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,width,height);
        //帧率
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,25);
        //设置码率,计算公式:像素(width * height) * 帧率 * Motion(图像等级) * 0.07
        int rate = (int) (width * height * 25 * 1 * 0.07);
        //化整
        rate = rate - rate % 1000 + 10000;
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE,rate);
        //颜色，从surface中自适应
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //关键帧间隔
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,20);
        //将参数设置编码器中
        mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
        /**
         * 3.创建surface
         */
        inputSurface = mediaCodec.createInputSurface();
        /**
         * 4.创建封装器
         * MUXER_OUTPUT_MPEG_4：MP4格式
         */
        mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        /**
         * 5.配置EGL环境
         */
        HandlerThread handlerThread = new HandlerThread("WonderfulMediaRecorder-Thread");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //创建EGL
                wonderfulEgl = new WonderfulEgl(context,eglContext,inputSurface,width,height);
                //开启编码器
                mediaCodec.start();
                recordRunning = true;
            }
        });
    }

    /**
     * 停止录制
     */
    public void stop(){
        recordRunning = false;
        if(handler != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    getEncodeData(true);
                    if(mediaCodec != null){
                        mediaCodec.stop();
                        mediaCodec.release();
                        mediaCodec = null;
                    }
                    if(mediaMuxer != null){
                        mediaMuxer.stop();
                        mediaMuxer.release();
                        mediaMuxer = null;
                    }
                    if(inputSurface != null){
                        inputSurface.release();
                        inputSurface = null;
                    }
                    if (wonderfulEgl != null){
                        wonderfulEgl.release();
                        wonderfulEgl = null;
                    }
                    handler.getLooper().quitSafely();
                    handler = null;
                }
            });
        }
    }

    /**
     * 渲染并从输出缓冲区取出编码数据
     */
    public void encodeFrame(final int textureId,final long timestamp){
        if (!recordRunning)return;
        if (handler != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //渲染
                    if(wonderfulEgl != null){
                        wonderfulEgl.draw(textureId,timestamp);
                    }
                    //取出编码数据
                    getEncodeData(false);
                }
            });
        }
    }

    /**
     * 从输出缓冲区取出编码数据并封装保存
     * @param endOfStream 结束标志
     */
    private void getEncodeData(boolean endOfStream){
        if (endOfStream){
            mediaCodec.signalEndOfInputStream();
        }
        //缓冲区关键信息
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int status;
        int index = 0;
        long lastTimeUs = -1;
        while (true){
            status = mediaCodec.dequeueOutputBuffer(bufferInfo,10_000);
            if(status == MediaCodec.INFO_TRY_AGAIN_LATER){
                if (!endOfStream)break;
            }else if(status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
                MediaFormat format = mediaCodec.getOutputFormat();
                index = mediaMuxer.addTrack(format);
                //启动封装器
                mediaMuxer.start();
            }
            //取到了编码数据
            else {
                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(status);
                if (outputBuffer == null){
                    throw new RuntimeException("getOutputBuffer fail!");
                }
                //如果是配置信息
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG )!= 0){
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0){
                    //控制播放速度（有些bug）
                    bufferInfo.presentationTimeUs = (long) (bufferInfo.presentationTimeUs/speed);
                    if (lastTimeUs != -1 && bufferInfo.presentationTimeUs <= lastTimeUs){
                        bufferInfo.presentationTimeUs = (long) (lastTimeUs + 1000_000/25/speed);
                    }
                    lastTimeUs = bufferInfo.presentationTimeUs;
                    //位置偏移
                    outputBuffer.position(bufferInfo.offset);
                    //可读写的最大位置
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    //写数据
                    mediaMuxer.writeSampleData(index,outputBuffer,bufferInfo);
                }
                //释放输出缓冲区
                mediaCodec.releaseOutputBuffer(status,false);
                //编码结束
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM )!= 0)break;
            }
        }
    }
}
