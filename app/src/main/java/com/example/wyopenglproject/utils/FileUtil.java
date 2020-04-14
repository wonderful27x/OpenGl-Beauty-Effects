package com.example.wyopenglproject.utils;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static String copyAssets2SDCard(Context context, String srcName) {
        File file = new File(context.getExternalFilesDir("") + File.separator + srcName);
        try {
            if (!file.exists()) {
                InputStream is = context.getAssets().open(srcName);
                FileOutputStream fos = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[2048];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                is.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }
}
