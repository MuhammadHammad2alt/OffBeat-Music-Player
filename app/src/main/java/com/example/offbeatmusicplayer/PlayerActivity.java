package com.example.offbeatmusicplayer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class PlayerActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    Button playBtn, equalizerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Buttons
        playBtn = findViewById(R.id.playBtn);
        equalizerBtn = findViewById(R.id.equalizerBtn);

        // Load song (put any mp3 in res/raw folder)
        mediaPlayer = MediaPlayer.create(this, R.raw.songs);

        // Play / Pause
        playBtn.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                playBtn.setText("Play");
            } else {
                mediaPlayer.start();
                playBtn.setText("Pause");
            }
        });

        // Open Equalizer
        equalizerBtn.setOnClickListener(v -> {

            if (mediaPlayer != null) {

                Intent intent = new Intent(PlayerActivity.this, EqualizerActivity.class);
                intent.putExtra("audioSessionId", mediaPlayer.getAudioSessionId());
                startActivity(intent);

            } else {
                Toast.makeText(this, "Start music first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
    }
}