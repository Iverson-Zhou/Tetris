package com.example.think.tetris.engine;

/**
 * Created by THINK on 2018/7/27.
 */

public class EngingFactory {
    public static IEngine getDefaultEngine() {
        return new Engine();
    }
}
