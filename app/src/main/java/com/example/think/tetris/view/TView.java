package com.example.think.tetris.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by THINK on 2018/7/27.
 */

public class TView extends android.support.v7.widget.AppCompatButton {
    private Object lock = new Object();
    private Thread touchRun;
    private boolean alive = true;

    private boolean touching = false;

    private OnTouch onTouch;

    public TView(Context context) {
        super(context);
        init();
    }

    public TView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        touchRun = new Thread(new TouchRunnable());
        touchRun.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                synchronized (lock) {
                    lock.notifyAll();
                }
                touching = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                touching = false;
                break;
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        alive = false;
        touchRun.interrupt();
    }

    class TouchRunnable implements Runnable{
        @Override
        public void run() {
            while (alive) {
                if (!touching) {
                    try {
                        synchronized (lock) {
                            lock.wait();
                        }
                        //直接响应单击事件
                        if (null != onTouch) {
                            onTouch.onTouch();
                        }

                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                //等待200ms后，如果还在点击状态，回调点击监听
                if (null != onTouch && touching) {
                    onTouch.onTouch();
                }

                try {
                    //值越大飞的越慢
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void setOnTouch(OnTouch onTouch) {
        this.onTouch = onTouch;
    }

    public interface OnTouch {
        void onTouch();
    }
}
