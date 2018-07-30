package com.example.think.tetris.engine;

import android.graphics.Point;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by THINK on 2018/7/27.
 */

public class Engine implements IEngine {

    private PanelRefreshListener panelRefreshListener;

    private PanelRefreshListener nextPanelRefreshListener;

    private NextListener nextListener;

    private Random random;

    private int score;

    private boolean blockMap[][];
    private int blockMapWidth;
    private int blockMapHeight;

    private boolean shape[][][] = Blocks.shape;

    private Point nowBlockPosition;
    private Point newBlockPositon;

    private boolean nowBlockMap[][];
    private boolean newBlockMap[][];

    private int nowBlockState;
    private int newBlockState;

    private Thread engineThread;

    private Thread refreshThread;

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

    private boolean[][] generateBlock(int blockState) {
        boolean[][] blockOfState = shape[blockState / 4];
        int arc = blockState % 4;
        return rotateBlock(blockOfState, Math.PI / 2 * arc);
    }

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
        }
    }

    @Override
    public void down() {
        if (isTouched(nowBlockMap, new Point(nowBlockPosition.x, nowBlockPosition.y + 1))) {
            return;
        }
        nowBlockPosition.y++;
        mq.offer(Message.REFRESH);
    }

    @Override
    public void left() {
        if (isTouched(nowBlockMap, new Point(nowBlockPosition.x - 1, nowBlockPosition.y))) {
            return;
        }
        nowBlockPosition.x--;
        mq.offer(Message.REFRESH);
    }

    @Override
    public void right() {
        if (isTouched(nowBlockMap, new Point(nowBlockPosition.x + 1, nowBlockPosition.y))) {
            return;
        }
        nowBlockPosition.x++;
        mq.offer(Message.REFRESH);
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

    class RefreshRunnable implements Runnable {
        @Override
        public void run() {
            while (running) {
                Message msg = Message.REFRESH;
                try {
                    msg = mq.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < stateMap.length; i++) {
                    for (int j = 0; j < stateMap[0].length; j++) {
                        stateMap[i][j] = BlockState.IDLE;
                    }
                }

                switch (msg) {
                    case REFRESH:
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
                        synchronized (lock) {

                            for (int t = 0; t < 3; t++) {

                                for (int i = 0; i < stateMap.length; i++) {
                                    for (int j = 0; j < stateMap[0].length; j++) {
                                        stateMap[i][j] = BlockState.IDLE;
                                    }
                                }

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
                                        for (int j = 0; j < blockMap[i].length; j++) {
                                            if (t % 2 == 0) {
                                                stateMap[i][j] = BlockState.CLEANING;
                                            } else {
                                                stateMap[i][j] = BlockState.IDLE;
                                            }
                                        }
                                    }
                                }
                                panelRefreshListener.onPanelRefresh(stateMap);

                                try {
                                    Thread.sleep(20);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            synchronized (lock) {
                                lock.notifyAll();
                            }
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

            try {
                mq.offer(Message.CLEAN);
                synchronized (lock) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        REFRESH,
        CLEAN;
    }
}
