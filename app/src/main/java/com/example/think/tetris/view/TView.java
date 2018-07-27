package com.example.think.tetris.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by THINK on 2018/7/27.
 */

public class TView extends android.support.v7.widget.AppCompatTextView {
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
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (null != onTouch) {
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
