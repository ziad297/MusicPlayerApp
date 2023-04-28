package com.ziad.musicplayerapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongsHolder> implements OnClickInterface {
    Context context;
    ArrayList<Song> songList;

    public SongAdapter(Context context, ArrayList<Song> songList) {
        this.context = context;
        this.songList = songList;
    }

    @NonNull
    @Override
    public SongsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new SongsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsHolder holder, @SuppressLint("RecyclerView") int position) {
        Song songData = songList.get(position);
        holder.titleTextView.setText(songData.getTitle());

        if (ManageMediaPlayer.currentIndex == position) {
            holder.titleTextView.setTextColor(Color.parseColor("#FF0000"));
        } else
            holder.titleTextView.setTextColor(Color.parseColor("#000000"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ManageMediaPlayer.getInstance().reset();
                ManageMediaPlayer.currentIndex = position;
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("LIST", songList);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    @Override
    public void onItemClick(int pos) {

    }

    public class SongsHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView iconImageView;

        public SongsHolder(@NonNull View itemView) {
            super(itemView);

            titleTextView = itemView.findViewById(R.id.music_title_text);
            iconImageView = itemView.findViewById(R.id.icon_view);
        }
    }
}
