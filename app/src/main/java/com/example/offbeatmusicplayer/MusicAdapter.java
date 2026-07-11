package com.example.offbeatmusicplayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class MusicAdapter extends ArrayAdapter<MusicModel> {

    private final Context context;
    private final ArrayList<MusicModel> songs;

    public MusicAdapter(Context context, ArrayList<MusicModel> songs) {
        super(context, 0, songs);
        this.context = context;
        this.songs   = songs;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.song_item, parent, false);
            holder = new ViewHolder();
            holder.albumArtBg = convertView.findViewById(R.id.albumArtBg);
            holder.albumArtIcon = convertView.findViewById(R.id.albumArtIcon);
            holder.songName   = convertView.findViewById(R.id.songName);
            holder.artistName = convertView.findViewById(R.id.artistName);
            holder.duration   = convertView.findViewById(R.id.songDuration);
            holder.menuBtn    = convertView.findViewById(R.id.menuBtn);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MusicModel song = songs.get(position);

        holder.songName.setText(song.getSongName());
        holder.artistName.setText(song.getArtist());
        holder.duration.setText(MainActivity.formatDuration(song.getDuration()));

        // Load album art — real photo if available, colored bg if not
        AlbumArtLoader.load(context, song.getAlbumId(), position,
                holder.albumArtBg, holder.albumArtIcon);

        // 3-dot menu
        holder.menuBtn.setOnClickListener(v -> {
            boolean inPlaylist   = PlaylistManager.isInPlaylist(context, song);
            boolean inFavourites = PlaylistManager.isInFavourites(context, song);

            String playlistLabel   = inPlaylist   ? "✓ Remove from Playlist"   : "+ Add to Playlist";
            String favouritesLabel = inFavourites ? "✓ Remove from Favourites" : "♥ Add to Favourites";

            String[] options = {playlistLabel, favouritesLabel, "Share", "Info"};

            new android.app.AlertDialog.Builder(context)
                    .setTitle(song.getSongName())
                    .setItems(options, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                if (PlaylistManager.isInPlaylist(context, song)) {
                                    PlaylistManager.removeFromPlaylist(context, song);
                                    Toast.makeText(context, "Removed from Playlist", Toast.LENGTH_SHORT).show();
                                } else {
                                    PlaylistManager.addToPlaylist(context, song);
                                    Toast.makeText(context, "Added to Playlist ✓", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 1:
                                if (PlaylistManager.isInFavourites(context, song)) {
                                    PlaylistManager.removeFromFavourites(context, song);
                                    Toast.makeText(context, "Removed from Favourites", Toast.LENGTH_SHORT).show();
                                } else {
                                    PlaylistManager.addToFavourites(context, song);
                                    Toast.makeText(context, "Added to Favourites ♥", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case 2:
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.setType("audio/*");
                                intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(song.getSongPath()));
                                context.startActivity(Intent.createChooser(intent, "Share Song"));
                                break;
                            case 3:
                                new android.app.AlertDialog.Builder(context)
                                        .setTitle("Song Info")
                                        .setMessage(
                                                "Title:    " + song.getSongName() + "\n" +
                                                        "Artist:   " + song.getArtist()   + "\n" +
                                                        "Duration: " + MainActivity.formatDuration(song.getDuration())
                                        )
                                        .setPositiveButton("OK", null)
                                        .show();
                                break;
                        }
                    })
                    .show();
        });

        return convertView;
    }

    static class ViewHolder {
        ImageView   albumArtBg;
        ImageView   albumArtIcon;
        TextView    songName;
        TextView    artistName;
        TextView    duration;
        ImageButton menuBtn;
    }
}