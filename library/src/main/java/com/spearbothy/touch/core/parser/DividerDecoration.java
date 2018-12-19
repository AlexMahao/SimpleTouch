package com.spearbothy.touch.core.parser;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class DividerDecoration extends RecyclerView.ItemDecoration {
    private RecyclerView mList;
    private Drawable mDivider;
    private int mDividerHeight;
    private Paint mDividerPaint;
    private int mDividerMarginLeft;
    private int mDividerMarginRight;
    private int mDividerPaddingBottom;

    public DividerDecoration(RecyclerView list, int dividerColor, int dividerHeight) {
        mList = list;
        setDividerColorAndHeight(dividerColor, dividerHeight);
    }

    public DividerDecoration(RecyclerView list, int dividerColor, int dividerHeight, int dividerMarginLeft, int dividerMarginRight, int dividerMarginBottom) {
        this(list, dividerColor, dividerHeight);
        mDividerMarginLeft = dividerMarginLeft;
        mDividerMarginRight = dividerMarginRight;
        mDividerPaddingBottom = dividerMarginBottom;
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (mDivider == null && mDividerPaint == null) {
            return;
        }
        final int childCount = parent.getChildCount();
        final int width = parent.getWidth();
        for (int childViewIndex = 0; childViewIndex < childCount; childViewIndex++) {
            final View view = parent.getChildAt(childViewIndex);
            int top = (int) ViewCompat.getY(view) + view.getHeight();
            if (mDivider != null) {
                mDivider.setBounds(mDividerMarginLeft, top + mDividerPaddingBottom, width - mDividerMarginRight, top + mDividerPaddingBottom + mDividerHeight);
                mDivider.draw(canvas);
            } else {
                canvas.drawLine(mDividerMarginLeft, top + mDividerPaddingBottom, width - mDividerMarginRight, top + mDividerPaddingBottom + mDividerHeight, mDividerPaint);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.bottom = mDividerPaddingBottom + mDividerHeight;
    }

    public void setDivider(Drawable divider) {
        if (divider != null) {
            mDividerHeight = divider.getIntrinsicHeight();
        } else {
            mDividerHeight = 0;
        }
        mDivider = divider;
        mList.invalidateItemDecorations();
    }

    public void setDividerHeight(int dividerHeight) {
        mDividerHeight = dividerHeight;
        mList.invalidateItemDecorations();
    }

    public void setDividerColorAndHeight(int color, int height) {
        if (mDividerPaint == null) {
            mDividerPaint = new Paint();
        }
        mDividerPaint.setColor(color);
        mDividerHeight = height;
        mList.invalidateItemDecorations();
    }
}
