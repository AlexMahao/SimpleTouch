package com.spearbothy.touch.core;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import java.io.File;

/**
 * @author mahao
 * @date 2018/11/13 下午2:24
 */

public class Touch {

    static final String DEX_CACHE_DIR = "proxy";

    static final String LOG_TAG = "SimpleTouch";

    public static void init(Context context) {
        File dir = context.getDir(DEX_CACHE_DIR, Context.MODE_PRIVATE);
        dir.delete();
    }

    public static void inject(Context context) {
        LayoutInflater inflater;
        if (context instanceof Activity) {
            inflater = ((Activity) context).getLayoutInflater();
        } else {
            inflater = LayoutInflater.from(context);
        }
        ViewFactory factory = new ViewFactory();
        if (context instanceof AppCompatActivity) {
            final AppCompatDelegate delegate = ((AppCompatActivity) context).getDelegate();
            factory.setInterceptFactory(new LayoutInflater.Factory() {
                @Override
                public View onCreateView(String name, Context context, AttributeSet attrs) {
                    return delegate.createView(null, name, context, attrs);
                }
            });
        }
        inflater.setFactory(factory);
    }
}
