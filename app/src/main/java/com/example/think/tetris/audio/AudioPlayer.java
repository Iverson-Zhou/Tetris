package com.example.think.tetris.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.example.think.tetris.R;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by THINK on 2018/7/30.
 */

public class AudioPlayer {
    public static class Type{
        public final static int SOUND_CLEAN = 1 ;
        public final static int SOUND_MOVE = 2 ;
        public final static int SOUND_GAME_OVER = 3;
    }

    private Context mContext ;
    private static AudioPlayer sInstance ;
    private SoundPool mSp  ;
    private Map<Integer ,Integer> sSpMap ;

    private AudioPlayer(Context context){
        mContext = context ;
        sSpMap = new TreeMap<>() ;
        mSp = new SoundPool(10 , AudioManager.STREAM_MUSIC ,100) ;
        sSpMap.put(Type.SOUND_CLEAN, mSp.load(mContext, R.raw.clean, 1)) ;
        sSpMap.put(Type.SOUND_MOVE, mSp.load(mContext, R.raw.move, 1)) ;
        sSpMap.put(Type.SOUND_GAME_OVER, mSp.load(mContext, R.raw.gameover, 1)) ;
    }

    public static AudioPlayer getInstance(Context context){
        if(null == sInstance) {
            synchronized (AudioPlayer.class) {
                if (null == sInstance) {
                    sInstance = new AudioPlayer(context) ;
                }
            }
        }
        return sInstance ;
    }

    public void play(int type){
        if(null == sSpMap.get(type)) {
            return ;
        }

        mSp.play(sSpMap.get(type), 1, 1, 0, 0, 1) ;
    }

}
