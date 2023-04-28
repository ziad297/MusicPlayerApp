package com.ziad.musicplayerapp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noMusicTV;

    ArrayList<Song> songsList = new ArrayList<>();
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 1;
    private static final int PERMISSION_REQUEST_CODE = 200;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.RecyclerViewSongs);
        noMusicTV = findViewById(R.id.NoSongsFound);
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filter search results as user types
                filterSearchResults(newText);
                return true;
            }
        });
        requestPermissions();


    }
    private boolean checkPermissions (){
        int result = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);

        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (checkPermissions())
        {
            //Toast.makeText(this, "Permission granted.!!!!!! ", Toast.LENGTH_SHORT).show();
            loadSongs();
        }
        else
            requestPermission();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }


    private void loadSongs() {
        songsList = new ArrayList<>(); // Initialize the songsList ArrayList
        String[] projection={
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC +" != 0";

        String sortOrder = MediaStore.Audio.Media.TITLE + "ASC";

        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection , selection , null, null);

        Log.i(TAG, "loadSongs: " + cursor.getCount());
        while (cursor.moveToNext()) {


            // 1 >> Data(path) , 0 >> title , 2 >> Duration
            Song songData = new Song(cursor.getString(1), cursor.getString(0), cursor.getString(2));
            Log.i(TAG, "loadSongs: " + songData);

            songsList.add(songData);
        }
        cursor.close();

        Collections.reverse(songsList);

        if (songsList.size() == 0) {
            noMusicTV.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new SongAdapter(getApplicationContext(), songsList));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean storageAccept = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (storageAccept) {
                    Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
                    loadSongs();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (recyclerView != null){
            recyclerView.setAdapter(new SongAdapter(getApplicationContext() , songsList));
        }
    }
    private void performSearch(String query) {
        ArrayList<Song> searchResults = new ArrayList<>();
        for (Song song : songsList) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(song);
            }
        }
        if (searchResults.size() == 0) {
            noMusicTV.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            noMusicTV.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(new SongAdapter(getApplicationContext(), searchResults));
        }
    }

    private void filterSearchResults(String query) {
        ArrayList<Song> filteredResults = new ArrayList<>();
        for (Song song : songsList) {
            if (song.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredResults.add(song);
            }
        }
        recyclerView.setAdapter(new SongAdapter(getApplicationContext(), filteredResults));
    }
}