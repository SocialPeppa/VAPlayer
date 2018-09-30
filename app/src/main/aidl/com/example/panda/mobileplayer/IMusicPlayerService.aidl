// IMusicPlayerService.aidl
package com.example.panda.mobileplayer;

// Declare any non-default types here with import statements

interface IMusicPlayerService {

     void openAudio(int position);

     void start();

     void pause();

     void stop();

     int getCurrentPosition();

     int getDuration();

     String getName();

     String getAudioPath();

     void next();

     void pre();

     void setPlayMode(int playMode);

     int getPlayMode();

     String getArtist();

     boolean isPlaying();

     void seekTo(int position);

      int getPositionTwo();

      int getAudioSessionId();


 }
