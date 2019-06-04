package com.spearbothy.touch.core.parser.entity;

/**
 * @author mahao
 * @date 2018/12/20 上午11:47
 */

public class ChartFlow {
    private int position;
    private String desc;
    private Direction direction;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "ChartFlow{" +
                "position=" + position +
                ", desc='" + desc + '\'' +
                ", direction=" + direction +
                '}';
    }

    public enum Direction {
        TOP_ENTER, TOP_EXIT, BOTTOM_ENTER, BOTTOM_EXIT, RIGHT_ENTER, RIGHT_EXIT, LEFT_ENTER, LEFT_EXIT
    }
}
