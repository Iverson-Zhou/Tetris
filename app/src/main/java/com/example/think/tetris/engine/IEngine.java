package com.example.think.tetris.engine;

/**
 * Created by THINK on 2018/7/27.
 */

public interface IEngine {

    void setPanelRefreshListener(PanelRefreshListener panelRefreshListener);

    void setNextPanelRefreshListener(PanelRefreshListener panelRefreshListener);

    void setNextListener(NextListener nextListener);

    /**
     * 初始化引擎
     * @param width 方块主view宽度
     * @param height 方块主view高度
     */
    void setUp(int width, int height);

    /**
     * 游戏开始
     */
    void start();

    void stop();

    void pause();

    /**
     * 旋转当前方块
     */
    void up();

    /**
     * 加速下落
     */
    void down();

    /**
     * 左移
     */
    void left();

    /**
     * 右移
     */
    void right();

    /**
     * 给方块主view的回调
     */
    interface PanelRefreshListener {
        /**
         *
         * @param newPanel 每个方块主view状态的抽象，与方块主view一一对应
         *                 请查看{@link BlockState}，了解组成方块主view中的
         *                 每个小view的所有状态
         */
        void onPanelRefresh(BlockState[][] newPanel);
    }

    interface NextListener {
        void onScore(int score);

        void onClean();

        void onMove();

        void onGameOver();
    }
}
