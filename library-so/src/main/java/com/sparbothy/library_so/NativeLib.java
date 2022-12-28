package com.sparbothy.library_so;

public class NativeLib {

    static {
        System.loadLibrary("simple_touch");
    }

    public static native void initInReleaseMode();

    public static native void initJvmti();
}