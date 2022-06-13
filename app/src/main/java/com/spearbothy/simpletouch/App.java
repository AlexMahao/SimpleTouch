package com.spearbothy.simpletouch;

import android.app.Application;
import android.os.Build;

import com.sparbothy.library_so.JVMTIHelper;
import com.spearbothy.touch.core.Config;
import com.spearbothy.touch.core.Touch;

/**
 * @author mahao
 * @date 2018/11/9 下午2:14
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
//        Touch.init(this, new Config().setSimple(false));
// 开发时再打开，演示时关闭即可
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            JVMTIHelper.init(this);
//        }
    }
}
