package com.example.wyopenglproject;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class WonderfulGLSurfaceView extends GLSurfaceView {

    private WonderfulRender wonderfulRender;
    private SpeedMode speedMode;

    public WonderfulGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        //设置版本
        setEGLContextClientVersion(2);
        //设置自定义渲染器
        wonderfulRender = new WonderfulRender(this);
        setRenderer(wonderfulRender);
        //设置渲染模式-按需渲染
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        wonderfulRender.surfaceDestroyed();
    }

    public void startRecording(){
        float speed = 1.0f;
        if (speedMode == null){
            wonderfulRender.startRecording(speed);
            return;
        }
        switch (speedMode){
            case EXTRA_SLOW:
                speed = 0.3f;
                break;
            case SLOW:
                speed = 0.5f;
                break;
            case NORMAL:
                speed = 1.0f;
                break;
            case FAST:
                speed = 1.5f;
                break;
            case EXTRA_FAST:
                speed = 3.0f;
                break;
        }
        wonderfulRender.startRecording(speed);
    }

    public void stopRecording(){
        wonderfulRender.stopRecording();
    }

    public void setSpeedMode(SpeedMode speedMode){
        this.speedMode = speedMode;
    }

    public void enableBigEye(boolean enable){
        wonderfulRender.enableBigEye(enable);
    }

    /**
     * 贴纸滤镜开关
     * @param enable
     * @param stickSourceId 贴纸资源id
     */
    public void enableStick(final boolean enable,final int stickSourceId){
        wonderfulRender.enableStick(enable,stickSourceId);
    }

    public void enableBeauty(final boolean enable){wonderfulRender.enableBeauty(enable);}
}
