package com.ziad.musicplayerapp;


import android.media.MediaPlayer;

public class ManageMediaPlayer {
    static MediaPlayer instance;
    private static int size=0;
    static int currentIndex = -1;


    public  void initialize(int size) {
      ManageMediaPlayer.size = size;
    }

    public static int getCurrentIndex() {
        return currentIndex;
    }

    public  void setCurrentIndex(int index) {
        currentIndex = index;
    }

    public  void playNext() {
        if (currentIndex < size - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
    }

    public  void playPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = size - 1;
        }
    }
    public static MediaPlayer getInstance() {
        if (instance == null)
            instance = new MediaPlayer();

        return instance;
    }


}
