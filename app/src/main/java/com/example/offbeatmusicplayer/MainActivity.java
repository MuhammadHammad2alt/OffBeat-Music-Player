package com.example.offbeatmusicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    ListView songListView;
    ArrayList<MusicModel> songsList = new ArrayList<>();
    MediaPlayer mediaPlayer;

    TextView currentSong, currentArtist;
    SeekBar seekBar;
    ImageButton playBtn, nextBtn, prevBtn;
    ImageView miniAlbumArt;

    // Tabs
    LinearLayout tabSongs, tabArtists, tabAlbums, tabFolders;
    TextView tabSongsText, tabArtistsText, tabAlbumsText, tabFoldersText;
    View tabSongsLine, tabArtistsLine, tabAlbumsLine, tabFoldersLine;

    int currentIndex = 0;
    Handler handler = new Handler();
    Runnable updateSeekBarRunnable;

    int[] albumBgColors = {
            R.drawable.album_art_bg_purple,
            R.drawable.album_art_bg_pink,
            R.drawable.album_art_bg_orange,
            R.drawable.album_art_bg_teal,
            R.drawable.album_art_bg_green
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        songListView  = findViewById(R.id.songListView);
        currentSong   = findViewById(R.id.currentSong);
        currentArtist = findViewById(R.id.currentArtist);
        seekBar       = findViewById(R.id.seekBar);
        playBtn       = findViewById(R.id.playBtn);
        nextBtn       = findViewById(R.id.nextBtn);
        prevBtn       = findViewById(R.id.prevBtn);
        miniAlbumArt  = findViewById(R.id.miniAlbumArt);

        // Bind tabs
        tabSongs       = findViewById(R.id.tabSongs);
        tabArtists     = findViewById(R.id.tabArtists);
        tabAlbums      = findViewById(R.id.tabAlbums);
        tabFolders     = findViewById(R.id.tabFolders);
        tabSongsText   = findViewById(R.id.tabSongsText);
        tabArtistsText = findViewById(R.id.tabArtistsText);
        tabAlbumsText  = findViewById(R.id.tabAlbumsText);
        tabFoldersText = findViewById(R.id.tabFoldersText);
        tabSongsLine   = findViewById(R.id.tabSongsLine);
        tabArtistsLine = findViewById(R.id.tabArtistsLine);
        tabAlbumsLine  = findViewById(R.id.tabAlbumsLine);
        tabFoldersLine = findViewById(R.id.tabFoldersLine);

        requestPermissionsAndLoad();

        // SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        // Play / Pause
        playBtn.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playBtn.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    mediaPlayer.start();
                    playBtn.setImageResource(android.R.drawable.ic_media_pause);
                }
            }
        });

        // Next
        nextBtn.setOnClickListener(v -> {
            currentIndex = (currentIndex < songsList.size() - 1) ? currentIndex + 1 : 0;
            playSong(currentIndex);
        });

        // Previous
        prevBtn.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.getCurrentPosition() > 3000) {
                mediaPlayer.seekTo(0);
            } else if (currentIndex > 0) {
                currentIndex--;
                playSong(currentIndex);
            }
        });

        // Search
        ImageButton searchBtn = findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Search Songs");
            final EditText input = new EditText(this);
            input.setHint("Type song or artist name...");
            input.setPadding(40, 20, 40, 20);
            builder.setView(input);
            builder.setPositiveButton("Search", (dialog, which) -> {
                String query = input.getText().toString().toLowerCase().trim();
                ArrayList<MusicModel> filtered = new ArrayList<>();
                for (MusicModel song : songsList) {
                    if (song.getSongName().toLowerCase().contains(query)
                            || song.getArtist().toLowerCase().contains(query)) {
                        filtered.add(song);
                    }
                }
                if (filtered.isEmpty()) {
                    Toast.makeText(this, "No songs found for: " + query, Toast.LENGTH_SHORT).show();
                } else {
                    songListView.setAdapter(new MusicAdapter(this, filtered));
                    songListView.setOnItemClickListener((parent, view, position, id) -> {
                        currentIndex = songsList.indexOf(filtered.get(position));
                        playSong(currentIndex);
                    });
                }
            });
            builder.setNegativeButton("Clear", (dialog, which) -> {
                songListView.setAdapter(new MusicAdapter(this, songsList));
                restoreListClickListener();
                setActiveTab(tabSongs);
            });
            builder.show();
        });

        // Menu
        ImageButton menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> {
            String[] menuItems = {"Library", "Playlist", "Settings", "About", "Share App", "Rate Us"};
            new AlertDialog.Builder(this)
                    .setTitle("OffBeat Music Player")
                    .setItems(menuItems, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                setActiveTab(tabSongs);
                                songListView.setAdapter(new MusicAdapter(this, songsList));
                                restoreListClickListener();
                                break;
                            case 1:
                                showPlaylist();
                                break;
                            case 2:
                                openEqualizer();
                                break;
                            case 3:
                                new AlertDialog.Builder(this)
                                        .setTitle("About")
                                        .setMessage("OffBeat Music Player\nVersion 1.0\n\nBuilt By Muhammad Hammad")
                                        .setPositiveButton("OK", null)
                                        .show();
                                break;
                            case 4:
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("text/plain");
                                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out OffBeat Music Player!");
                                startActivity(Intent.createChooser(shareIntent, "Share via"));
                                break;
                            case 5:
                                Toast.makeText(this, "Thanks for the love! ⭐", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    })
                    .show();
        });

        // Tabs
        tabSongs.setOnClickListener(v -> {
            setActiveTab(tabSongs);
            songListView.setAdapter(new MusicAdapter(this, songsList));
            restoreListClickListener();
        });

        tabArtists.setOnClickListener(v -> {
            setActiveTab(tabArtists);
            ArrayList<MusicModel> byArtist = new ArrayList<>(songsList);
            byArtist.sort((a, b) -> a.getArtist().compareToIgnoreCase(b.getArtist()));
            songListView.setAdapter(new MusicAdapter(this, byArtist));
            songListView.setOnItemClickListener((parent, view, position, id) -> {
                currentIndex = songsList.indexOf(byArtist.get(position));
                playSong(currentIndex);
            });
        });

        tabAlbums.setOnClickListener(v -> {
            setActiveTab(tabAlbums);
            ArrayList<MusicModel> byAlbum = new ArrayList<>(songsList);
            byAlbum.sort((a, b) ->
                    getAlbumFromPath(a.getSongPath()).compareToIgnoreCase(getAlbumFromPath(b.getSongPath())));
            songListView.setAdapter(new MusicAdapter(this, byAlbum));
            songListView.setOnItemClickListener((parent, view, position, id) -> {
                currentIndex = songsList.indexOf(byAlbum.get(position));
                playSong(currentIndex);
            });
        });

        tabFolders.setOnClickListener(v -> {
            setActiveTab(tabFolders);
            ArrayList<MusicModel> byFolder = new ArrayList<>(songsList);
            byFolder.sort((a, b) ->
                    getFolderFromPath(a.getSongPath()).compareToIgnoreCase(getFolderFromPath(b.getSongPath())));
            songListView.setAdapter(new MusicAdapter(this, byFolder));
            songListView.setOnItemClickListener((parent, view, position, id) -> {
                currentIndex = songsList.indexOf(byFolder.get(position));
                playSong(currentIndex);
            });
        });

        // Bottom nav
        findViewById(R.id.navHome).setOnClickListener(v -> {
            setActiveTab(tabSongs);
            songListView.setAdapter(new MusicAdapter(this, songsList));
            restoreListClickListener();
        });

        findViewById(R.id.navPlaylist).setOnClickListener(v -> showPlaylist());

        findViewById(R.id.navEqualizer).setOnClickListener(v -> openEqualizer());

        findViewById(R.id.navFavorites).setOnClickListener(v -> showFavourites());
    }

    // ─── Playlist ──────────────────────────────────────────────────────────────

    private void showPlaylist() {
        ArrayList<MusicModel> playlist = PlaylistManager.getPlaylist(this);
        if (playlist.isEmpty()) {
            Toast.makeText(this,
                    "Playlist is empty. Long-press ⋮ on any song to add.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        songListView.setAdapter(new MusicAdapter(this, playlist));
        songListView.setOnItemClickListener((parent, view, position, id) -> {
            // Find real index in full list
            currentIndex = songsList.indexOf(playlist.get(position));
            if (currentIndex == -1) currentIndex = 0;
            playSong(currentIndex);
        });
        Toast.makeText(this, "Playlist — " + playlist.size() + " songs", Toast.LENGTH_SHORT).show();
    }

    // ─── Favourites ────────────────────────────────────────────────────────────

    private void showFavourites() {
        ArrayList<MusicModel> favs = PlaylistManager.getFavourites(this);
        if (favs.isEmpty()) {
            Toast.makeText(this,
                    "No favourites yet. Tap ⋮ on any song to add.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        songListView.setAdapter(new MusicAdapter(this, favs));
        songListView.setOnItemClickListener((parent, view, position, id) -> {
            currentIndex = songsList.indexOf(favs.get(position));
            if (currentIndex == -1) currentIndex = 0;
            playSong(currentIndex);
        });
        Toast.makeText(this, "Favourites — " + favs.size() + " songs", Toast.LENGTH_SHORT).show();
    }

    // ─── Equalizer ─────────────────────────────────────────────────────────────

    private void openEqualizer() {
        Intent intent = new Intent(this, EqualizerActivity.class);
        intent.putExtra("audioSessionId",
                mediaPlayer != null ? mediaPlayer.getAudioSessionId() : 0);
        startActivity(intent);
    }

    // ─── Tab Highlight ─────────────────────────────────────────────────────────

    private void setActiveTab(LinearLayout activeTab) {
        tabSongsText.setTextColor(Color.parseColor("#CCB8FF"));
        tabSongsText.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabSongsLine.setBackgroundColor(Color.TRANSPARENT);

        tabArtistsText.setTextColor(Color.parseColor("#CCB8FF"));
        tabArtistsText.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabArtistsLine.setBackgroundColor(Color.TRANSPARENT);

        tabAlbumsText.setTextColor(Color.parseColor("#CCB8FF"));
        tabAlbumsText.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabAlbumsLine.setBackgroundColor(Color.TRANSPARENT);

        tabFoldersText.setTextColor(Color.parseColor("#CCB8FF"));
        tabFoldersText.setTypeface(null, android.graphics.Typeface.NORMAL);
        tabFoldersLine.setBackgroundColor(Color.TRANSPARENT);

        TextView activeText;
        View activeLine;

        if (activeTab == tabSongs) {
            activeText = tabSongsText; activeLine = tabSongsLine;
        } else if (activeTab == tabArtists) {
            activeText = tabArtistsText; activeLine = tabArtistsLine;
        } else if (activeTab == tabAlbums) {
            activeText = tabAlbumsText; activeLine = tabAlbumsLine;
        } else {
            activeText = tabFoldersText; activeLine = tabFoldersLine;
        }

        activeText.setTextColor(Color.WHITE);
        activeText.setTypeface(null, android.graphics.Typeface.BOLD);
        activeLine.setBackgroundColor(Color.WHITE);
    }

    // ─── Permissions ───────────────────────────────────────────────────────────

    private void requestPermissionsAndLoad() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO}, 1);
            } else {
                loadSongs();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                loadSongs();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            Toast.makeText(this, "Permission denied. Cannot load songs.", Toast.LENGTH_LONG).show();
        }
    }

    // ─── Load Songs ────────────────────────────────────────────────────────────

    private void loadSongs() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ALBUM_ID  // NEW
        };
        Cursor cursor = getContentResolver().query(
                uri, projection,
                MediaStore.Audio.Media.IS_MUSIC + " != 0",
                null,
                MediaStore.Audio.Media.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name    = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String path    = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                String artist  = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                long duration  = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                long albumId   = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)); // NEW

                if (name.contains(".")) name = name.substring(0, name.lastIndexOf('.'));
                if (artist == null || artist.equals("<unknown>")) artist = "Unknown Artist";

                songsList.add(new MusicModel(name, path, artist, duration, albumId));
            }
            cursor.close();
        }

        songListView.setAdapter(new MusicAdapter(this, songsList));
        restoreListClickListener();
        setActiveTab(tabSongs);

        if (songsList.isEmpty()) {
            Toast.makeText(this, "No songs found on device", Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Play Song ─────────────────────────────────────────────────────────────

    private void playSong(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (updateSeekBarRunnable != null) handler.removeCallbacks(updateSeekBarRunnable);

        MusicModel song = songsList.get(position);
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(song.getSongPath());
            mediaPlayer.prepare();
            mediaPlayer.start();

            currentSong.setText(song.getSongName());
            currentArtist.setText(song.getArtist());
            playBtn.setImageResource(android.R.drawable.ic_media_pause);
            seekBar.setMax(mediaPlayer.getDuration());
            seekBar.setProgress(0);
            miniAlbumArt.setBackgroundResource(albumBgColors[position % albumBgColors.length]);

            mediaPlayer.setOnCompletionListener(mp -> {
                currentIndex = (currentIndex < songsList.size() - 1) ? currentIndex + 1 : 0;
                playSong(currentIndex);
            });

            startSeekBarUpdater();

        } catch (IOException e) {
            Toast.makeText(this, "Cannot play: " + song.getSongName(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // ─── SeekBar ───────────────────────────────────────────────────────────────

    private void startSeekBarUpdater() {
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.post(updateSeekBarRunnable);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private void restoreListClickListener() {
        songListView.setOnItemClickListener((parent, view, position, id) -> {
            currentIndex = position;
            playSong(currentIndex);
        });
    }

    private String getAlbumFromPath(String path) {
        try { return new File(path).getParentFile().getName(); }
        catch (Exception e) { return "Unknown Album"; }
    }

    private String getFolderFromPath(String path) {
        try { return new File(path).getParentFile().getAbsolutePath(); }
        catch (Exception e) { return "Unknown Folder"; }
    }

    public static String formatDuration(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    // ─── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }
}