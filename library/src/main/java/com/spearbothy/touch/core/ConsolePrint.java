package com.spearbothy.touch.core;

import android.util.Log;

import java.util.List;

/**
 * @author mahao
 * @date 2018/11/15 下午3:18
 */

public class ConsolePrint implements Print {

    @Override
    public void printMessage(Message message) {
        Log.i(Touch.LOG_TAG, message.getPrintMessage());
    }

    @Override
    public void printMultipleMessage(List<Message> messages) {

    }
}
