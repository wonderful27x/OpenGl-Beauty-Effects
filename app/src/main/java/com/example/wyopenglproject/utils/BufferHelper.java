package com.example.wyopenglproject.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BufferHelper {

    /**
     * 创建浮点型顶点坐标缓存数据
     * @param vertex 顶点坐标（包括xy）
     * @return
     */
    public static FloatBuffer createFloatBuffer(float[] vertex){
        FloatBuffer floatBuffer;
        //分配一块内存，不受GC影响
        //vertex是一个包含xy的浮点型数据，vertex.length * 4代表数据长度*每个数据字节数（float占四字节）
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertex.length * 4);
        //设置使用设备硬件的本地字节序，保证字节排序一致
        byteBuffer.order(ByteOrder.nativeOrder());
        //从ByteBuffer创建一个浮点缓冲区
        floatBuffer = byteBuffer.asFloatBuffer();
        //写入坐标数组
        floatBuffer.clear();
        floatBuffer.put(vertex);
        //设置默认读取位置，从第一个数据开始
        floatBuffer.position(0);
        return floatBuffer;
    }
}
