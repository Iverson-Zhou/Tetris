package com.example.think.tetris.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.example.think.tetris.R;

/**
 * Created by THINK on 2018/8/2.
 */

public class ScoreView extends LinearLayout {
    private static final int N_PLURAL = 4;
    /**
     * 千位
     */
    private ElectronicalNumber thousands;

    /**
     * 百位
     */
    private ElectronicalNumber hundreds;

    /**
     * 十位
     */
    private ElectronicalNumber ten;

    /**
     * 个位
     */
    private ElectronicalNumber one;

    private ElectronicalNumber[] electronicalNumbers;

    public ScoreView(Context context) {
        super(context);
    }

    public ScoreView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void initView() {
        thousands = findViewById(R.id.thousands);
        hundreds = findViewById(R.id.hundreds);
        ten = findViewById(R.id.ten);
        one = findViewById(R.id.one);

        electronicalNumbers = new ElectronicalNumber[N_PLURAL];
        electronicalNumbers[3] = thousands;
        electronicalNumbers[2] = hundreds;
        electronicalNumbers[1] = ten;
        electronicalNumbers[0] = one;
        showNumber(0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initView();
    }

    /**
     * 拆分个位、十位、百位、千位，分别显示
     * @param number
     */
    public void showNumber(int number) {
        int i = 0;
        int remainder = 0;
        do {
            remainder = number % 10;
            number = number / 10;
            if (null != electronicalNumbers[i]) {
                electronicalNumbers[i++].showNumber(remainder);
            }
        } while (number != 0 && i < N_PLURAL);

        //清空不应显示的高位
        for (;i < N_PLURAL; i++) {
            if (null != electronicalNumbers[i]) {
                electronicalNumbers[i].showNumber(-1);
            }
        }
    }
}
