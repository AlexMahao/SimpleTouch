package com.spearbothy.touch.core;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author mahao
 * @date 2018/11/15 下午3:05
 */

public class JsonFactory {

    public static String toJson(List<Message> messagesList) {
        JsonPrintEntity data = new JsonPrintEntity();
        List<JsonPrintEntity.TouchLog> touchLogs = new ArrayList<>();
        String eventKey = "";
        for (int i = 0; i < messagesList.size(); i++) {
            Message message = messagesList.get(i);
            if (!message.getEvent().equals(eventKey)) {
                if (!TextUtils.isEmpty(eventKey)) {
                    data.put(eventKey, touchLogs);
                    touchLogs = new ArrayList<>();
                }
            }
            JsonPrintEntity.TouchLog touchLog = new JsonPrintEntity.TouchLog();
            touchLog.put(message.getClassName(), JsonPrintEntity.TouchLogDetail.getInstance(message));
            touchLogs.add(touchLog);
            eventKey = message.getEvent();
        }
        // 添加最后一个
        if (!TextUtils.isEmpty(eventKey)) {
            data.put(eventKey, touchLogs);
        }
        return JSON.toJSONString(data, true);
    }


    private static class JsonPrintEntity extends LinkedHashMap<String, List<JsonPrintEntity.TouchLog>> {

        public static class TouchLog extends LinkedHashMap<String, JsonPrintEntity.TouchLogDetail> {}

        public static class TouchLogDetail {
            private String className;
            private String method;
            private String event;
            private String direction;
            private Boolean result = null;

            public static TouchLogDetail getInstance(Message message) {
                TouchLogDetail detail = new TouchLogDetail();
                detail.setClassName(message.getClassName());
                detail.setDirection(message.isBefore() ? ">>" : "<<");
                detail.setEvent(message.getEvent());
                detail.setMethod(message.getMethodName());
                detail.setResult(message.getResult());
                return detail;
            }

            public String getClassName() {
                return className;
            }

            public void setClassName(String className) {
                this.className = className;
            }

            public String getMethod() {
                return method;
            }

            public void setMethod(String method) {
                this.method = method;
            }

            public String getEvent() {
                return event;
            }

            public void setEvent(String event) {
                this.event = event;
            }

            public String getDirection() {
                return direction;
            }

            public void setDirection(String direction) {
                this.direction = direction;
            }

            public Boolean getResult() {
                return result;
            }

            public void setResult(Boolean result) {
                this.result = result;
            }

            public void setResult(boolean result) {
                this.result = result;
            }

            @Override
            public String toString() {
                return "TouchLogDetail{" +
                        "className='" + className + '\'' +
                        ", method='" + method + '\'' +
                        ", event='" + event + '\'' +
                        ", direction='" + direction + '\'' +
                        ", result=" + result +
                        '}';
            }
        }

    }
}
