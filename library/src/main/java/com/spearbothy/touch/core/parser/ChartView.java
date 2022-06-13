package com.spearbothy.touch.core.parser;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.spearbothy.touch.core.parser.entity.ChartBlock;
import com.spearbothy.touch.core.parser.entity.ChartFlow;
import com.spearbothy.touch.core.parser.entity.ChartGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mahao
 */

public class ChartView extends View {

    public static final int MAX_UNIT = 320;
    public static final int HORIZONTAL_COUNT = 3;

    private int unit;

    private List<ChartGroup> groups = new ArrayList<>();

    private int blockHeight;

    private int blockWidth;

    private int flowLength;

    private int width;

    private int height;

    private int horizontalSpace;

    private int flowPadding;

    private int titleTextSize;

    private Paint rectPaint;

    private TextPaint textPaint;

    private int arrowLength;

    private TextPaint groupTextPaint;
    private Paint groupPaint;
    private int groupVerticalPadding;

    public ChartView(Context context) {
        this(context, null);
    }

    public ChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initUnit(context);
        initPaint();
    }

    private void initPaint() {
        groupTextPaint = new TextPaint();
        groupTextPaint.setColor(Color.GRAY);
        groupTextPaint.setTextSize(titleTextSize);
        groupTextPaint.setAntiAlias(true);

        groupPaint = new Paint();
        groupPaint.setColor(Color.GRAY);
        groupPaint.setAntiAlias(true);
        groupPaint.setStyle(Paint.Style.STROKE);
        groupPaint.setStrokeWidth(unit);

        rectPaint = new Paint();
        rectPaint.setColor(Color.BLACK);
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(unit);

        textPaint = new TextPaint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(titleTextSize);
        textPaint.setAntiAlias(true);
    }

    private void initUnit(Context context) {
        width = context.getResources().getDisplayMetrics().widthPixels;
        unit = width / MAX_UNIT;
        blockHeight = 40 * unit;
        horizontalSpace = 15 * unit;
        flowLength = 50 * unit;
        flowPadding = 3 * unit;
        titleTextSize = 7 * unit;
        arrowLength = 4 * unit;
        groupVerticalPadding = 20 * unit;
        blockWidth = (width - horizontalSpace * 2 - flowLength * 2) / HORIZONTAL_COUNT;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (groups == null) {
            height = 0;
        } else {
            height = groups.size() * (blockHeight + flowLength);
        }
        if (height != 0) {
            height += flowLength;
        }
        setMeasuredDimension(width, height);
    }

    public void setData(List<ChartGroup> data) {
        if (data != null) {
            groups.clear();
            groups.addAll(data);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int y = 0;
        for (int i = 0; i < groups.size(); i++) {
            ChartGroup group = groups.get(i);
            drawGroup(y, group, canvas);
            y += blockHeight + flowLength;
        }
    }

    private void drawGroup(int y, ChartGroup group, Canvas canvas) {
        canvas.drawRect(horizontalSpace / 2,
                y + flowLength - groupVerticalPadding,
                width - horizontalSpace / 2,
                y + blockHeight + flowLength + groupVerticalPadding,
                groupPaint);
        drawText(group.getTitle(),
                horizontalSpace,
                y + flowLength - groupVerticalPadding + unit,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                groupTextPaint,
                canvas);
        drawText(group.getDesc(),
                horizontalSpace,
                y + flowLength - groupVerticalPadding + unit * 2 + titleTextSize,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                groupTextPaint,
                canvas);
        for (int i = 0; i < group.getBlocks().size(); i++) {
            ChartBlock chartBlock = group.getBlocks().get(i);
            drawBlock(y + flowLength, chartBlock, canvas);
        }
    }

    private void drawBlock(int y, ChartBlock block, Canvas canvas) {
        int x = 0;
        switch (block.getHorizontal()) {
            case LEFT:
                x += horizontalSpace;
                break;
            case CENTER:
                x += horizontalSpace + blockWidth + flowLength;
                break;
            case RIGHT:
                x += horizontalSpace + blockWidth * 2 + flowLength * 2;
                break;
            default:
                break;
        }
        canvas.drawRect(x, y, x + blockWidth, y + blockHeight, rectPaint);
        drawText(block.getTitle(), x, y, blockWidth, Layout.Alignment.ALIGN_CENTER, textPaint, canvas);
        drawText(block.getDesc(), x, y + titleTextSize + 2 * unit, blockWidth, Layout.Alignment.ALIGN_CENTER, textPaint, canvas);

        for (int i = 0; i < block.getFlows().size(); i++) {
            drawFlow(x, y, block.getFlows().get(i), canvas);
        }
    }

    public void drawText(String text, int x, int y, int width, Layout.Alignment alignment, TextPaint paint, Canvas canvas) {
        if (!TextUtils.isEmpty(text)) {
            canvas.save();
            canvas.translate(x, y);
            StaticLayout layout = new StaticLayout(text, paint, width, alignment, 1, 0, false);
            layout.draw(canvas);
            canvas.restore();
        }
    }

    public void drawHorizontalFlowText(String text, int x, int y, int maxWidth, Canvas canvas) {
        StaticLayout layout = new StaticLayout(text, textPaint, maxWidth, Layout.Alignment.ALIGN_CENTER, 1, 0, false);
        int height = layout.getHeight();
        canvas.save();
        canvas.translate(x, y - height - 2 * unit);
        layout.draw(canvas);
        canvas.restore();
    }

    public void drawVerticalFlowText(String text, int x, int y, int maxHeight, Canvas canvas) {
        StaticLayout layout = new StaticLayout(text, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1, 0, false);
        int height = layout.getHeight();
        canvas.save();
        canvas.translate(x + 2 * unit, y + (maxHeight - height) / 2);
        layout.draw(canvas);
        canvas.restore();
    }

    private void drawFlow(int x, int y, ChartFlow flow, Canvas canvas) {
        int flowRealWidth = flowLength - flowPadding * 2;
        String text = String.valueOf(flow.getPosition());
        if (!TextUtils.isEmpty(flow.getDesc())) {
            text += " " + flow.getDesc();
        }
        switch (flow.getDirection()) {
            case TOP_ENTER:
                x += blockWidth / 4;
                y += flowPadding - flowLength;
                drawVerticalFlowText(text, x, y, flowRealWidth, canvas);
                drawArrow(x, y, x, y + flowRealWidth, false, canvas);
                break;
            case TOP_EXIT:
                x += blockWidth / 4 * 3;
                y -= flowPadding;
                drawVerticalFlowText(text, x, y - flowRealWidth, flowRealWidth, canvas);
                drawArrow(x, y, x, y - flowRealWidth, false, canvas);
                break;
            case LEFT_ENTER:
                x += flowPadding - flowLength;
                y += blockHeight / 4;
                drawHorizontalFlowText(text, x, y, flowRealWidth, canvas);
                drawArrow(x, y, x + flowRealWidth, y, true, canvas);
                break;
            case LEFT_EXIT:
                x -= flowPadding;
                y += blockHeight / 4 * 3;
                drawHorizontalFlowText(text, x - flowRealWidth, y, flowRealWidth, canvas);
                drawArrow(x, y, x - flowRealWidth, y, true, canvas);
                break;
            case RIGHT_ENTER:
                x += blockWidth + flowPadding + flowRealWidth;
                y += blockHeight / 4;

                drawHorizontalFlowText(text, x - flowRealWidth, y, flowRealWidth, canvas);
                drawArrow(x, y, x - flowRealWidth, y, true, canvas);
                break;
            case RIGHT_EXIT:
                x += blockWidth + flowPadding;
                y += blockHeight / 4 * 3;

                drawHorizontalFlowText(text, x, y, flowRealWidth, canvas);
                drawArrow(x, y, x + flowRealWidth, y, true, canvas);
                break;
            // 不存在下面的情况
//            case BOTTOM_ENTER:
//                x += blockWidth / 4;
//                y += blockHeight + flowPadding + flowRealWidth;
//                drawArrow(x, y, x, y - flowRealWidth, false, canvas);
//                break;
//            case BOTTOM_EXIT:
//                x += blockWidth / 4;
//                y += blockHeight + flowPadding;
//                drawArrow(x, y, x, y + flowRealWidth, false, canvas);
//                break;
            default:
                break;
        }
    }

    private void drawArrow(int startX, int startY, int endX, int endY, boolean isHorizontal, Canvas canvas) {
        canvas.drawLine(startX, startY, endX, endY, rectPaint);
        if (isHorizontal) {
            if (endX > startX) {
                canvas.drawLine(endX - arrowLength, endY - arrowLength, endX, endY, rectPaint);
                canvas.drawLine(endX - arrowLength, endY + arrowLength, endX, endY, rectPaint);
            } else {
                canvas.drawLine(endX + arrowLength, endY - arrowLength, endX, endY, rectPaint);
                canvas.drawLine(endX + arrowLength, endY + arrowLength, endX, endY, rectPaint);
            }
        } else {
            if (endY > startY) {
                canvas.drawLine(endX - arrowLength, endY - arrowLength, endX, endY, rectPaint);
                canvas.drawLine(endX + arrowLength, endY - arrowLength, endX, endY, rectPaint);
            } else {
                canvas.drawLine(endX - arrowLength, endY + arrowLength, endX, endY, rectPaint);
                canvas.drawLine(endX + arrowLength, endY + arrowLength, endX, endY, rectPaint);
            }
        }
    }
}
