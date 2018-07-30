package com.example.think.tetris.engine;

/**
 * Created by THINK on 2018/7/27.
 */

public interface IEngine {

    void setPanelRefreshListener(PanelRefreshListener panelRefreshListener);

    void setNextPanelRefreshListener(PanelRefreshListener panelRefreshListener);

    void setNextListener(NextListener nextListener);

    void setUp(int width, int height);

    void start();

    void stop();

    void pause();

    void up();

    void down();

    void left();

    void right();

    interface PanelRefreshListener {
        void onPanelRefresh(BlockState[][] newPanel);
    }

    interface NextListener {
        void onScore(int score);

        void onClean();

        void onMove();
    }
}
