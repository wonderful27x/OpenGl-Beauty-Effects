package com.example.wyopenglproject.filter;

import android.content.Context;
import com.example.wyopenglproject.R;

/**
 * 屏幕绘制滤镜，只负责把FBO数据绘制到屏幕上显示出来
 * 注意：这里的纹理坐标不需要转换
 */
public class ScreenFilter extends BaseFilter{
    public ScreenFilter(Context context) {
        super(context, R.raw.base_vertex_shader, R.raw.base_fragment_shader);
    }
}
