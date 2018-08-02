package com.example.think.tetris.view;


import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.think.tetris.R;

/**
 * Created by THINK on 2018/8/1.
 */

public class ElectronicalNumber extends View {
    private static final float HEIGH_TWIDTH_RATIO = 2.0f;//高宽比
    private static final int MAX_WIDTH = 200;//最大宽度

    private Paint paint;
    private Path path;

    /**
     * 控件宽度
     */
    private int width;
    /**
     * 控件长度
     */
    private int height;

    /**
     * 组成数字的线段宽度
     */
    private float lineWidth;
    /**
     * 组成数字的线段长度
     */
    private float lineLength;

    /**
     * 数字与边界距离
     */
    private float padding;

    /**
     * 数字倾斜度数
     */
    private float tiltDegree;

    /**
     * 需要显示的数字
     */
    private int number = -1;

    public ElectronicalNumber(Context context) {
        super(context);
        init();
    }

    public ElectronicalNumber(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ElectronicalNumber(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));

        path = new Path();

        tiltDegree = 5f;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if ((float) heightSize / (float) widthSize > HEIGH_TWIDTH_RATIO) {
            int desired = widthSize;
            if (widthMode == MeasureSpec.AT_MOST) {
                if (widthSize > MAX_WIDTH) {
                    desired = MAX_WIDTH;
                }
            }
            //按照高宽比例返回控件的宽高
            setMeasuredDimension(desired, (int) (desired * HEIGH_TWIDTH_RATIO));
        } else {
            int desired = heightSize;
            if (heightMode == MeasureSpec.AT_MOST) {
                if (heightSize > (float) MAX_WIDTH * HEIGH_TWIDTH_RATIO) {
                    desired = (int) ((float) MAX_WIDTH * HEIGH_TWIDTH_RATIO);
                }
            }
            //按照高宽比例返回控件的宽高
            setMeasuredDimension((int) ((float) desired / HEIGH_TWIDTH_RATIO), desired);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        height = getHeight();

        lineWidth = (float) width * 0.1f;
        lineLength = (float) width * 0.7f;
        padding = lineWidth;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        switch (number) {
            case -1://代表全空状态
                part1(canvas, false);
                part2(canvas, false);
                part3(canvas, false);
                part4(canvas, false);
                part5(canvas, false);
                part6(canvas, false);
                part7(canvas, false);
                break;
            case 0:
                part1(canvas, true);
                part2(canvas, true);
                part3(canvas, true);
                part4(canvas, false);
                part5(canvas, true);
                part6(canvas, true);
                part7(canvas, true);
                break;
            case 1:
                part1(canvas, false);
                part2(canvas, false);
                part3(canvas, true);
                part4(canvas, false);
                part5(canvas, false);
                part6(canvas, true);
                part7(canvas, false);
                break;
            case 2:
                part1(canvas, true);
                part2(canvas, false);
                part3(canvas, true);
                part4(canvas, true);
                part5(canvas, true);
                part6(canvas, false);
                part7(canvas, true);
                break;
            case 3:
                part1(canvas, true);
                part2(canvas, false);
                part3(canvas, true);
                part4(canvas, true);
                part5(canvas, false);
                part6(canvas, true);
                part7(canvas, true);
                break;
            case 4:
                part1(canvas, false);
                part2(canvas, true);
                part3(canvas, true);
                part4(canvas, true);
                part5(canvas, false);
                part6(canvas, true);
                part7(canvas, false);
                break;
            case 5:
                part1(canvas, true);
                part2(canvas, true);
                part3(canvas, false);
                part4(canvas, true);
                part5(canvas, false);
                part6(canvas, true);
                part7(canvas, true);
                break;
            case 6:
                part1(canvas, true);
                part2(canvas, true);
                part3(canvas, false);
                part4(canvas, true);
                part5(canvas, true);
                part6(canvas, true);
                part7(canvas, true);
                break;
            case 7:
                part1(canvas, true);
                part2(canvas, false);
                part3(canvas, true);
                part4(canvas, false);
                part5(canvas, false);
                part6(canvas, true);
                part7(canvas, false);
                break;
            case 8:
                part1(canvas, true);
                part2(canvas, true);
                part3(canvas, true);
                part4(canvas, true);
                part5(canvas, true);
                part6(canvas, true);
                part7(canvas, true);
                break;
            case 9:
                part1(canvas, true);
                part2(canvas, true);
                part3(canvas, true);
                part4(canvas, true);
                part5(canvas, false);
                part6(canvas, true);
                part7(canvas, true);
                break;
            default:
                part1(canvas, true);
                part2(canvas, true);
                part3(canvas, true);
                part4(canvas, true);
                part5(canvas, true);
                part6(canvas, true);
                part7(canvas, true);
                break;

        }
    }

    public void showNumber(int n) {
        if (n < -1 || n > 9) {
            return;
        }
        number = n;
        postInvalidate();
    }

    /**
     *  -
     * * *
     *  *
     * * *
     * *
     */
    private void part1(Canvas canvas, boolean solid) {
        setUpPaint(solid);
        canvas.save();
        canvas.translate((float) (lineLength * Math.sin(Math.PI / 180f * tiltDegree)), 0);
        canvas.rotate(-90, (float) (lineLength * Math.sin(Math.PI / 180f * tiltDegree)) + padding, padding);

        drawPart();

        canvas.drawPath(path, paint);
        canvas.restore();
    }

    /**
     *  -
     * | *
     *  *
     * * *
     * *
     */
    private void part2(Canvas canvas, boolean solid) {
        setUpPaint(solid);

        canvas.save();
        canvas.translate((float) (lineLength * Math.sin(Math.PI / 180f * tiltDegree)), 0);
        canvas.rotate(tiltDegree, padding, padding);

        drawPart();

        canvas.drawPath(path, paint);
        canvas.restore();
    }

    /**
     *  -
     * | |
     *  *
     * * *
     * *
     */
    private void part3(Canvas canvas, boolean solid) {
        setUpPaint(solid);


        canvas.save();
        canvas.translate((float) (lineLength * Math.sin(Math.PI / 180f * tiltDegree) + lineLength), 0);
        canvas.rotate(tiltDegree, padding, padding);

        drawPart();

        canvas.drawPath(path, paint);
        canvas.restore();
    }

    /**
     *  -
     * | |
     *  -
     * * *
     * *
     */
    private void part4(Canvas canvas, boolean solid) {
        setUpPaint(solid);

        canvas.save();
        canvas.translate((float) (lineLength * Math.sin(Math.PI / 180f * tiltDegree)), 0);
        canvas.rotate(-90, padding, padding);
        canvas.translate(-(lineLength + padding), 0);

        drawPart();

        path.close();
        canvas.drawPath(path, paint);
        canvas.restore();
    }

    /**
     *  -
     * | |
     *  -
     * | *
     *  *
     */
    private void part5(Canvas canvas, boolean solid) {
        setUpPaint(solid);

        canvas.save();
        canvas.translate((float) (lineLength * Math.sin(Math.PI / 180f * tiltDegree)), 0);
        canvas.rotate(tiltDegree, padding, padding);
        canvas.translate(0, lineLength + padding);

        drawPart();

        canvas.drawPath(path, paint);
        canvas.restore();
    }

    /**
     *  -
     * | |
     *  -
     * | |
     *  *
     */
    private void part6(Canvas canvas, boolean solid) {
        setUpPaint(solid);

        canvas.save();
        canvas.translate((float) (lineLength * Math.sin(Math.PI / 180f * tiltDegree) + lineLength), 0);
        canvas.rotate(tiltDegree, padding, padding);
        canvas.translate(0, lineLength + padding);

        drawPart();

        canvas.drawPath(path, paint);
        canvas.restore();
    }

    /**
     *  -
     * | |
     *  -
     * | |
     *  -
     */
    private void part7(Canvas canvas, boolean solid) {
        setUpPaint(solid);

        canvas.save();
        canvas.translate(-(float) (lineLength * Math.sin(Math.PI / 180f * tiltDegree)) + padding / 2, 0);
        canvas.rotate(-90, padding, padding);
        canvas.translate(-(2 * (lineLength + padding)), 0);

        drawPart();

        canvas.drawPath(path, paint);
        canvas.restore();
    }

    private void drawPart() {
        path.reset();
        path.moveTo(padding, padding);
        path.quadTo(padding + (lineWidth / 2), padding - (lineWidth / 2), padding + lineWidth, padding);
        path.lineTo(padding + lineWidth, padding + lineLength);
        path.quadTo(padding + (lineWidth / 2), lineLength + padding + (padding / 2), padding, padding + lineLength);
        path.close();
    }


    private void setUpPaint(boolean solid) {
        if (solid) {
            paint.setColor(getResources().getColor(R.color.colorBlocked));
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
        } else {
            paint.setColor(getResources().getColor(R.color.colorTransparentBlack));
            paint.setStyle(Paint.Style.STROKE);
        }
    }
}
