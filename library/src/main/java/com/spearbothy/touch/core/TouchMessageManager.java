package com.spearbothy.touch.core;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mahao
 * @date 2018/11/13 下午2:32
 */

class TouchMessageManager {

    private static final String ACTION_DOWN = "ACTION_DOWN";
    private static final String ACTION_UP = "ACTION_UP";
    private static final long CLEAR_DELAY = 1000L;

    private List<Message> messagesList = new ArrayList<>();

    private Clear clear = new Clear();
    private Handler handler = new Handler();

    private Print mConsolePrint = new ConsolePrint();
    private Print mFilePrint = new FilePrint();

    private static TouchMessageManager sInstance;

    private TouchMessageManager() {}

    public static TouchMessageManager getInstance() {
        if (sInstance == null) {
            sInstance = new TouchMessageManager();
        }
        return sInstance;
    }

    void printBefore(Object proxy, Method method, Object[] args) {
        if (isBelongContentView((View) proxy)) {
            Message message = buildMessage(proxy, method, args);
            message.setBefore(true);
            addMessage(message);
        }
    }

    void printAfter(Object proxy, Method method, Object[] args, Object result) {
        if (isBelongContentView((View) proxy)) {
            Message message = buildMessage(proxy, method, args);
            message.setBefore(false);
            message.setResult((Boolean) result);
            addMessage(message);
        }
    }

    private void addMessage(Message message) {
        messagesList.add(message);
        mConsolePrint.printMessage(message);
        handler.removeCallbacks(clear);
        handler.postDelayed(clear, CLEAR_DELAY);
    }

    private boolean isBelongContentView(View view) {
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            View parentView = (View) parent;
            if (parentView.getId() == android.R.id.content) {
                return true;
            } else {
                return isBelongContentView(parentView);
            }
        }
        return false;
    }

    private static Message buildMessage(Object proxy, Method method, Object[] args) {

        String className = proxy.getClass().getSuperclass().getSimpleName();
        String methodName = method.getName();
        String eventStr = "";
        Object arg = args[0];
        if (arg instanceof MotionEvent) {
            MotionEvent event = (MotionEvent) arg;
            eventStr = MotionEvent.actionToString(event.getAction());
        }
        Message message = new Message(className, methodName, eventStr);
        message.setViewToken(proxy.hashCode());
        return message;
    }

    private void clearMessage() {
        if (messagesList.isEmpty()) {
            return;
        }
        List<Message> writeMessageList = new ArrayList<>(messagesList);

        mFilePrint.printMultipleMessage(writeMessageList);

        messagesList.clear();
    }

    private class Clear implements Runnable {

        @Override
        public void run() {
            clearMessage();
        }
    }
}
