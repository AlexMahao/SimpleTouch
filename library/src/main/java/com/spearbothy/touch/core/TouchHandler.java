package com.spearbothy.touch.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author mahao
 * @date 2018/11/13 下午2:20
 */

public class TouchHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TouchMessageManager.getInstance().printBefore(proxy, method, args);
        Object result = ViewProxyBuilder.callSuper(proxy, method, args);
        TouchMessageManager.getInstance().printAfter(proxy, method, args ,result);
        return result;
    }
}
