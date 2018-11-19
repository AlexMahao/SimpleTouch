package com.spearbothy.simpletouch;

import android.app.Application;

import com.spearbothy.touch.core.Touch;

/**
 * @author mahao
 * @date 2018/11/9 下午2:14
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Touch.init(this , false);
    }
}
