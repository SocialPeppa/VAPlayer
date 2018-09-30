package com.example.panda.mobileplayer.activity;

import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;

import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;


import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.panda.mobileplayer.IMusicPlayerService;
import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.domain.MediaItem;
import com.example.panda.mobileplayer.service.MusicPlayerService;
import com.example.panda.mobileplayer.utils.LyricUtils;
import com.example.panda.mobileplayer.utils.Utils;
import com.example.panda.mobileplayer.view.ShowLyricView;


import java.io.File;
import java.util.ArrayList;


public class AudioPlayerActivity extends Activity implements View.OnClickListener {
    private static final int PROGRESS = 1;
    private int position;
    private IMusicPlayerService service;
    private TextView tvArtist;
    private TextView tvName;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;
    private Button btnLyrc;
    private ShowLyricView showLyricView;
    private MyReceiver1 receiver1;
    private MyReceiver2 receiver2;
    private Utils utils;
    private boolean isFromNotification;
    public static final int NORMAL = 1;
    public static final int SINGLE = 2;
    public static final int ALL = 3;
    private int playMode;
    private ArrayList<MediaItem> mediaItems;


    private ServiceConnection connection;


    private void findViews() {
        setContentView(R.layout.activity_audio_player);
        tvArtist = findViewById(R.id.tv_artist);
        tvName = findViewById(R.id.tv_name);
        tvTime = findViewById(R.id.tv_time);
        seekbarAudio = findViewById(R.id.seekbar_audio);
        btnAudioPlaymode = findViewById(R.id.btn_audio_playmode);
        btnAudioPre = findViewById(R.id.btn_audio_pre);
        btnAudioStartPause = findViewById(R.id.btn_audio_start_pause);
        btnAudioNext = findViewById(R.id.btn_audio_next);
        btnLyrc = findViewById(R.id.btn_lyrc);
        showLyricView = findViewById(R.id.showLyricView);
        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener(this);
        btnAudioStartPause.setOnClickListener(this);
        btnAudioNext.setOnClickListener(this);
        btnLyrc.setOnClickListener(this);
        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }


    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                try {
                    service.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnAudioPlaymode) {
            setPlayMode();
        } else if (v == btnAudioPre) {

            if (service != null) {
                try {
                    service.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        } else if (v == btnAudioStartPause) {
            if (service != null) {
                try {
                    if (service.isPlaying()) {
                        service.pause();
                        handler.removeMessages(PROGRESS);
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                    } else {
                        service.start();
                        handler.sendEmptyMessage(PROGRESS);
                        btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }

        } else if (v == btnAudioNext) {

            if (service != null) {
                try {
                    service.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        } else if (v == btnLyrc) {

        }
    }

    private void setPlayMode() {
        try {
            playMode = service.getPlayMode();

            if (playMode == NORMAL) {
                service.setPlayMode(SINGLE);
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
                Toast.makeText(getApplicationContext(), "单曲循环", Toast.LENGTH_SHORT).show();
            } else if (playMode == SINGLE) {
                service.setPlayMode(ALL);
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
                Toast.makeText(getApplicationContext(), "全部循环", Toast.LENGTH_SHORT).show();
            } else if (playMode == ALL) {
                service.setPlayMode(NORMAL);
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
                Toast.makeText(getApplicationContext(), "顺序播放", Toast.LENGTH_SHORT).show();
            } else {
                service.setPlayMode(NORMAL);
            }


        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViews();
        getData();
        initData();
        initView();
        bandAndStartService();
        setStatus();
    }

    private void setStatus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void initData() {
        utils = new Utils();
        receiver1 = new MyReceiver1();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(MusicPlayerService.OPENAUDIO);
        registerReceiver(receiver1, intentFilter1);
        receiver2 = new MyReceiver2();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(MusicPlayerService.FINISH);
        registerReceiver(receiver2, intentFilter2);
    }


    class MyReceiver1 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showViewData();
        }
    }

    class MyReceiver2 extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
        }
    }

    private void checkPlayMode() {

        try {
            if (service.getPlayMode() == 0) {
                service.setPlayMode(NORMAL);
            } else if (service.getPlayMode() == NORMAL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            } else if (service.getPlayMode() == SINGLE) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            } else if (service.getPlayMode() == ALL) {
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void showViewData() {

        try {
            tvArtist.setText(service.getArtist());
            tvName.setText(service.getName());
            seekbarAudio.setMax(service.getDuration());
            if (service.isPlaying()) {
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
            } else {
                btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
            }
            handler.sendEmptyMessage(PROGRESS);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case (PROGRESS):
                    try {
                        int currentPosition = service.getCurrentPosition();
                        seekbarAudio.setProgress(currentPosition);
                        tvTime.setText(utils.stringForTime(currentPosition) + "/" + utils.stringForTime(service.getDuration()));
                        showLyricView.setShowLyric(currentPosition);
                        showLyric();
                        removeMessages(PROGRESS);
                        sendEmptyMessage(PROGRESS);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;

            }

        }
    };

    private void showLyric() {
        LyricUtils lyricUtils = new LyricUtils();

        try {
            String path = service.getAudioPath();
            path = path.substring(0, path.lastIndexOf("."));
            File file = new File(path + ".lrc");
            lyricUtils.readLyricFile(file);
            showLyricView.setLyrics(lyricUtils.getLyrics());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void bandAndStartService() {
        Intent intent = new Intent(this, MusicPlayerService.class);
        Bundle bundle = new Bundle();
        if (mediaItems != null) {
            bundle.putSerializable("audiolist", mediaItems);
            intent.putExtras(bundle);
        }
        intent.putExtra("position",position);
        intent.setAction("com.panda.mobileplayer_OPENAUDIO");
        connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder iBinder) {
                service = IMusicPlayerService.Stub.asInterface(iBinder);
                if (service != null) {

                    showViewData();
                    checkPlayMode();

                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                if (service != null) {
                    try {
                        service.stop();
                        service = null;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

            }
        };
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        startService(intent);

    }

    private void getData() {
        isFromNotification = getIntent().getBooleanExtra("Notification", false);
        if (!isFromNotification) {
            position = getIntent().getIntExtra("position", 0);
            mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("audiolist");
        }
    }


    private void initView() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (receiver1 != null) {
            unregisterReceiver(receiver1);
            receiver1 = null;
        }
        if (receiver2 != null) {
            unregisterReceiver(receiver2);
            receiver2 = null;
        }
        if (connection != null) {
            unbindService(connection);
            connection = null;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();

    }
}
