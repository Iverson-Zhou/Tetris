package com.example.think;

import android.app.Application;
import android.graphics.Typeface;
import android.util.Log;

import java.lang.reflect.Field;

/**
 * Created by THINK on 2018/8/6.
 */

public class TetrisApplication extends Application {
    private static final String TAG = "TetrisApplication";

    private static Typeface typeface;
    @Override
    public void onCreate() {
        super.onCreate();

        initTTF();
    }

    private void initTTF() {
        try {
//            typeface = Typeface.createFromAsset(getAssets(), "fonts/FISH.ttf");
            typeface = Typeface.createFromAsset(getAssets(), "fonts/PingFangLight.ttf");

            Field field = Typeface.class.getDeclaredField("MONOSPACE");
            field.setAccessible(true);
            field.set(null, typeface);
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "initTTF: ");
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "initTTF: ");
            e.printStackTrace();
        }
    }
}
