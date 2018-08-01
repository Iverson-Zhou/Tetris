package com.example.think.tetris.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.think.tetris.R;
import com.example.think.tetris.engine.BlockState;
import com.example.think.tetris.engine.IEngine;


/**
 * Created by THINK on 2018/7/26.
 */

public class Panel extends ViewGroup implements IEngine.PanelRefreshListener{

    private int blockMapWidth;
    private int blockMapHeight;

    private Handler handler;

    private Runnable refreshRunnable;

    private Context mContext;

    private BlockItemView[][] blockMap;

    private int defaultBlockSize = 30;

    private float blockActualSize;

    private float heightMatchWidth;
    private float widthMatchHeight;

    private BlockState[][] state;

    public Panel(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public Panel(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context, attrs);
    }

    public Panel(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.Panel);
        blockMapWidth = array.getInt(R.styleable.Panel_panel_width, 10);
        blockMapHeight = array.getInt(R.styleable.Panel_panel_height, 16);

        handler = new Handler();
        refreshRunnable = new RefreshRunnable();

        heightMatchWidth = (float) blockMapHeight / (float) blockMapWidth;
        widthMatchHeight = (float) blockMapWidth / (float) blockMapHeight;
        initBlockMap();
    }

    private void initBlockMap() {
        blockMap = new BlockItemView[blockMapHeight][blockMapWidth];
        for (int i = 0; i < blockMapHeight; i++) {
            for (int j = 0; j < blockMapWidth; j++) {
                blockMap[i][j] = new BlockItemView(mContext);
                addView(blockMap[i][j]);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY || heightMode == MeasureSpec.EXACTLY) {
            if (widthSize / blockMapWidth < heightSize / blockMapHeight) {
                setMeasuredDimension(widthSize, (int) ((float) widthSize * heightMatchWidth));
            } else {
                setMeasuredDimension((int) ((float) heightSize * widthMatchHeight), heightSize);
            }
        } else {
            int defaultBlockWidthNeed = defaultBlockSize * blockMapWidth;
            int defaultBlockHeightNeed = defaultBlockSize * blockMapHeight;

            if (defaultBlockWidthNeed >= widthSize) {
                if (defaultBlockHeightNeed >= heightSize) {
                    if (widthSize / blockMapWidth < heightSize / blockMapHeight) {
                        setMeasuredDimension(widthSize, (int) ((float) widthSize * heightMatchWidth));
                    } else {
                        setMeasuredDimension((int) ((float) heightSize * widthMatchHeight), heightSize);
                    }
                } else {
                    setMeasuredDimension((int) ((float) heightSize * widthMatchHeight), (heightSize));
                }
            } else {
                if (defaultBlockHeightNeed >= heightSize) {
                    setMeasuredDimension(widthSize, (int) ((float) widthSize * heightMatchWidth));
                } else {
                    setMeasuredDimension(defaultBlockWidthNeed, defaultBlockHeightNeed);
                }
            }
        }

        blockActualSize = getMeasuredHeight() / blockMapHeight;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            child.measure(MeasureSpec.makeMeasureSpec((int) ((float) getMeasuredWidth() / (float) blockMapWidth), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec((int) ((float) getMeasuredHeight() / (float) blockMapHeight), MeasureSpec.EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < blockMapHeight; i++) {
            for (int j = 0; j < blockMapWidth; j++) {
                blockMap[i][j].layout(Math.round(j * blockActualSize),
                        Math.round(i * blockActualSize),
                        Math.round((j + 1) * blockActualSize),
                        Math.round((i + 1) * blockActualSize));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint p = new Paint();
        p.setStrokeWidth(10);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, getWidth(), getHeight(), p);
    }

    public int getBlockMapWidth() {
        return blockMapWidth;
    }

    public int getBlockMapHeight() {
        return blockMapHeight;
    }

    public void drawState(BlockState[][] state) {
        if (null == state) {
            return;
        }
        if (state.length != blockMapHeight || state[0].length != blockMapWidth) {
            return;
        }

        this.state = state.clone();
        handler.post(refreshRunnable);
    }

    class RefreshRunnable implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < state.length; i++) {
                for (int j = 0; j < state[0].length; j++) {
                    blockMap[i][j].changeState(state[i][j]);
                }
            }
        }
    }

    @Override
    public void onPanelRefresh(BlockState[][] newPanel) {
        drawState(newPanel);
    }
}
