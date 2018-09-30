package com.example.panda.mobileplayer.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.domain.MediaItem;
import com.example.panda.mobileplayer.utils.Utils;
import com.example.panda.mobileplayer.view.VitamioVideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

public class VitamioVideoPlayer extends Activity implements View.OnClickListener {
    private static final int PROGRESS = 1;
    private static final int HIDE_MEDIACONTROLLER = 2;
    private static final int SHOW_NETSPEED=3;
    private static final int FULL_SCREEN = 1;
    private static final int DEFAULT_SCREEN = 2;
    private VitamioVideoView videoview;
    private Uri uri;

    private TextView tvName;
    private ImageView ivBattery;
    private TextView tvSystemTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private Button btnSwitchPlayer;

    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnExit;
    private Button btnVideoPre;
    private Button btnVideoPause;
    private Button btnVideoNext;
    private Button btnVideoSwitchScreen;
    private Utils utils;
    private MyReceiver receiver;
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private GestureDetector detector;
    private RelativeLayout media_controller;
    private boolean isshowMediaController=false;
    private boolean isFullScreen=false;
    private int screenWidth;
    private int screenHeight;
    private int videoWideth;
    private int videoHeight;
    private AudioManager am;
    private int currentVoice;
    private int maxVoice;
    private boolean isMute=false;
    private boolean isNetUri;
    private LinearLayout ll_buffer;
    private TextView tv_buffer_netspeed;
    private LinearLayout ll_loading;
    private TextView tv_loading_netspeed;

    private void findViews() {
        setContentView(R.layout.activity_vitamio_video_player);

        tvName = findViewById(R.id.tv_name);
        ivBattery = findViewById(R.id.iv_battery);
        tvSystemTime = findViewById(R.id.tv_system_time);
        btnVoice = findViewById(R.id.btn_voice);
        seekbarVoice = findViewById(R.id.seekbar_voice);
        btnSwitchPlayer = findViewById(R.id.btn_switch_player);

        tvCurrentTime = findViewById(R.id.tv_current_time);
        seekbarVideo = findViewById(R.id.seekbar_video);
        tvDuration = findViewById(R.id.tv_duration);
        btnExit = findViewById(R.id.btn_exit);
        btnVideoPre = findViewById(R.id.btn_video_pre);
        btnVideoPause = findViewById(R.id.btn_video_pause);
        btnVideoNext = findViewById(R.id.btn_video_next);
        btnVideoSwitchScreen = findViewById(R.id.btn_video_switch_screen);
        videoview =findViewById(R.id.vitamiovideoview);
        media_controller = findViewById(R.id.media_controller);
        tv_buffer_netspeed = findViewById(R.id.tv_buffer_netspeed);
        ll_buffer = findViewById(R.id.ll_buffer);
        ll_loading = findViewById(R.id.ll_loading);
        tv_loading_netspeed = findViewById(R.id.tv_loading_netspeed);

        btnVoice.setOnClickListener(this);
        btnSwitchPlayer.setOnClickListener(this);
        btnExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSwitchScreen.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            isMute=!isMute;
            upDataVoice(currentVoice,isMute);
        } else if (v == btnSwitchPlayer) {
            showSwitchPlayerDialog();
        } else if (v == btnExit) {
            finish();
        } else if (v == btnVideoPre) {
            playPreVideo();
        } else if (v == btnVideoPause) {
            startAndPause();
        } else if (v == btnVideoNext) {
            playNextVidoe();
        } else if (v == btnVideoSwitchScreen) {
            setFullAndDefault();
        }
        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
    }

    private void showSwitchPlayerDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("当前是万能播放器");
        builder.setMessage("是否切换到系统播放器");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startSystemVideoPlayer();
            }
        });
        builder.setNegativeButton("取消",null);

        builder.show();
    }

    private void startSystemVideoPlayer() {
        if(videoview!=null){
            videoview.stopPlayback();
        }
        Intent intent=new Intent(this,SystemVideoPlayer.class);
        if (mediaItems!=null&&mediaItems.size()>0){
            Bundle bundle=new Bundle();
            bundle.putSerializable("videolist",mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position",position);
        }else if (uri!=null){
            intent.setData(uri);
        }
        startActivity(intent);
        finish();
    }

    private void startAndPause() {
        if (videoview.isPlaying()) {
            videoview.pause();
            handler.removeMessages(PROGRESS);
            btnVideoPause.setBackgroundResource(R.drawable.btn_video_start_selector);
        } else {
            videoview.start();
            handler.sendEmptyMessage(PROGRESS);
            btnVideoPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    private void playPreVideo() {
        if (mediaItems!=null&&mediaItems.size()>0){
            position--;
            if (position>=0){
                ll_loading.setVisibility(View.VISIBLE);
                handler.sendEmptyMessage(SHOW_NETSPEED);
                MediaItem mediaItem=mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                videoview.setVideoPath(mediaItem.getData());
                isNetUri=utils.isNetUri(mediaItem.getData());
            }
        }else if (uri!=null){

        }
    }

    private void playNextVidoe() {
        if (mediaItems!=null&&mediaItems.size()>0){
            position++;
            if (position<mediaItems.size()){
                ll_loading.setVisibility(View.VISIBLE);
                handler.sendEmptyMessage(SHOW_NETSPEED);
                MediaItem mediaItem=mediaItems.get(position);
                tvName.setText(mediaItem.getName());
                videoview.setVideoPath(mediaItem.getData());
                isNetUri=utils.isNetUri(mediaItem.getData());
            }
        }else if (uri!=null){

        }
    }

    private void setButtonState() {
        if (mediaItems!=null&&mediaItems.size()>0){
            if (mediaItems.size()==1){
                setEnable(false);
            }else if (position==0){
                btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
                btnVideoPre.setEnabled(false);
            }else if(position==mediaItems.size()-1){
                btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
                btnVideoNext.setEnabled(false);
            }else {
                setEnable(true);
        }

        }else if(uri!=null){
            setEnable(false);


        }

    }

    private void setEnable(boolean isEnable) {
        if (isEnable){
            btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_seletor);
            btnVideoNext.setEnabled(true);
            btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_seletor);
            btnVideoPre.setEnabled(true);
        }else {
        btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
        btnVideoNext.setEnabled(false);
        btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
        btnVideoPre.setEnabled(false);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case PROGRESS:
                    int currentPosition = (int) videoview.getCurrentPosition();
                    seekbarVideo.setProgress(currentPosition);
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    tvSystemTime.setText(getSystemTime());
                    if (isNetUri){
                        int buffer=videoview.getBufferPercentage();
                        int totalbuffer=buffer*seekbarVideo.getMax();
                        int secondaryProgress=totalbuffer/100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);
                    }else {

                    }

                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessage(PROGRESS);
                    break;
                case HIDE_MEDIACONTROLLER:
                    hideMediaController();
                    break;
                case SHOW_NETSPEED:
                    String netSpeed=utils.getNetSpeed(VitamioVideoPlayer.this);
                    handler.removeMessages(SHOW_NETSPEED);
                    handler.sendEmptyMessageDelayed(SHOW_NETSPEED,1000);
                    tv_buffer_netspeed.setText("加载中...."+netSpeed);
                    tv_loading_netspeed.setText("加载中...."+netSpeed);
                    break;
            }
        }
    };

    private String getSystemTime() {
        SimpleDateFormat format=new SimpleDateFormat("HH:mm:ss");
        String systemTime=format.format(new Date());
        return systemTime;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        setListener();
        getData();
        setData();
        MyGestureListener();
        getWindowParams();
        setVoicebar();
        handler.sendEmptyMessage(SHOW_NETSPEED);
        Vitamio.isInitialized(this);
        // videoview.setMediaController(new MediaController(this));//系统自带的控制面板
    }

    private void setVoicebar() {
        seekbarVoice.setMax(maxVoice);
        seekbarVoice.setProgress(currentVoice);
    }


    private void MyGestureListener() {
        detector=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                setFullAndDefault();
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isshowMediaController){
                    hideMediaController();
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                }else {
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
                }

                return super.onSingleTapConfirmed(e);
            }
        });
    }

    private void setFullAndDefault() {
        if(isFullScreen){
            setVideoType(DEFAULT_SCREEN);
        }else{
            setVideoType(FULL_SCREEN);
        }
    }

    private void getWindowParams() {
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth=displayMetrics.widthPixels;
        screenHeight=displayMetrics.heightPixels;
        //得到音量
        am= (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVoice=am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVoice=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    private void setVideoType(int defaultScreen) {
        switch (defaultScreen){
            case  FULL_SCREEN:
                videoview.setVideoSize(screenWidth,screenHeight);
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_defaule_seletor);
                isFullScreen=true;
                break;
            case DEFAULT_SCREEN:
                int mVideoWidth=videoWideth;
                int mVideoHeight=videoHeight;
                int width=screenWidth;
                int height=screenHeight;

                if ( mVideoWidth * height  < width * mVideoHeight ) {

                    width = height * mVideoWidth / mVideoHeight;
                } else if ( mVideoWidth * height  > width * mVideoHeight ) {

                    height = width * mVideoHeight / mVideoWidth;
                }
                videoview.setVideoSize(width,height);
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_video_switch_screen_full_seletor);
                isFullScreen=false;
                break;

        }
    }

    private void showMediaController(){
        media_controller.setVisibility(View.VISIBLE);
        isshowMediaController=true;
    }
  private void hideMediaController(){
        media_controller.setVisibility(View.GONE);
        isshowMediaController=false;
    }

    private void setData() {
        if (mediaItems!=null&&mediaItems.size()>0){
            MediaItem mediaItem=mediaItems.get(position);
            isNetUri=utils.isNetUri(mediaItem.getData());
            tvName.setText(mediaItem.getName());
            videoview.setVideoPath(mediaItem.getData());

        }else if (uri != null) {
            tvName.setText(uri.toString());
            videoview.setVideoURI(uri);
            isNetUri=utils.isNetUri(uri.toString());
        }

    }

    private void getData() {
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position=getIntent().getIntExtra("position",0);

    }

    private void initData() {
        utils = new Utils();
        receiver = new MyReceiver();
        IntentFilter intentfiler = new IntentFilter();
        intentfiler.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, intentfiler);
    }

    public void setBattery(int battery) {
        if(battery<=0){
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        }else if(battery<=10){
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        }else if(battery<=20){
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        }else if(battery<=40){
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        }else if(battery<=60){
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        }else if(battery<=80){
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        }else if(battery<=100){
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            setBattery(level);
        }
    }

    private void setListener() {
        videoview.setOnPreparedListener(new MyOnPreparedListener());
        videoview.setOnErrorListener(new MyOnErrorListener());
        videoview.setOnCompletionListener(new MyOnCompletionListener());
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
        videoview.setOnInfoListener(new MyOnInfoListener());
    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener{

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what){
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    ll_buffer.setVisibility(View.VISIBLE);
                    handler.sendEmptyMessage(SHOW_NETSPEED);
                    break;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    ll_buffer.setVisibility(View.GONE);
                    handler.removeMessages(SHOW_NETSPEED);
                    break;
            }
            return false;
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                if (progress>0){
                    isMute=false;
                }else{
                    isMute=true;
                }
                upDataVoice(progress,isMute);

            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
        }
    }

    private void upDataVoice(int progress,boolean isMute) {
        if(isMute){
            am.setStreamVolume(AudioManager.STREAM_MUSIC,0,0);
            seekbarVoice.setProgress(0);
        }else {
        am.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
        seekbarVoice.setProgress(progress);
        currentVoice=progress;
        }
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoview.seekTo(progress);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            videoview.start();
            setButtonState();
            int duration = (int) videoview.getDuration();
            seekbarVideo.setMax(duration);
            tvDuration.setText(utils.stringForTime(duration));
            handler.sendEmptyMessage(PROGRESS);
            hideMediaController();
            videoWideth=mp.getVideoWidth();
            videoHeight=mp.getVideoHeight();
            setVideoType(DEFAULT_SCREEN);
            ll_loading.setVisibility(View.GONE);
            handler.removeMessages(SHOW_NETSPEED);

        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            showErrorDialog();
            return true;
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("播放错误");
        builder.setMessage("抱歉，无法播放");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            playNextVidoe();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);

        if (receiver!=null){
        unregisterReceiver(receiver);
        receiver=null;
        }
        super.onDestroy();
    }

    private float startY;
    private float touchRang;//移动范围 屏幕的高
    private int mVol;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                touchRang = Math.min(screenHeight, screenWidth);
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:
                float endY = event.getY();
                float endX = event.getX();
                float distanceY = startY - endY;
                if (endX < screenWidth / 2) {
                    final double FLING_MIN_DISTANCE = 0.5;
                    final double FLING_MIN_VELOCITY = 0.5;

                    if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(20);
                    }
                    if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                        setBrightness(-20);
                    }

                } else {
                    float delta = (distanceY / touchRang) * maxVoice;
                    int voice = (int) Math.min(Math.max(mVol + delta, 0), maxVoice);
                    if (delta != 0) {
                        isMute = false;
                        upDataVoice(voice, isMute);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 4000);
                break;

        }
        return super.onTouchEvent(event);
    }

    public void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            currentVoice--;
            upDataVoice(currentVoice,false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
            return true;
        }else if (keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            currentVoice++;
            upDataVoice(currentVoice,false);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,4000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}