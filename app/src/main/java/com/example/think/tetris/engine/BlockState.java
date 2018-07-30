package com.example.think.tetris.engine;

/**
 * Created by THINK on 2018/7/27.
 */

public enum BlockState {
    /**
     * 当前位置没有方块
     */
    IDLE(1),

    /**
     * 当前位置有方块
     */
    BLOCKED(2),

    /**
     * 清除当前位置
     */
    CLEANING(3);

    private int state;

    BlockState(int state) {
        this.state = state;
    }
}
