package com.example.think.tetris.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.think.tetris.R;
import com.example.think.tetris.engine.BlockState;

/**
 * Created by THINK on 2018/7/26.
 */

public class BlockItemView extends View {

    private BlockState blockState = BlockState.IDLE;

    private Paint outerRectPaint;
    private Paint innerRectPaint;
    private Paint marginPaint;

    private float outerStorkWidth;
    private float marginStorkWidth;

    public BlockItemView(Context context) {
        super(context);
    }

    public BlockItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BlockItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        marginStorkWidth = Math.round( (float) getWidth() / 7f);
        marginPaint = new Paint();
        marginPaint.setStrokeWidth(marginStorkWidth);
        marginPaint.setColor(getResources().getColor(R.color.colorIdle));

        outerStorkWidth = Math.round((float) getWidth() / 6f);
        outerRectPaint = new Paint();
        outerRectPaint.setStrokeWidth(outerStorkWidth);

        innerRectPaint = new Paint();
        innerRectPaint.setStrokeWidth(1);

        marginPaint.setStyle(Paint.Style.STROKE);
        outerRectPaint.setStyle(Paint.Style.STROKE);
        innerRectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (blockState == BlockState.IDLE) {
            outerRectPaint.setColor(getResources().getColor(R.color.colorIdle));
            innerRectPaint.setColor(getResources().getColor(R.color.colorIdle));
        } else if (blockState == BlockState.BLOCKED) {
            outerRectPaint.setColor(getResources().getColor(R.color.colorBlocked));
            innerRectPaint.setColor(getResources().getColor(R.color.colorBlocked));
        } else {
            outerRectPaint.setColor(getResources().getColor(R.color.colorCleaning));
            innerRectPaint.setColor(getResources().getColor(R.color.colorCleaning));
        }

        canvas.drawRect(0, 0, getWidth(), getHeight(), marginPaint);
        canvas.drawRect(0 + marginStorkWidth,
                0 + marginStorkWidth,
                getWidth() - marginStorkWidth,
                getHeight() - marginStorkWidth, outerRectPaint);
        canvas.drawRect((float) (0 + (1.5 * outerStorkWidth + marginStorkWidth)),
                (float) (0 + (1.5 * outerStorkWidth + marginStorkWidth)),
                (float) (getWidth() - (1.5 * outerStorkWidth + marginStorkWidth)),
                (float) (getHeight() - (1.5 * outerStorkWidth + marginStorkWidth)),
                innerRectPaint);
    }

    public void changeState(BlockState state) {
        blockState = state;
        invalidate();
    }
}
