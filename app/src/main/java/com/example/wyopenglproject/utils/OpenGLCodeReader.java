package com.example.wyopenglproject.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 从文件中读出openGL的源码
 */
public class OpenGLCodeReader {

    public static String sourceReader(Context context,int id){
        StringBuilder builder = new StringBuilder();
        InputStream inputStream = context.getResources().openRawResource(id);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        try {
            String code;
            while ((code = bufferedReader.readLine()) != null){
                builder.append(code);
                builder.append('\n');
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}
