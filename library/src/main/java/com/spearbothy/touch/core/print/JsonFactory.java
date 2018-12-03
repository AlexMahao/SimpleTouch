package com.spearbothy.touch.core.print;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.spearbothy.touch.core.Constants;
import com.spearbothy.touch.core.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mahao 2018/11/15 下午3:05
 */

public class JsonFactory {

    public static String toJson(List<Message> messagesList) {
        JsonPrintEntity data = new JsonPrintEntity();
        JsonPrintEntity.TouchView rootTouchView = new JsonPrintEntity.TouchView();
        for (int i = 0; i < messagesList.size(); i++) {
            Message message = messagesList.get(i);
            if (message.getViewToken() == rootTouchView.getViewToken()
                    && message.isBefore()
                    && Constants.DISPATCH_TOUCH_EVENT.equals(message.getMethodName())
                    ) {
                data.add(rootTouchView);
                rootTouchView = new JsonPrintEntity.TouchView();
            }

            if (rootTouchView.getViewToken() == 0) {
                rootTouchView.init(message);
            }

            // calls 方法判断规则
            // 如果viewToken存在，则添加方法，如果不存在，则添加view and method
            JsonPrintEntity.TouchView currentTouchView = null;
            JsonPrintEntity.TouchView findTouchView = findTouchView(rootTouchView, message.getViewToken());

            if (findTouchView.getViewToken() == message.getViewToken()) {
                // viewToken已存在
                currentTouchView = findTouchView;
            } else {
                // viewToken不存在
                currentTouchView = new JsonPrintEntity.TouchView();
                currentTouchView.init(message);
                findTouchView.getCalls().add(currentTouchView);
            }

            // 添加方法
            JsonPrintEntity.TouchMethod touchMethod = new JsonPrintEntity.TouchMethod();
            touchMethod.setDirection(message.isBefore() ? ">>" : "<<");
            touchMethod.setEvent(message.getEvent());
            touchMethod.setMethodName(message.getMethodName());
            touchMethod.setResult(message.getResult());
            currentTouchView.getCalls().add(touchMethod);
        }
        if (rootTouchView.getViewToken() != 0) {
            data.add(rootTouchView);
        }

        return JSON.toJSONString(data, true);
    }

    public static JsonPrintEntity.TouchView findTouchView(JsonPrintEntity.TouchView root, int viewToken) {
        // 查询token
        if (root.getViewToken() == viewToken) {
            return root;
        }
        for (int i = 0; i < root.calls.size(); i++) {
            Object call = root.getCalls().get(i);
            if (call instanceof JsonPrintEntity.TouchView) {
                return findTouchView((JsonPrintEntity.TouchView) call, viewToken);
            }
        }
        return root;
    }

    private static class JsonPrintEntity extends ArrayList<JsonPrintEntity.TouchView> {

        public static class TouchView {

            @JSONField(ordinal = 0)
            private String className;
            @JSONField(ordinal = 5)
            private String absClassName;
            @JSONField(ordinal = 10)
            private String id;
            @JSONField(ordinal = 20, serialize = false)
            private int viewToken;
            @JSONField(ordinal = 30)
            private List<Object> calls = new ArrayList<>(); // 包含 TouchView 和 TouchMethod

            public String getAbsClassName() {
                return absClassName;
            }

            public void setAbsClassName(String absClassName) {
                this.absClassName = absClassName;
            }

            public void init(Message message) {
                setClassName(message.getClassName());
                setViewToken(message.getViewToken());
                setId(message.getId());
                setAbsClassName(message.getAbsClassName());
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                TouchView touchView = (TouchView) o;

                if (viewToken != touchView.viewToken) return false;
                if (className != null ? !className.equals(touchView.className) : touchView.className != null)
                    return false;
                if (absClassName != null ? !absClassName.equals(touchView.absClassName) : touchView.absClassName != null)
                    return false;
                if (id != null ? !id.equals(touchView.id) : touchView.id != null) return false;
                return calls != null ? calls.equals(touchView.calls) : touchView.calls == null;
            }

            @Override
            public int hashCode() {
                int result = className != null ? className.hashCode() : 0;
                result = 31 * result + (absClassName != null ? absClassName.hashCode() : 0);
                result = 31 * result + (id != null ? id.hashCode() : 0);
                result = 31 * result + viewToken;
                result = 31 * result + (calls != null ? calls.hashCode() : 0);
                return result;
            }

            public int getViewToken() {
                return viewToken;
            }

            public void setViewToken(int viewToken) {
                this.viewToken = viewToken;
            }

            public String getClassName() {
                return className;
            }

            public void setClassName(String className) {
                this.className = className;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public List<Object> getCalls() {
                return calls;
            }

            public void setCalls(List<Object> calls) {
                this.calls = calls;
            }

            @Override
            public String toString() {
                return "TouchView{" +
                        "className='" + className + '\'' +
                        ", id='" + id + '\'' +
                        ", viewToken=" + viewToken +
                        ", calls=" + calls +
                        '}';
            }
        }

        public static class TouchMethod {
            @JSONField(ordinal = 0)
            private String methodName;
            @JSONField(ordinal = 10)
            private String direction;
            @JSONField(ordinal = 20)
            private String event;
            @JSONField(ordinal = 30)
            private Boolean result;

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                TouchMethod that = (TouchMethod) o;

                if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null)
                    return false;
                if (direction != null ? !direction.equals(that.direction) : that.direction != null)
                    return false;
                if (event != null ? !event.equals(that.event) : that.event != null) return false;
                return result != null ? result.equals(that.result) : that.result == null;
            }

            @Override
            public int hashCode() {
                int result1 = methodName != null ? methodName.hashCode() : 0;
                result1 = 31 * result1 + (direction != null ? direction.hashCode() : 0);
                result1 = 31 * result1 + (event != null ? event.hashCode() : 0);
                result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
                return result1;
            }

            public String getMethodName() {
                return methodName;
            }

            public void setMethodName(String methodName) {
                this.methodName = methodName;
            }

            public String getDirection() {
                return direction;
            }

            public void setDirection(String direction) {
                this.direction = direction;
            }

            public String getEvent() {
                return event;
            }

            public void setEvent(String event) {
                this.event = event;
            }

            public Boolean getResult() {
                return result;
            }

            public void setResult(Boolean result) {
                this.result = result;
            }

            @Override
            public String toString() {
                return "TouchMethod{" +
                        "methodName='" + methodName + '\'' +
                        ", direction='" + direction + '\'' +
                        ", event='" + event + '\'' +
                        ", result=" + result +
                        '}';
            }
        }
    }
}
