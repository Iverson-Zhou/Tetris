package com.example.think.tetris.view;

import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
    private static final String TAG = "Panel";

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

    private Camera camera;
    private Matrix cameraMatrix;

    private float canvasTranslateX;

    private float canvasTranslateY;

    private float canvasScaleX;

    private float canvasScaleY;

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


        canvasTranslateX = 0;
        canvasTranslateY = 0;
        canvasScaleX = 1.0f;
        canvasScaleY = 1.0f;
        camera = new Camera();
        cameraMatrix = new Matrix();

        array.recycle();
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
        setCanvasTrans(canvas);

        Paint p = new Paint();
        p.setStrokeWidth(10);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, getWidth(), getHeight(), p);
    }

    private void setCanvasTrans(Canvas canvas) {
        cameraMatrix.reset();
        camera.save();
        camera.getMatrix(cameraMatrix);
        camera.restore();

        cameraMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        cameraMatrix.postTranslate(getWidth() / 2, getHeight() / 2);

        canvas.concat(cameraMatrix);

        canvas.scale(canvasScaleX, canvasScaleY, getWidth() / 2, getHeight() / 2);

        canvas.translate(canvasTranslateX, canvasTranslateY);
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

    public void cleanAnimation() {
        final String translateX = "canvasTranslateX";
        final String translateY = "canvasTranslateY";
        final String scaleX = "scaleX";
        final String scaleY = "scaleY";

        canvasScaleX = 0.97f;
        canvasScaleY = 0.97f;
        canvasTranslateX = 10;
//        canvasTranslateY = 10;
        PropertyValuesHolder propertyValuesHolderScaleX =
                PropertyValuesHolder.ofFloat(scaleX, canvasScaleX, 1.0f);
        PropertyValuesHolder propertyValuesHolderScaleY =
                PropertyValuesHolder.ofFloat(scaleY, canvasScaleY, 1.0f);
        PropertyValuesHolder propertyValuesHolderTranslateX =
                PropertyValuesHolder.ofFloat(translateX, canvasTranslateX, 0);
        PropertyValuesHolder propertyValuesHolderTranslateY =
                PropertyValuesHolder.ofFloat(translateY, canvasTranslateY, 0);

        ValueAnimator valueAnimator = ValueAnimator.
                ofPropertyValuesHolder(propertyValuesHolderTranslateX,
                        propertyValuesHolderTranslateY);

        ValueAnimator valueAnimatorScale = ValueAnimator.ofPropertyValuesHolder(
                propertyValuesHolderScaleX, propertyValuesHolderScaleY);

        valueAnimator.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {

                float f = 0.571429f;
                return (float) (Math.pow(2, -2 * input) * Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
            }
        });

        valueAnimator.setDuration(200);
        valueAnimatorScale.setDuration(200);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                canvasTranslateX = (float) animation.getAnimatedValue(translateX);
                canvasTranslateY = (float) animation.getAnimatedValue(translateY);

                postInvalidate();
            }
        });
        valueAnimatorScale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                canvasScaleX = (float) animation.getAnimatedValue(scaleX);
                canvasScaleY = (float) animation.getAnimatedValue(scaleY);
                Log.i(TAG, "onAnimationUpdate: " + canvasScaleX + canvasScaleY);
            }
        });

        valueAnimator.start();
        valueAnimatorScale.start();

    }
}
