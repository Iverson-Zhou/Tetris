package com.example.think.tetris;

import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.think.tetris.audio.AudioPlayer;
import com.example.think.tetris.engine.EngingFactory;
import com.example.think.tetris.engine.IEngine;
import com.example.think.tetris.view.Panel;
import com.example.think.tetris.view.ScoreView;
import com.example.think.tetris.view.TView;

public class MainActivity extends AppCompatActivity implements IEngine.NextListener{

    private IEngine engine;

    private Panel panel;

    private Panel nextPanel;

    private TView up;
    private TView left;
    private TView right;
    private TView down;

    private Button btnStart;
    private Button btnStop;
    private Button btnPause;

    private ScoreView scoreView;

    private Handler handler;

    private AudioPlayer audioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();

        initView();
        initEngine();

    }

    private void initView() {
        up = findViewById(R.id.btn_up);
        left = findViewById(R.id.btn_left);
        right = findViewById(R.id.btn_right);
        down = findViewById(R.id.btn_down);

        scoreView = findViewById(R.id.scrollView);

        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnPause = findViewById(R.id.btn_pause);

        up.setOnTouch(new TView.OnTouch() {
            @Override
            public void onTouch() {
                engine.up();
            }
        });
        left.setOnTouch(new TView.OnTouch() {
            @Override
            public void onTouch() {
                engine.left();
            }
        });
        right.setOnTouch(new TView.OnTouch() {
            @Override
            public void onTouch() {
                engine.right();
            }
        });
        down.setOnTouch(new TView.OnTouch() {
            @Override
            public void onTouch() {
                engine.down();
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engine.start();
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                engine.pause();
            }
        });
    }

    public void initEngine() {
        audioPlayer = AudioPlayer.getInstance(this);

        panel = findViewById(R.id.panel);
        nextPanel = findViewById(R.id.next_panel);

        engine = EngingFactory.getDefaultEngine();
        engine.setPanelRefreshListener(panel);
        engine.setNextPanelRefreshListener(nextPanel);
        engine.setNextListener(this);
        engine.setUp(panel.getBlockMapWidth(), panel.getBlockMapHeight());

    }

    @Override
    public void onScore(final int score) {
        scoreView.showNumber(score);
    }

    @Override
    public void onClean() {
        audioPlayer.play(AudioPlayer.Type.SOUND_CLEAN);
        handler.post(new Runnable() {
            @Override
            public void run() {
                panel.cleanAnimation();
            }
        });
    }

    @Override
    public void onMove() {
        audioPlayer.play(AudioPlayer.Type.SOUND_MOVE);
    }

    @Override
    public void onGameOver() {
        Log.i("zhoukai", "onOver");
        audioPlayer.play(AudioPlayer.Type.SOUND_GAME_OVER);
    }
}
