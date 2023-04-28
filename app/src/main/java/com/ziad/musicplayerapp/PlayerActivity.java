package com.ziad.musicplayerapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {
    TextView titleTV, currentTimeTV, totalTimeTV;
    SeekBar seekBar;
    ImageView pausePlay, nextBtn, previousBtn, musicIcon;

    ArrayList<Song> songsList;

    Song currentSong;

    MediaPlayer mediaPlayer = ManageMediaPlayer.getInstance();
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "music_channel";

    private static final String ACTION_SET_DEFAULT_MUSIC_PLAYER =
            "android.settings.APPLICATION_DETAILS_SETTINGS";


    int x = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        titleTV = findViewById(R.id.songTitle);
        currentTimeTV = findViewById(R.id.current_time);
        totalTimeTV = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seek_bar);
        pausePlay = findViewById(R.id.pausePlay);
        nextBtn = findViewById(R.id.next);
        previousBtn = findViewById(R.id.previous);
        musicIcon = findViewById(R.id.music_icon_big);

        titleTV.setSelected(true);

        songsList = new ArrayList<>();
        songsList = (ArrayList<Song>) getIntent().getSerializableExtra("LIST");
        // Initialize mediaPlayer
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(mp -> {
            // Play next song when current song finishes
            playNext();
        });
        setResourcesWithMusic();

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                    currentTimeTV.setText(convertToMMSS(mediaPlayer.getCurrentPosition() + ""));


                    if (mediaPlayer.isPlaying()) {
                        pausePlay.setImageResource(R.drawable.baseline_pause_24);
                        musicIcon.setRotation(x++);
                    } else {
                        pausePlay.setImageResource(R.drawable.baseline_play_arrow_24);

                    }

                }

                new Handler().postDelayed(this, 100);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("music_channel",
                    "Music Channel",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        createNotificationChannel();

        // Set up the notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_music_note_24)
                .setContentTitle("Music Player")
                .setContentText(currentSong.getTitle())
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        // Set up the pending intents for the notification buttons
        Intent playPauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(NotificationReceiver.ACTION_PLAY_PAUSE);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(this, 0,
                playPauseIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(mediaPlayer.isPlaying() ? R.drawable.baseline_pause_24 : R.drawable.baseline_play_arrow_24,
                mediaPlayer.isPlaying() ? "Pause" : "Play", playPausePendingIntent);

        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(NotificationReceiver.ACTION_PREVIOUS);
        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(this, 0,
                prevIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPendingIntent);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(NotificationReceiver.ACTION_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(this, 0,
                nextIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.addAction(R.drawable.baseline_skip_next_24, "Next", nextPendingIntent);

        // Set up the pending intent for when the notification is clicked
        Intent appIntent = new Intent(this, PlayerActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(this, 0,
                appIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(appPendingIntent);

        // Display the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }

    void setResourcesWithMusic() {
        if (songsList == null || songsList.isEmpty()) {
            // Handle empty songsList
            return;
        }

        int currentIndex = ManageMediaPlayer.getCurrentIndex();
        if (currentIndex < 0 || currentIndex >= songsList.size()) {
            // Handle invalid currentIndex
            return;
        }

        currentSong = songsList.get(currentIndex);
        titleTV.setText(currentSong.getTitle());
        totalTimeTV.setText(convertToMMSS(currentSong.getDuration()));

        pausePlay.setOnClickListener(v -> togell());
        nextBtn.setOnClickListener(v -> playNext());
        previousBtn.setOnClickListener(v -> playPraveious());

        playMusic();
    }


    private void playMusic() {
        mediaPlayer.reset();
        try {

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.reset();
                    ManageMediaPlayer.currentIndex += 1;
                    setResourcesWithMusic();
                }
            });
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            seekBar.setProgress(0);
            seekBar.setMax(mediaPlayer.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void playNext() {
        if (ManageMediaPlayer.getCurrentIndex() == songsList.size() - 1)
            return;
        ManageMediaPlayer.currentIndex += 1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void playPraveious() {
        if (ManageMediaPlayer.currentIndex == 0)
            return;
        ManageMediaPlayer.currentIndex -= 1;
        mediaPlayer.reset();
        setResourcesWithMusic();
    }

    private void togell() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.pause();
        else
            mediaPlayer.start();

    }

    public static String convertToMMSS(String duration) {
        Long millis = Long.parseLong(duration);
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Music Channel",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    public  class NotificationReceiver extends BroadcastReceiver {
        public static final String ACTION_PLAY_PAUSE = "com.ziad.musicplayer.ACTION_PLAY_PAUSE";
        public static final String ACTION_PREVIOUS = "com.ziad.musicplayer.ACTION_PREVIOUS";
        public static final String ACTION_NEXT = "com.ziad.musicplayer.ACTION_NEXT";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_PLAY_PAUSE)) {
                togell();
            } else if (action.equals(ACTION_PREVIOUS)) {
                playPraveious();
            } else if (action.equals(ACTION_NEXT)) {
                playNext();
            }
        }
    }
}

