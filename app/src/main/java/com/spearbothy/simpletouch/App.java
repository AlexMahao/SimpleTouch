package com.spearbothy.simpletouch;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.sparbothy.library_so.JVMTIHelper;
import com.sparbothy.library_so.NativeCallbackHelper;
import com.spearbothy.touch.core.Config;
import com.spearbothy.touch.core.Touch;

/**
 * @author mahao
 * @date 2018/11/9 下午2:14
 */

public class App extends Application implements NativeCallbackHelper.ExceptionCallback {

    @Override
    public void onCreate() {
        super.onCreate();
//        Touch.init(this, new Config().setSimple(false));
// 开发时再打开，演示时关闭即可
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            JVMTIHelper.init(this);
        }

        NativeCallbackHelper.addExceptionCallback(this);
    }

    @Override
    public void onException(Object exception) {
        if (exception instanceof Throwable) {
            Log.e("simple_touch", "异常监控", (Throwable) exception);
        }
    }
}
