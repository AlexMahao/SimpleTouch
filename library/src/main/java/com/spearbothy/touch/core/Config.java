package com.spearbothy.touch.core;

/**
 * @author mahao 2018/11/22 上午10:39
 */

public class Config {

    // 输出的日志以极简模式输出
    private boolean isSimple = true;
    // 是否延迟打印日志，延迟打印日志会在触摸事件结束之后打印，并且具有去重功能
    private boolean isDelay = true;
    // 是否保留重复的，默认不保留
    private boolean isRepeat = false;
    // 是否写入到文件
    private boolean isPrint2File = true;

    public boolean isSimple() {
        return isSimple;
    }

    public Config setSimple(boolean simple) {
        isSimple = simple;
        return this;
    }

    public boolean isDelay() {
        return isDelay;
    }

    public Config setDelay(boolean delay) {
        isDelay = delay;
        return this;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public Config setRepeat(boolean repeat) {
        isRepeat = repeat;
        return this;
    }

    public boolean isPrint2File() {
        return isPrint2File;
    }

    public Config setPrint2File(boolean print2File) {
        isPrint2File = print2File;
        return this;
    }
}
