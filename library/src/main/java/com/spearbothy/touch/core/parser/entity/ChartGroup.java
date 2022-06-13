package com.spearbothy.touch.core.parser.entity;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * @author mahao
 */

public class ChartGroup {
    private int token; // 唯一id
    private String title;
    private String desc;
    private ArrayList<ChartBlock> blocks = new ArrayList<>();

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

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

    public ArrayList<ChartBlock> getBlocks() {
        return blocks;
    }

    public void setBlocks(ArrayList<ChartBlock> blocks) {
        this.blocks = blocks;
    }

    @Override
    public String toString() {
        return "ChartGroup{" +
                "token='" + token + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", blocks=" + blocks +
                '}';
    }
}
