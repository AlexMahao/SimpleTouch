package com.spearbothy.touch.core;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.spearbothy.touch.core.parser.NotificationHelper;

import java.io.File;

/**
 * 默认简单模式
 *
 * @author mahao 2018/11/13 下午2:24
 */

public class Touch {

    public static String sHostPackage = "";

    public static Config sConfig;

    public static void init(Context context) {
        init(context, new Config());
    }

    public static void init(Context context, Config config) {
        if (!config.isProcess()) {
            return;
        }
        File dir = context.getDir(Constants.DEX_CACHE_DIR, Context.MODE_PRIVATE);
        dir.delete();

        sHostPackage = context.getPackageName();
        sConfig = config;

        NotificationHelper.init(context);
        NotificationHelper.sendNotificationForParser(context);
    }

    public static void inject(Context context) {
        if (sConfig == null || !sConfig.isProcess()) {
            return;
        }
        LayoutInflater inflater;
        if (context instanceof Activity) {
            inflater = ((Activity) context).getLayoutInflater();
        } else {
            inflater = LayoutInflater.from(context);
        }
        ViewFactory factory = new ViewFactory();
        if (context instanceof AppCompatActivity) {
            final AppCompatDelegate delegate = ((AppCompatActivity) context).getDelegate();
            factory.setInterceptFactory(new LayoutInflater.Factory2() {
                @Override
                public View onCreateView(String name, Context context, AttributeSet attrs) {
                    return delegate.createView(null, name, context, attrs);
                }

                @Override
                public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
                    return delegate.createView(parent, name, context, attrs);
                }
            });
        }
        inflater.setFactory2(factory);
    }
}
