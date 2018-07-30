package com.example.think.tetris.engine;

import android.graphics.Point;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by THINK on 2018/7/27.
 */

public class Engine implements IEngine {

    /**
     * 给方块主view的回调
     */
    private PanelRefreshListener panelRefreshListener;
    /**
     * 显示下一个方块的回调
     */
    private PanelRefreshListener nextPanelRefreshListener;

    private NextListener nextListener;

    /**
     * 生成下一方块
     */
    private Random random;

    /**
     * 得分，消除一行得10分
     */
    private int score;

    /**
     * 方块主view{@link com.example.think.tetris.view.Panel}的抽象
     */
    private boolean blockMap[][];
    private int blockMapWidth;
    private int blockMapHeight;

    /**
     * 所有可能的方块, 不包含旋转的
     */
    private boolean shape[][][] = Blocks.shape;

    /**
     * 当前方块在方块主view的位置
     */
    private Point nowBlockPosition;

    /**
     * 下一方块在显示下一方块的view中的位置
     */
    private Point newBlockPositon;

    /**
     * 当前的方块
     */
    private boolean nowBlockMap[][];

    /**
     * 下一个方块
     */
    private boolean newBlockMap[][];

    /**
     * 代表随机生成的方块。shape中总共有7中原始的方块，每种方块4中旋转，总共28中可能性。
     * nowBlockState / 4，代表当前的方块种类
     * nowBlockState % 4，代表当前方块的旋转次数
     */
    private int nowBlockState;

    /**
     * 下一次随机生成的方块
     */
    private int newBlockState;

    /**
     * 游戏业务主循环
     */
    private Thread engineThread;

    /**
     * 刷新循环
     */
    private Thread refreshThread;

    /**
     * 将{@link blockMap}中方块主view映射出不同的额状态，用于直接更新方块主view
     */
    private BlockState[][] stateMap;
    private BlockState[][] newStateMap;

    private boolean running = false;

    private Object lock = new Object();

    private BlockingQueue<Message> mq = new ArrayBlockingQueue<>(10);

    public Engine() {
        random = new Random();
    }

    @Override
    public void setPanelRefreshListener(PanelRefreshListener panelRefreshListener) {
        this.panelRefreshListener = panelRefreshListener;
    }

    @Override
    public void setNextPanelRefreshListener(PanelRefreshListener panelRefreshListener) {
        nextPanelRefreshListener = panelRefreshListener;
    }

    @Override
    public void setNextListener(NextListener nextListener) {
        this.nextListener = nextListener;
    }

    @Override
    public void setUp(int width, int height) {
        blockMapWidth = width;
        blockMapHeight = height;
        blockMap = new boolean[height][width];
        stateMap = new BlockState[height][width];
        newStateMap = new BlockState[4][4];

        init();
    }

    private void init() {
        for (int i = 0; i < blockMap.length; i++) {
            for (int j = 0; j < blockMap[0].length; j++) {
                blockMap[i][j] = false;
                stateMap[i][j] = BlockState.IDLE;
            }
        }

        score = 0;

        nowBlockState = generateBlockState();
        nowBlockMap = generateBlock(newBlockState);
        newBlockState = generateBlockState();
        newBlockMap = generateBlock(newBlockState);

        newBlockPositon = generateNextBlockPosition(newBlockMap);
        nowBlockPosition = generateBlockPosition(nowBlockMap);
    }

    private Point generateNextBlockPosition(boolean[][] blockMap) {
        return new Point(4 / 2 - blockMap[0].length / 2, 4 / 2 - blockMap.length / 2);
    }

    private Point generateBlockPosition(boolean[][] blockMap) {
        return new Point(blockMapWidth / 2 - blockMap[0].length / 2, - blockMap.length);
    }

    private int generateBlockState() {
        int blockNum = shape.length * 4;
        return Math.abs(random.nextInt()) % blockNum;
    }

    /**
     * 根据随机生成的值，生成一个方块
     * @param blockState 一个随机生成的方块值
     * @return
     */
    private boolean[][] generateBlock(int blockState) {
        boolean[][] blockOfState = shape[blockState / 4];
        int arc = blockState % 4;
        return rotateBlock(blockOfState, Math.PI / 2 * arc);
    }

    /**
     * 旋转方块
     * @param blockMap
     * @param angel
     * @return
     */
    private boolean[][] rotateBlock(boolean[][] blockMap, double angel) {
        int Heigth = blockMap.length;
        int Width = blockMap[0].length;
        boolean[][] ResultBlockMap = new boolean[Heigth][Width];
        float CenterX = (Width - 1) / 2f;
        float CenterY = (Heigth - 1) / 2f;
        for (int i = 0; i < blockMap.length; i++) {
            for (int j = 0; j < blockMap[i].length; j++) {
                float RelativeX = j - CenterX;
                float RelativeY = i - CenterY;
                float ResultX = (float) (Math.cos(angel) * RelativeX - Math.sin(angel) * RelativeY);
                float ResultY = (float) (Math.cos(angel) * RelativeY + Math.sin(angel) * RelativeX);
                Point OrginPoint = new Point(Math.round(CenterX + ResultX), Math.round(CenterY + ResultY));
                ResultBlockMap[OrginPoint.y][OrginPoint.x] = blockMap[i][j];
            }
        }
        return ResultBlockMap;
    }

    @Override
    public void up() {
        boolean[][] tmpBlockMap = rotateBlock(nowBlockMap, Math.PI / 2 * 1);
        if (!isTouched(tmpBlockMap, nowBlockPosition)) {
            nowBlockMap = tmpBlockMap;
            mq.offer(Message.REFRESH);
            nextListener.onMove();
        }
    }

    @Override
    public void down() {
        if (isTouched(nowBlockMap, new Point(nowBlockPosition.x, nowBlockPosition.y + 1))) {
            return;
        }
        nowBlockPosition.y++;
        mq.offer(Message.REFRESH);
        nextListener.onMove();
    }

    @Override
    public void left() {
        if (isTouched(nowBlockMap, new Point(nowBlockPosition.x - 1, nowBlockPosition.y))) {
            return;
        }
        nowBlockPosition.x--;
        mq.offer(Message.REFRESH);
        nextListener.onMove();
    }

    @Override
    public void right() {
        if (isTouched(nowBlockMap, new Point(nowBlockPosition.x + 1, nowBlockPosition.y))) {
            return;
        }
        nowBlockPosition.x++;
        mq.offer(Message.REFRESH);
        nextListener.onMove();
    }

    @Override
    public void start() {
        running = true;
        engineThread = new Thread(new Task());
        engineThread.start();
        refreshThread = new Thread(new RefreshRunnable());
        refreshThread.start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void pause() {

    }

    class Task implements Runnable {
        @Override
        public void run() {
            while (running) {
                if (isTouched(nowBlockMap, new Point(nowBlockPosition.x, nowBlockPosition.y + 1))) {
                    if (fixMap()) {
                        score = score + clearLines() * 10;
                        nextListener.onScore(score);
                        generateNewBlock();
                    } else {
                        //游戏结束
                        gameOver();
                        init();
                    }
                } else {
                    nowBlockPosition.y++;
                }
                mq.offer(Message.REFRESH);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void gameOver() {
        nextListener.onGameOver();
        mq.offer(Message.GAME_OVER);

        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class RefreshRunnable implements Runnable {
        Set<Integer> completeLineId = new HashSet<>();
        @Override
        public void run() {
            while (running) {
                Message msg = Message.REFRESH;
                try {
                    msg = mq.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                switch (msg) {
                    case REFRESH:

                        //游戏主view刷新
                        for (int i = 0; i < stateMap.length; i++) {
                            for (int j = 0; j < stateMap[0].length; j++) {
                                stateMap[i][j] = BlockState.IDLE;
                            }
                        }
                        for (int i = 0; i < nowBlockMap.length; i++) {
                            for (int j = 0; j < nowBlockMap[0].length; j++) {
                                if (nowBlockMap[i][j] && nowBlockPosition.y + i >= 0) {
                                    stateMap[nowBlockPosition.y + i][nowBlockPosition.x + j] = BlockState.BLOCKED;
                                }
                            }
                        }
                        for (int i = 0; i < blockMap.length; i++) {
                            for (int j = 0; j < blockMap[i].length; j++) {
                                if (blockMap[i][j]) {
                                    stateMap[i][j] = BlockState.BLOCKED;
                                }
                            }
                        }
                        panelRefreshListener.onPanelRefresh(stateMap);

                        //下个方块view刷新
                        for (int i = 0; i < 4; i++) {
                            for (int j = 0; j < 4; j++) {
                                newStateMap[i][j] = BlockState.IDLE;

                            }
                        }
                        for (int i = 0; i < newBlockMap.length; i++) {
                            for (int j = 0; j < newBlockMap[i].length; j++) {
                                if (newBlockMap[i][j]) {
                                    newStateMap[newBlockPositon.y + i][newBlockPositon.x + j] = BlockState.BLOCKED;
                                }
                            }
                        }
                        nextPanelRefreshListener.onPanelRefresh(newStateMap);
                        break;
                    case CLEAN:
                            completeLineId.clear();
                            for (int i = 0; i < blockMap.length; i++) {
                                boolean isCompleteLine = true;
                                for (int j = 0; j < blockMap[i].length; j++) {
                                    if (blockMap[i][j]) {
                                        stateMap[i][j] = BlockState.BLOCKED;
                                    } else {
                                        isCompleteLine = false;
                                    }
                                }

                                if (isCompleteLine) {
                                    completeLineId.add(i);
                                }
                            }

                            if (completeLineId.size() > 0) {
                                nextListener.onClean();
                                for (int t = 0; t < 6; t++) {

                                    for (Integer i : completeLineId) {

                                        for (int j = 0; j < blockMap[i].length; j++) {
                                            if (t % 2 == 0) {
                                                stateMap[i][j] = BlockState.CLEANING;
                                            } else {
                                                stateMap[i][j] = BlockState.IDLE;
                                            }
                                        }
                                    }
                                    panelRefreshListener.onPanelRefresh(stateMap);

                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            synchronized (lock) {
                                //UI刷新完成后，游戏主线程继续
                                lock.notifyAll();
                            }
                        break;
                    case GAME_OVER:
                        for (int i = stateMap.length - 1; i >= 0; i--) {
                            for (int j = 0; j < stateMap[i].length; j++) {
                                stateMap[i][j] = BlockState.BLOCKED;
                            }

                            panelRefreshListener.onPanelRefresh(stateMap);
                            try {
                                Thread.sleep(20);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        for (int i = 0; i < stateMap.length; i++) {
                            for (int j = 0; j < stateMap[i].length; j++) {
                                stateMap[i][j] = BlockState.IDLE;
                            }

                            panelRefreshListener.onPanelRefresh(stateMap);
                            try {
                                Thread.sleep(30);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        synchronized (lock) {
                            //UI刷新完成后，游戏主线程继续
                            lock.notifyAll();
                        }
                        break;
                        default:
                            break;
                }

            }
        }
    }

    private void generateNewBlock() {
        nowBlockMap = newBlockMap;

        nowBlockState = newBlockState;
        nowBlockMap = newBlockMap;
        newBlockState = generateBlockState();
        newBlockMap = generateBlock(newBlockState);

        newBlockPositon = generateNextBlockPosition(newBlockMap);
        nowBlockPosition = generateBlockPosition(nowBlockMap);

    }

    private int clearLines() {
        int lines = 0;
        try {
            //刷新UI
            mq.offer(Message.CLEAN);
            synchronized (lock) {
                //等待UI刷新完成
                lock.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < blockMap.length; i++) {
            boolean isCompleteLine = true;
            for (int j = 0; j < blockMap[i].length; j++) {
                if (!blockMap[i][j]) {
                    isCompleteLine = false;
                    continue;
                }

            }
            if (!isCompleteLine) {
                continue;
            }

            for (int k = i; k > 0; k--) {
                blockMap[k] = blockMap[k - 1];
            }

            blockMap[0] = new boolean[blockMapWidth];
            lines++;
        }
        return lines;
    }

    private boolean fixMap() {
        for (int i = 0; i < nowBlockMap.length; i++) {
            for (int j = 0; j < nowBlockMap[0].length; j++) {
                if (nowBlockMap[i][j]) {
                    if (nowBlockPosition.y + i < 0) {
                        return false;
                    } else {
                        blockMap[nowBlockPosition.y + i][nowBlockPosition.x + j] = nowBlockMap[i][j];
                    }
                }
            }
        }
        return true;
    }

    private boolean isTouched(boolean[][] moveNextblockMap, Point point) {
        if (null == moveNextblockMap) {
            return false;
        }

        for (int i = 0; i < moveNextblockMap.length; i++) {
            for (int j = 0; j < moveNextblockMap[i].length; j++) {
                if (moveNextblockMap[i][j]) {
                    if (point.x + j < 0 || point.x + j >= blockMapWidth || point.y + i >= blockMapHeight) {
                        return true;
                    } else {
                        if (point.y + i < 0) {
                            continue;
                        } else {
                            if (blockMap[point.y + i][point.x + j]) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    enum Message {
        /**
         * 刷新一次
         */
        REFRESH,

        /**
         * 清除完整行
         */
        CLEAN,

        /**
         * 游戏结束
         */
        GAME_OVER
    }
}
