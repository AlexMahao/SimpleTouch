package com.spearbothy.touch.core;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.spearbothy.touch.core.print.ConsolePrint;
import com.spearbothy.touch.core.print.FilePrint;
import com.spearbothy.touch.core.print.Print;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mahao 2018/11/13 下午2:32
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
        if (!Touch.sConfig.isDelay()) {
            mConsolePrint.printMessage(message);
        }
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
        View view = (View) proxy;
        String className = view.getClass().getSuperclass().getSimpleName();
        String methodName = method.getName();
        String eventStr = "";
        Object arg = args[0];
        if (arg instanceof MotionEvent) {
            MotionEvent event = (MotionEvent) arg;
            eventStr = actionToString(event.getAction());
        }
        Message message = new Message(className, methodName, eventStr);
        message.setViewToken(view.hashCode());
        message.setId(getId(view));
        message.setAbsClassName(view.getClass().getSuperclass().getName());
        return message;
    }

    /**
     * from MotionEvent.actionToString()
     */
    public static String actionToString(int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                return "ACTION_DOWN";
            case MotionEvent.ACTION_UP:
                return "ACTION_UP";
            case MotionEvent.ACTION_CANCEL:
                return "ACTION_CANCEL";
            case MotionEvent.ACTION_OUTSIDE:
                return "ACTION_OUTSIDE";
            case MotionEvent.ACTION_MOVE:
                return "ACTION_MOVE";
            case MotionEvent.ACTION_HOVER_MOVE:
                return "ACTION_HOVER_MOVE";
            case MotionEvent.ACTION_SCROLL:
                return "ACTION_SCROLL";
            case MotionEvent.ACTION_HOVER_ENTER:
                return "ACTION_HOVER_ENTER";
            case MotionEvent.ACTION_HOVER_EXIT:
                return "ACTION_HOVER_EXIT";
            case MotionEvent.ACTION_BUTTON_PRESS:
                return "ACTION_BUTTON_PRESS";
            case MotionEvent.ACTION_BUTTON_RELEASE:
                return "ACTION_BUTTON_RELEASE";
        }
        int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                return "ACTION_POINTER_DOWN(" + index + ")";
            case MotionEvent.ACTION_POINTER_UP:
                return "ACTION_POINTER_UP(" + index + ")";
            default:
                return Integer.toString(action);
        }
    }

    public List<Message> filter(List<Message> messages) {
        List<Message> result = new ArrayList<>();
        // 去重操作
        List<Message> prev = new ArrayList<>();
        List<Message> next = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (message.isBefore() && Constants.DISPATCH_TOUCH_EVENT.equals(message.getMethodName())) {
                if (prev.isEmpty()) {
                    if (!next.isEmpty() && next.get(0).getViewToken() == message.getViewToken()) {
                        // 达到边界条件，重置数据
                        prepareNext(result, prev, next);
                    }
                } else {
                    if (prev.get(0).getViewToken() == message.getViewToken()) {
                        // 达到边界条件，重置数据
                        prepareNext(result, prev, next);
                    }
                }

            }
            next.add(message);
        }

        // 最后一次
        if (!prev.equals(next) && !next.isEmpty()) {
            // 如果相等，抛弃数据
            result.addAll(next);
        }
        return result;
    }

    public void prepareNext(List<Message> result, List<Message> prev, List<Message> next) {
        if (prev.equals(next)) {
            // 如果相等，抛弃数据
            next.clear();
        } else {
            result.addAll(next);
            prev.clear();
            prev.addAll(next);
            next.clear();
        }
    }

    public static String getId(View view) {
        if (view.getId() == 0xffffffff) return "no-id";
        else return view.getResources().getResourceName(view.getId());
    }

    private void clearMessage() {
        if (messagesList.isEmpty()) {
            return;
        }
        List<Message> printMessageList;
        if (Touch.sConfig.isRepeat()) {
            printMessageList = new ArrayList<>(messagesList);
        } else {
            printMessageList = filter(messagesList);
        }
        if (Touch.sConfig.isDelay()) {
            mConsolePrint.printMultipleMessage(printMessageList);
        }
        if (Touch.sConfig.isPrint2File()) {
            mFilePrint.printMultipleMessage(printMessageList);
        }
        messagesList.clear();
    }

    private class Clear implements Runnable {

        @Override
        public void run() {
            clearMessage();
        }
    }
}
