package com.sparbothy.library_so;

public class NativeLib {

    // Used to load the 'library_so' library on application startup.
    static {
        System.loadLibrary("library_so");
    }

    /**
     * A native method that is implemented by the 'library_so' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}