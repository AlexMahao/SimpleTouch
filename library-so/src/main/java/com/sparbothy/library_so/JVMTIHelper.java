package com.sparbothy.library_so;


import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.util.Log;


import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;


public class JVMTIHelper {

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static void init(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ClassLoader classLoader = context.getClassLoader();
                Method findLibrary = ClassLoader.class.getDeclaredMethod("findLibrary", String.class);
                String hotfixSo = (String) findLibrary.invoke(classLoader, "simple_touch");
                String agentSoPath = copy(context, hotfixSo, "jvmti", "simple_touch.so");

                // 初始化relase模式
                NativeLib.initInReleaseMode();
                // 添加agent代理
                Debug.attachJvmtiAgent(agentSoPath, null, classLoader);

                NativeLib.initJvmti();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String copy(Context context, String source, String targetDir, String targetFile) throws IOException {
        File filesDir = context.getFilesDir();
        File jvmtiLibDir = new File(filesDir, targetDir);
        if (!jvmtiLibDir.exists()) {
            jvmtiLibDir.mkdirs();

        }
        File agentLibSo = new File(jvmtiLibDir, targetFile);
        if (agentLibSo.exists()) {
            agentLibSo.delete();
        }
        Files.copy(Paths.get(new File(source).getAbsolutePath()), Paths.get((agentLibSo).getAbsolutePath()));

        return agentLibSo.getAbsolutePath();
    }
}
