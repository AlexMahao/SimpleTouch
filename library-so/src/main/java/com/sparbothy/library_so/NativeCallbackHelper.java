package com.sparbothy.library_so;

import java.util.concurrent.CopyOnWriteArrayList;

public class NativeCallbackHelper {

    private static CopyOnWriteArrayList<ExceptionCallback> exceptionCallbacks = new CopyOnWriteArrayList<>();

    public static void exception(Object exception) {
        for (ExceptionCallback callback : exceptionCallbacks) {
            callback.onException(exception);
        }
    }

    public static void addExceptionCallback(ExceptionCallback callback) {
        exceptionCallbacks.add(callback);
    }

    public static void removeExceptionCallback(ExceptionCallback callback) {
        exceptionCallbacks.remove(callback);
    }

    public interface ExceptionCallback {
        void onException(Object exception);
    }
}

