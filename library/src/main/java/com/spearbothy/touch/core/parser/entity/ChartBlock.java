package com.spearbothy.touch.core.parser.entity;

import java.util.ArrayList;

/**
 * @author mahao
 */

public class ChartBlock {
    private String title; // 每一个group中唯一id
    private String desc;
    private Horizontal horizontal;
    private ArrayList<ChartFlow> flows = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public ArrayList<ChartFlow> getFlows() {
        return flows;
    }

    public void setFlows(ArrayList<ChartFlow> flows) {
        this.flows = flows;
    }

    public Horizontal getHorizontal() {
        return horizontal;
    }

    public void setHorizontal(Horizontal horizontal) {
        this.horizontal = horizontal;
    }

    @Override
    public String toString() {
        return "ChartBlock{" +
                "title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", horizontal=" + horizontal +
                ", flows=" + flows +
                '}';
    }

    public enum Horizontal {
        LEFT, CENTER, RIGHT
    }
}
