package com.ziad.musicplayerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String ACTION_PLAY_PAUSE = "com.your.package.name.ACTION_PLAY_PAUSE";
    public static final String ACTION_PREVIOUS = "com.your.package.name.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.your.package.name.ACTION_NEXT";

    private MediaPlayer mediaPlayer;
    private ArrayList<Song> songList;
    private int currentIndex;

    public NotificationReceiver(MediaPlayer mediaPlayer, ArrayList<Song> songList, int currentIndex) {
        this.mediaPlayer = mediaPlayer;
        this.songList = songList;
        this.currentIndex = currentIndex;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case ACTION_PLAY_PAUSE:
                togglePlayback();
                break;
            case ACTION_PREVIOUS:
                playPrevious();
                break;
            case ACTION_NEXT:
                playNext();
                break;
        }
    }

    private void togglePlayback() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    private void playPrevious() {
        if (currentIndex > 0) {
            currentIndex--;
        } else {
            currentIndex = songList.size() - 1;
        }

        playSong(currentIndex);
    }

    private void playNext() {
        if (currentIndex < songList.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }

        playSong(currentIndex);
    }

    private void playSong(int index) {
        Song song = songList.get(index);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}