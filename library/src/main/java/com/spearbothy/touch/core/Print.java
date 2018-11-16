package com.spearbothy.touch.core;

import java.util.List;

/**
 * @author mahao
 * @date 2018/11/15 下午3:19
 */

public interface Print {

    void printMessage(Message message);

    void printMultipleMessage(List<Message> messages);
}
