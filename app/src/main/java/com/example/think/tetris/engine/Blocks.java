package com.example.think.tetris.engine;

/**
 * Created by THINK on 2018/7/26.
 */

public class Blocks {
    public static final boolean shape[][][] = {
            /**
             * OOOO
            */
            {
                    {false, false, false, false},
                    {true, true, true, true},
                    {false, false, false, false},
                    {false, false, false, false}
            },
            /**
             *    O
             *  O O O
            */
            {
                    {false, true, false},
                    {true, true, true},
                    {false, false, false}
            },
            /**
             * O
             * O O O
            */
            {
                    {true, false, false},
                    {true, true, true},
                    {false, false, false}
            },
            /**
             *     O
             * O O O
            */
            {
                    {false, false, true},
                    {true, true, true},
                    {false, false, false}
            },
            /**
             * O O
             *   O O
            */
            {
                    {true, true, false},
                    {false, true, true},
                    {false, false, false}
            },
            /**
             *   O O
             * O O
            */
            {
                    {false, true, true},
                    {true, true, false},
                    {false, false, false}
            },
            /**
             * O O
             * O O
            */
            {
                    {true, true},
                    {true, true}
            }
    };
}
