package com.example.wyopenglproject.utils;

import android.util.Log;

import static android.opengl.GLES20.*;
/**
 * 着色器工具类
 */
public class ShaderHelper {

    private static final String TAG = "ShaderHelper";

    /**
     * 编译着色器代码
     * @param type 着色器类型，顶点或片元
     * @param shaderSource 着色器源码
     * @return 返回着色器id,0表示失败
     */
    public static int compileShader(int type,String shaderSource){
        //创建着色器
        int shaderId = glCreateShader(type);
        if (shaderId == 0){
            Log.e(TAG, "创建着色器失败！");
            return 0;
        }
        //绑定源码
        glShaderSource(shaderId,shaderSource);
        //编译源码
        glCompileShader(shaderId);
        //获取编译状态
        int[] status = new int[1];
        glGetShaderiv(shaderId,GL_COMPILE_STATUS,status,0);
        if(status[0] == GL_FALSE){
            Log.e(TAG, "着色器源码编译失败！code: " + shaderSource);
            glDeleteShader(shaderId);
            return 0;
        }
        return shaderId;
    }

    /**
     * 编译顶点着色器
     * @param shaderSource
     * @return
     */
    public static int compileVertexShader(String shaderSource){
        return compileShader(GL_VERTEX_SHADER,shaderSource);
    }

    /**
     * 编译片元着色器
     * @param shaderSource
     * @return
     */
    public static int compileFragmentShader(String shaderSource){
        return compileShader(GL_FRAGMENT_SHADER,shaderSource);
    }

    /**
     * 将顶点和片元着色器链接到openGL程序中
     * @param vertexShaderId 顶点着色器id
     * @param fragmentShaderId 片元着色器id
     * @return 程序id，0表示链接失败
     */
    public static int linkProgram(int vertexShaderId,int fragmentShaderId){
        //创建程序
        int programId = glCreateProgram();
        if(programId == 0){
            Log.e(TAG, "创建程序失败！");
            return 0;
        }
        //绑定着色器id
        glAttachShader(programId,vertexShaderId);
        glAttachShader(programId,fragmentShaderId);
        //链接
        glLinkProgram(programId);
        //获取链接状态
        int[] status = new int[1];
        glGetProgramiv(programId,GL_LINK_STATUS,status,0);
        if (status[0] == GL_FALSE){
            Log.e(TAG, "程序链接失败！");
            glDeleteProgram(programId);
            return 0;
        }
        return programId;
    }

    /**
     * 验证程序
     * @param programId
     * @return
     */
    public static boolean validateProgram(int programId){
        glValidateProgram(programId);
        int[] status = new int[1];
        glGetProgramiv(programId,GL_VALIDATE_STATUS,status,0);
        return status[0] != 0;
    }
}
