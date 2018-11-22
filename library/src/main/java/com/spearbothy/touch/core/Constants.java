package com.spearbothy.touch.core;

/**
 * @author mahao 2018/11/22 上午11:47
 */

public class Constants {
    public static final String DEX_CACHE_DIR = "proxy";

    public static final String LOG_TAG = "SimpleTouch";

    public static final String DISPATCH_TOUCH_EVENT = "dispatchTouchEvent";
    public static final String ON_TOUCH_EVENT = "onTouchEvent";
    public static final String ON_INTERCEPT_TOUCH_EVENT = "onInterceptTouchEvent";

    public static String[] PROXY_METHODS = {
            DISPATCH_TOUCH_EVENT,
            ON_TOUCH_EVENT,
            ON_INTERCEPT_TOUCH_EVENT
    };
}
