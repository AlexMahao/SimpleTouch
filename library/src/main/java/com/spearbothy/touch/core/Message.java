package com.spearbothy.touch.core;

/**
 * @author mahao
 * @date 2018/11/13 下午5:01
 */

public class Message {

    private String className;
    private String methodName;
    private String event;
    private boolean before;
    private Boolean result;
    private int viewToken;

    public Message(String className, String methodName, String event) {
        this.className = className;
        this.methodName = methodName;
        this.event = event;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public boolean isBefore() {
        return before;
    }

    public void setBefore(boolean before) {
        this.before = before;
    }

    public Boolean getResult() {
        return result;
    }

    public int getViewToken() {
        return viewToken;
    }

    public void setViewToken(int viewToken) {
        this.viewToken = viewToken;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getPrintMessage() {
        StringBuffer sb = new StringBuffer("class:" + className + " method:" + methodName + " event:" + event);
        if (isBefore()) {
            sb.append(" >>");
        } else {
            sb.append(" << ").append(result);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Message{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", event='" + event + '\'' +
                '}';
    }
}
