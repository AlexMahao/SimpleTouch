package com.spearbothy.touch.core.print;

import android.util.Log;

import com.spearbothy.touch.core.Constants;
import com.spearbothy.touch.core.Message;
import com.spearbothy.touch.core.Touch;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mahao 2018/11/15 下午3:18
 */

public class ConsolePrint implements Print {

    @Override
    public void printMessage(Message message) {
        if (Touch.sConfig.isSimple()) {
            Log.i(Constants.LOG_TAG, message.getSimplePrintMessage());
        } else {
            Log.i(Constants.LOG_TAG, message.getPrintMessage());
        }
    }

    @Override
    public void printMultipleMessage(List<Message> messages) {
        for (Message message : messages) {
            printMessage(message);
        }
    }
}
