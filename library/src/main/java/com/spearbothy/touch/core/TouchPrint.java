package com.spearbothy.touch.core;

import android.util.Log;
import android.view.MotionEvent;

import java.lang.reflect.Method;

/**
 * @author mahao
 * @date 2018/11/13 下午2:32
 */

class TouchPrint {

    private TouchPrint() {}

    static void print(Object proxy, Method method, Object[] args) {
        Log.i(Touch.LOG_TAG, buildLog(proxy, method, args));
    }

    private static String buildLog(Object proxy, Method method, Object[] args) {
        String className = proxy.getClass().getSuperclass().getSimpleName();
        String methodName = method.getName();
        String eventStr = "";
        Object arg = args[0];
        if (arg instanceof MotionEvent) {
            MotionEvent event = (MotionEvent) arg;
            eventStr = MotionEvent.actionToString(event.getAction());

        }
        return "class:" + className + " method:" + methodName + " event:" + eventStr;
    }
}
