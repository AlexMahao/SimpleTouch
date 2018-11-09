package com.spearbothy.simpletouch;

import android.content.Context;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.spearbothy.simpletouch.cglib.proxy.Enhancer;
import com.spearbothy.simpletouch.cglib.proxy.MethodInterceptor;
import com.spearbothy.simpletouch.cglib.proxy.MethodProxy;

/**
 * @author mahao
 * @date 2018/11/9 上午10:35
 */

public class TouchLayoutFactory implements LayoutInflater.Factory2 {
    private static final String TAG = TouchLayoutFactory.class.getSimpleName();
    AppCompatDelegate mDelegate;

    public TouchLayoutFactory(AppCompatDelegate delegate) {
        this.mDelegate = delegate;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        Log.i(TAG, name);
        View view = mDelegate.createView(parent, name, context, attrs);
        if (view == null) {
            Log.i(TAG, name + "is null");
            return null;
        }
        return proxy(view);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    private View proxy(final View view) {
        Enhancer enhancer = new Enhancer(view.getContext());
        enhancer.setSuperclass(View.class);
        enhancer.setInterceptor(new MethodInterceptor() {
            @Override
            public Object intercept(Object object, Object[] args, MethodProxy methodProxy) throws Exception {
                Log.i(TAG, "before:" + methodProxy.getMethodName());
                Object o = methodProxy.invokeSuper(object, args);
                Log.i(TAG, "after:" + methodProxy.getMethodName());
                return o;
            }
        });
        return (View) enhancer.create();
    }
}
