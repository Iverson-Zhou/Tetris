package com.example.think.tetris.engine;

/**
 * Created by THINK on 2018/7/27.
 */

public enum BlockState {
    IDLE(1),

    BLOCKED(2),

    CLEANING(3);

    private int state;

    BlockState(int state) {
        this.state = state;
    }
}
