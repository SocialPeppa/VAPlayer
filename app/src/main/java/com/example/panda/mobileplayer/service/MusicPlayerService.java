package com.example.panda.mobileplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.example.panda.mobileplayer.IMusicPlayerService;
import com.example.panda.mobileplayer.R;
import com.example.panda.mobileplayer.activity.AudioPlayerActivity;
import com.example.panda.mobileplayer.domain.MediaItem;
import com.example.panda.mobileplayer.utils.CacheUtils;
import java.io.IOException;
import java.util.ArrayList;
import io.vov.vitamio.utils.Log;


public class MusicPlayerService extends Service {

    public static final String OPENAUDIO = "OPENAUDIO";
    public static final String FINISH = "FINISH";
    private static final String YOUR_CHANNEL_ID = "YOUR_CHANNEL_ID";
    private static final String YOUR_CHANNEL_NAME = "播放提示";
    private ArrayList<MediaItem> mediaItems;
    private int positionTwo = 30000000;
    private int position;
    private MediaItem mediaItem;
    private MediaPlayer mediaPlayer;
    private NotificationManager manager;
    private int playMode;
    private int duration;



    @Override
    public void onCreate() {
        super.onCreate();
        playMode = CacheUtils.getPlayMode(this, "playMode");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getExtras()!=null) {
            mediaItems = (ArrayList<MediaItem>) intent.getExtras().getSerializable("audiolist");
        }
        position=intent.getIntExtra("position",0);
        if(position!=positionTwo){
            openAudio(position);
        }
        return START_STICKY;
    }


    private IMusicPlayerService.Stub stub = new IMusicPlayerService.Stub() {
        MusicPlayerService service = MusicPlayerService.this;

        @Override
        public void openAudio(int position) throws RemoteException {
            service.openAudio(position);
        }

        @Override
        public void start() throws RemoteException {
            service.start();
        }

        @Override
        public void pause() throws RemoteException {
            service.pause();
        }

        @Override
        public void stop() throws RemoteException {
            service.stop();
        }

        @Override
        public int getCurrentPosition() throws RemoteException {
            return service.getCurrentPosition();
        }

        @Override
        public int getDuration() throws RemoteException {
            return service.getDuration();
        }

        @Override
        public String getName() throws RemoteException {
            return service.getName();
        }

        @Override
        public String getAudioPath() throws RemoteException {
            return service.getAudioPath();
        }

        @Override
        public void next() throws RemoteException {
            service.next();
        }

        @Override
        public void pre() throws RemoteException {
            service.pre();
        }

        @Override
        public void setPlayMode(int playMode) throws RemoteException {
            service.setPlayMode(playMode);
        }

        @Override
        public int getPlayMode() throws RemoteException {
            return service.getPlayMode();
        }

        @Override
        public String getArtist() throws RemoteException {
            return service.getArtist();
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return service.isPlaying();
        }

        @Override
        public void seekTo(int position) throws RemoteException {
            mediaPlayer.seekTo(position);

        }

        @Override
        public int getPositionTwo() throws RemoteException {
            return service.getPositionTwo();
        }

        @Override
        public int getAudioSessionId() throws RemoteException {
            return service.getAudioSessionId();
        }


    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    private void openAudio(int position) {
        positionTwo = position;

        if (mediaItems != null && mediaItems.size() > 0) {
            mediaItem = mediaItems.get(position);

            try {
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                }
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(mediaItem.getData());
                mediaPlayer.prepareAsync();
                mediaPlayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaPlayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaPlayer.setOnErrorListener(new MyOnErrorListener());

            } catch (IOException e) {
                e.printStackTrace();

            }

        } else {

        }
        if (playMode == AudioPlayerActivity.SINGLE) {
            mediaPlayer.setLooping(true);
        } else {
            mediaPlayer.setLooping(false);
        }


    }

    private int getPositionTwo() {
        return positionTwo;
    }

    private int getAudioSessionId(){
        return mediaPlayer.getAudioSessionId();
    }


    private void start() {
        mediaPlayer.start();
        Notification.Builder builder;
        if (manager == null) {
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(YOUR_CHANNEL_ID, YOUR_CHANNEL_NAME, manager.IMPORTANCE_MIN);
            manager.createNotificationChannel(channel);
            builder = new Notification.Builder(this, "YOUR_CHANNEL_ID");

        } else {
            builder = new Notification.Builder(this);

        }
        Intent intent = new Intent(this, AudioPlayerActivity.class);
        intent.putExtra("Notification", true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent)
                .setContentTitle("音乐播放器")
                .setContentText("正在播放" + getName())
                .setDefaults(0)
                .setSmallIcon(R.drawable.notification_music_playing);
        Notification notification = builder.build();
        notification.flags = notification.FLAG_NO_CLEAR;

        manager.notify(1, notification);

    }


    private void pause() {
        mediaPlayer.pause();
        manager.cancel(1);
    }

    private void stop() {
        mediaPlayer.stop();
    }

    private int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    private int getDuration() {
            return duration;

    }

    private String getName() {
        return mediaItem.getName();
    }

    private String getAudioPath() {
        return mediaItem.getData();
    }

    private String getArtist() {
        return mediaItem.getArtist();
    }

    private void next() {
        positionTwo++;

        if (positionTwo < mediaItems.size()) {
            openAudio(positionTwo);
        } else if (playMode == AudioPlayerActivity.NORMAL) {
            positionTwo--;


            Toast.makeText(this, "已经是最后一个了", Toast.LENGTH_SHORT).show();
        } else {
            openAudio(0);
        }
    }

    private void pre() {
        positionTwo--;
        if (positionTwo >= 0) {
            openAudio(positionTwo);
        } else if (playMode == AudioPlayerActivity.NORMAL) {
            positionTwo++;
            Toast.makeText(this, "已经是第一个了", Toast.LENGTH_SHORT).show();
        } else {
            openAudio(mediaItems.size() - 1);
        }

    }

    private void setPlayMode(int playMode) {
        this.playMode = playMode;
        CacheUtils.putPlayMode(this, "playMode", playMode);
        if (playMode == AudioPlayerActivity.SINGLE) {
            mediaPlayer.setLooping(true);
        } else {
            mediaPlayer.setLooping(false);
        }
    }

    private int getPlayMode() {

        return playMode;
    }

    private boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {
            duration=mediaPlayer.getDuration();
            notifyChange(OPENAUDIO);
            start();
        }

    }

    private void notifyChange(String action) {
        Intent intent = new Intent(action);
        sendBroadcast(intent);

    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (playMode == AudioPlayerActivity.NORMAL && positionTwo == mediaItems.size() - 1) {
                notifyChange(FINISH);
            }
            next();

        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return false;
        }
    }

}

