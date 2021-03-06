package com.spearbothy.touch.core;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mahao 2018/11/9 上午10:35
 */

public class ViewFactory implements LayoutInflater.Factory2 {

    private static final String TAG = ViewFactory.class.getSimpleName();

    private LayoutInflater.Factory2 mViewCreateFactory;

    private static final Class<?>[] sConstructorSignature = new Class[]{Context.class, AttributeSet.class};
    private final Object[] mConstructorArgs = new Object[2];
    private static final Map<String, Constructor<? extends View>> sConstructorMap = new HashMap<>();

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = null;
        if (mViewCreateFactory != null) {
            view = mViewCreateFactory.onCreateView(parent, name, context, attrs);
        }
        if (view == null) {
            view = createViewFromTag(context, name, attrs);
        }
        if (view == null) {
            return null;
        }
        if ((view.getClass().getModifiers() & Modifier.FINAL) != 0) {
            return view;
        }
        return proxy(view, attrs);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    private static View proxy(final View view, AttributeSet attrs) {
        try {
            return ViewProxyBuilder.forClass(view.getClass())
                    .handler(new TouchHandler())
                    .dexCache(view.getContext().getDir(Constants.DEX_CACHE_DIR, Context.MODE_PRIVATE))
                    .constructorArgTypes(Context.class, AttributeSet.class)
                    .constructorArgValues(view.getContext(), attrs)
                    .addProxyMethod(Arrays.asList(Constants.PROXY_METHODS))
                    .build();
        } catch (IOException e) {
            return null;
        }
    }

    public void setInterceptFactory(LayoutInflater.Factory2 factory) {
        mViewCreateFactory = factory;
    }

    private View createViewFromTag(Context context, String name, AttributeSet attrs) {
        if (name.equals("view")) {
            name = attrs.getAttributeValue(null, "class");
        }
        try {
            mConstructorArgs[0] = context;
            mConstructorArgs[1] = attrs;

            if (-1 == name.indexOf('.')) {
                View view = null;
                if ("View".equals(name)) {
                    view = createView(context, name, "android.view.");
                }
                if (view == null) {
                    view = createView(context, name, "android.widget.");
                }
                if (view == null) {
                    view = createView(context, name, "android.webkit.");
                }
                return view;
            } else {
                return createView(context, name, null);
            }
        } catch (Exception e) {
            Log.w(Constants.LOG_TAG, "cannot create 【" + name + "】 : ");
            return null;
        } finally {
            mConstructorArgs[0] = null;
            mConstructorArgs[1] = null;
        }
    }

    private View createView(Context context, String name, String prefix) throws InflateException {
        Constructor<? extends View> constructor = sConstructorMap.get(name);
        try {
            if (constructor == null) {
                Class<? extends View> clazz = context.getClassLoader().loadClass(
                        prefix != null ? (prefix + name) : name).asSubclass(View.class);

                constructor = clazz.getConstructor(sConstructorSignature);
                sConstructorMap.put(name, constructor);
            }
            constructor.setAccessible(true);
            return constructor.newInstance(mConstructorArgs);
        } catch (Exception e) {
            Log.w(Constants.LOG_TAG, "cannot create 【" + name + "】 : ");
            return null;
        }
    }
}
